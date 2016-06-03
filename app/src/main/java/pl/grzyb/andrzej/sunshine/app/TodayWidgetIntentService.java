package pl.grzyb.andrzej.sunshine.app;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import pl.grzyb.andrzej.sunshine.app.data.WeatherContract;

/**
 * Created by andrzej on 23.08.15.
 */
public class TodayWidgetIntentService extends IntentService {

    private static final String[] FORECAST_COLUMNS = {
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
    };

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int INDEX_WEATHER_ID = 0;
    static final int INDEX_SHORT_DESC = 1;
    static final int INDEX_MAX_TEMP = 2;

    public TodayWidgetIntentService() {
        super("TodayWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, TodayWidgetProvider.class));

        //Get today's data from ContentProvider
        String location = Utility.getPreferredLocation(this);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(location, System.currentTimeMillis());
        Cursor cursor = getContentResolver().query(weatherForLocationUri, FORECAST_COLUMNS, null, null, WeatherContract.WeatherEntry.COLUMN_DATE + " ASC");
        if (cursor == null) {
            return;
        }
        if(!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        // Extract the weather data from the Cursor
        int weatherId = cursor.getInt(INDEX_WEATHER_ID);
        int weatherArtResourceId = Utility.getArtResourceForWeatherCondition(weatherId);
        String description = cursor.getString(INDEX_SHORT_DESC);
        double maxTemp = cursor.getDouble(INDEX_MAX_TEMP);
        String formattedMaxTemperature = Utility.formatTemperature(this, maxTemp);
        cursor.close();

        // for every created Widget
        for (int appWidgetId : appWidgetIds) {
            // add RemoteView
            RemoteViews views = new RemoteViews(
                    getPackageName(),
                    R.layout.widget_today_small);
            // Add the data to the RemoteViews
            views.setImageViewResource(R.id.widget_icon, weatherArtResourceId);
            views.setTextViewText(R.id.widget_temperature, formattedMaxTemperature);
            // Content Descriptions for RemoteViews were only added in ICS MR1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                setRemoteContentDescription(views, description);
            }
            // add action when user taps on the Widget - Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void setRemoteContentDescription(RemoteViews views, String description) {
        views.setContentDescription(R.id.widget_icon, description);
    }




}
