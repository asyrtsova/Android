package ness.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

/**
 * Created by administrator on 7/7/13.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static String REFRESH_BUTTON = "ness.android.widget.REFRESH_BUTTON";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

        //starts service to get data
        Intent intentStartService = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        intentStartService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
        context.startService(intentStartService);

        remoteViews.setOnClickPendingIntent(R.id.refresh_button, getPendingSelfIntent(context, REFRESH_BUTTON));

        appWidgetManager.updateAppWidget(nessWidget, remoteViews);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        super.onReceive(context, intent);

        //calls onUpdate method if refresh button is pressed
        if (REFRESH_BUTTON.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);

            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(nessWidget));

        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }


}
