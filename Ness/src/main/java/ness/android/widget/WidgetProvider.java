package ness.android.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

/**
 * Created by administrator on 7/7/13.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static final String REFRESH_ACTION = "ness.android.widget.REFRESH_ACTION";
    public static final String OPEN_BROWSER = "ness.android.widget.OPEN_BROWSER";
    public static final String EXTRA_ITEM = "ness.android.widget.EXTRA_ITEM";
    public static final String AUTO_UPDATE = "AUTO_UPDATE";

    private final int ALARM_ID = 0;
    private final int INTERVAL_MILLIS = 120000;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        for (int i = 0; i < appWidgetIds.length; ++i) {

            System.err.println("INSIDE PROVIDER ONUPDATE FORLOOP");

            Intent serviceIntent = new Intent(context, UpdateWidgetService.class);
            context.startService(serviceIntent);

            //add app widget ID to the intent extras
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            //embed extras
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            //set up the RemoteViews object to use a RemoteViews adapter, which connects to
            // a RemoveViewsService through the specified intent. This populates the data.
            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, serviceIntent);

            //sets an empty view to be displayed when the collection has no items
            remoteViews.setEmptyView(R.id.stack_view, R.id.empty_layout);

            //sets up pending intent template, allowing individualized behavior for each item
            Intent intentSetUris = new Intent(context, WidgetProvider.class);
            intentSetUris.setAction(WidgetProvider.OPEN_BROWSER);
            intentSetUris.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intentSetUris.setData(Uri.parse(intentSetUris.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent browserPendingIntent = PendingIntent.getBroadcast(context, 0, intentSetUris, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.stack_view, browserPendingIntent);

            //updates widget
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.err.println("onRecieve:" + intent.getAction());
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

        //calls onUpdate method if refresh button is pressed
        if (intent.getAction().equals(REFRESH_ACTION) || intent.getAction().equals(AUTO_UPDATE)) {

            for (int i = 0; i < appWidgetIds.length; ++i) {

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

                System.err.println("REFRESH ACTION IS IDENTIFIED");

                remoteViews.setViewVisibility(R.id.refresh_button, View.INVISIBLE);
                remoteViews.setViewVisibility(R.id.progress_bar, View.VISIBLE);

                appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

            }

            startAlarm(context);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view);

            onUpdate(context, appWidgetManager, appWidgetIds);

        }

        //opens browser if widget item is clicked
        if (intent.getAction().equals(OPEN_BROWSER)) {

            String uri = intent.getStringExtra(EXTRA_ITEM);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(uri));
            context.startActivity(browserIntent);

        }

        super.onReceive(context, intent);
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        // TODO: alarm should be stopped only if all widgets has been disabled

        stopAlarm(context);
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        startAlarm(context);
        super.onEnabled(context);
    }

    public void stopAlarm(Context context)
    {
        Intent alarmIntent = new Intent(WidgetProvider.AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }


    public void startAlarm(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, INTERVAL_MILLIS);

        Intent alarmIntent = new Intent(WidgetProvider.AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // RTC does not wake the device up
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), INTERVAL_MILLIS, pendingIntent);
    }


}
