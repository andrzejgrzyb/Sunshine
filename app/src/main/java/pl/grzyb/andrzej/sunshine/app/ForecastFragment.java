package pl.grzyb.andrzej.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import pl.grzyb.andrzej.sunshine.app.data.WeatherContract;
import pl.grzyb.andrzej.sunshine.app.sync.SunshineSyncAdapter;


/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public ForecastAdapter getmForecastAdapter() {
        return mForecastAdapter;
    }

    private ForecastAdapter mForecastAdapter;
    private boolean mUseTodayLayout = true;
    private int mPosition;
    public static final String SELECTED_KEY = "ListViewPositionKey";
    private ListView mListView;
    private View mRootView;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    private static final int FORECAST_LOADER = 0;

    private static final String[] FORECAST_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_COORD_LAT,
            WeatherContract.LocationEntry.COLUMN_COORD_LONG,
            WeatherContract.LocationEntry.COLUMN_CITY_NAME
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_LOCATION_SETTING = 5;
    static final int COL_WEATHER_CONDITION_ID = 6;
    static final int COL_COORD_LAT = 7;
    static final int COL_COORD_LONG = 8;
    static final int COL_CITY_NAME = 9;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_location_status_key))) {
            updateEmptyView();
        }
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        public void onItemSelected(Uri dateUri);
    }

    public ForecastFragment() {
    }
    public void onResume() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    public void onPause() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        getLoaderManager().initLoader(FORECAST_LOADER, null, this);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
      //  inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        if (id == R.id.action_showlocation) {
            openPreferredLocationMap();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateWeather() {

        SunshineSyncAdapter.syncImmediately(getActivity());

        Utility.showToast(getActivity(), "Query sent for location: " + Utility.getPreferredLocation(getActivity()));
    }

    public void setUseTodayLayout(boolean useTodayLayout) {
        if (mForecastAdapter != null) {
            mForecastAdapter.setUseTodayLayout(useTodayLayout);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // The CursorAdapter will take data from our cursor and populate the ListView.
        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        mRootView = inflater.inflate(R.layout.fragment_main, container, false);

        mListView = (ListView) mRootView.findViewById(R.id.listview_forecast);
        mListView.setAdapter(mForecastAdapter);
        mListView.setEmptyView(mRootView.findViewById(R.id.listview_empty_item));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                if (cursor != null && cursor.moveToPosition(position)) {
                    mPosition = position;

                    String locationSetting = Utility.getPreferredLocation(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(
                                    WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                                            locationSetting,
                                            cursor.getLong(COL_WEATHER_DATE)
                                    ));

//                    Intent intent = new Intent(getActivity(), DetailActivity.class)
//                            .setData(WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                    startActivity(intent);
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        mForecastAdapter.setUseTodayLayout(mUseTodayLayout);

        return mRootView;
    }

    public void openPreferredLocationMap() {


        if (null != mForecastAdapter) {
            Cursor cursor = mForecastAdapter.getCursor();
            cursor.moveToFirst();
            Double lat = cursor.getDouble(COL_COORD_LAT);
            Double lon = cursor.getDouble(COL_COORD_LONG);
            String cityName = cursor.getString(COL_CITY_NAME);


            Intent intent = new Intent(Intent.ACTION_VIEW);
//        old one using the location from SharedPreferences
//        String location = Utility.getPreferredLocation(getActivity());
//
//        Uri uri = Uri.parse("geo:0,0").buildUpon()
//                .appendQueryParameter("q", location)
//                .build();
            Uri uri = Uri.parse("geo:" + Double.toString(lat) + "," + Double.toString(lon));
            intent.setData(uri);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Log.d(LOG_TAG, "Couldn't call " + cityName + ", no receiving apps installed!");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (mPosition != ListView.INVALID_POSITION) {
            savedInstanceState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    public void onLocationChanged() {
        updateWeather();
        // ...restart loader
        getLoaderManager().restartLoader(FORECAST_LOADER, null, this);
    }

    /*
     *  Updates the empty list view with contextually relevant information that the user can
     *  use to determine why they aren't seeing weather.
     */
    private void updateEmptyView() {
        if (mForecastAdapter.getCount() == 0) {
            TextView tv = (TextView) getView().findViewById(R.id.listview_empty_item);
            if (null != tv) {
                // if cursor is empty, why? do we have an invalid location?
                int message = R.string.listview_empty_item_label;
                @SunshineSyncAdapter.LocationStatus int location = Utility.getLocationStatus(getActivity());
                switch (location) {
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_DOWN:
                        message = R.string.listview_empty_item_server_down_label;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_SERVER_INVALID:
                        message = R.string.listview_empty_item_server_invalid_label;
                        break;
                    case SunshineSyncAdapter.LOCATION_STATUS_INVALID:
                        message = R.string.listview_empty_item_location_invalid_label;
                        break;
                    default:
                        if(!Utility.isNetworkAvailable(getActivity())) {
                            message = R.string.listview_empty_item_no_network_label;
                        }
                }
                tv.setText(message);
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());

        //Sort order: Ascending, by date.
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(locationSetting, System.currentTimeMillis());

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                FORECAST_COLUMNS,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mForecastAdapter.swapCursor(cursor);
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.smoothScrollToPosition(mPosition);
        }
        updateEmptyView();

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }



}
