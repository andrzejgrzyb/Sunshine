package pl.grzyb.andrzej.sunshine.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import pl.grzyb.andrzej.sunshine.app.sync.SunshineSyncAdapter;

/**
 * Created by andrzej on 23.08.15.
 */
public class TodayWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, TodayWidgetIntentService.class));
    }
  //  @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (SunshineSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, TodayWidgetIntentService.class));
        }
/*

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                R.layout.widget_today_small);

        // Update text, images, whatever - here
        remoteViews.setTextViewText(R.id.widget_temperature, "My updated text");

        // Trigger widget layout update
        AppWidgetManager.getInstance(context).updateAppWidget(
                new ComponentName(context, TodayWidgetProvider.class), remoteViews);
*/

    }

}

