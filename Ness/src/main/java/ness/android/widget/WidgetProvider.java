package ness.android.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;

/**
 * Created by administrator on 7/7/13.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static final String REFRESH_ACTION = "ness.android.widget.REFRESH_ACTION";
    public static final String OPEN_BROWSER = "ness.android.widget.OPEN_BROWSER";
    public static final String AUTO_UPDATE = "ness.android.widget.AUTO_UPDATE";

    public static final String STOP_REFRESH = "ness.android.widget.STOP_REFRESH";
    public static final String SET_EMPTY_LIST_TEXT = "ness.android.widget.SET_EMPTY_LIST_TEXT";
    public static final String SET_NO_LOCATION_TEXT = "ness.android.widget.SET_NO_LOCATION_TEXT";

    public static final String URL_EXTRA = "ness.android.widget.URL_EXTRA";

    private final int ALARM_ID = 0;
    private final int INTERVAL_MILLIS = 600000; // auto update every 10 min

    public RemoteViews remoteViews;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        System.err.println("APPWIDGETPROVIDER ONUPDATE");

        //initialize remoteViews in case onUpdate is called before onReceive
        if(remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        }

        for (int i = 0; i < appWidgetIds.length; ++i) {

            Intent serviceIntent = new Intent(context, UpdateWidgetService.class);

            //add app widget ID to the intent extras
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);

            //embed extras
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            //set up the RemoteViews object to use a RemoteViews adapter, which connects to
            // a RemoveViewsService through the specified intent. This populates the data.
            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, serviceIntent);

            //sets an empty view to be displayed when the collection has no items
            remoteViews.setEmptyView(R.id.stack_view, R.id.empty_layout);

            //sets up pending intent template, allowing individualized behavior for each item (open browser to entity's specific page)
            Intent intentSetUris = new Intent(context, WidgetProvider.class);
            intentSetUris.setAction(WidgetProvider.OPEN_BROWSER);
            intentSetUris.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intentSetUris.setData(Uri.parse(intentSetUris.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent browserPendingIntent = PendingIntent.getBroadcast(context, 0, intentSetUris, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.stack_view, browserPendingIntent);

            //sets up refresh button
            Intent refreshIntent = new Intent(context, WidgetProvider.class);
            refreshIntent.setAction(WidgetProvider.REFRESH_ACTION);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent);

            //updates widget
            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        System.err.println("AppWidget onRecieve: " + intent.getAction());

        //initialize remoteViews in case onReceive is called before onUpdate
        if(remoteViews == null) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

        //updates widget if refresh button is pressed or alarm manager triggered
        if (intent.getAction().equals(REFRESH_ACTION) || intent.getAction().equals(AUTO_UPDATE)) {

            for (int i = 0; i < appWidgetIds.length; ++i) {

                remoteViews.setViewVisibility(R.id.refresh_button, View.INVISIBLE);
                remoteViews.setViewVisibility(R.id.progress_bar, View.VISIBLE);

                appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

            }
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view);

        }

        //opens browser to Ness entity page if item view is clicked
        if (intent.getAction().equals(OPEN_BROWSER)) {

            String uri = intent.getStringExtra(URL_EXTRA);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse(uri));
            context.startActivity(browserIntent);

        }

        //stops progress bar; refresh button reappears
        if (intent.getAction().equals(STOP_REFRESH)) {

            System.err.println("REFRESHING STOPPED");
            for (int i = 0; i < appWidgetIds.length; ++i) {


                remoteViews.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.progress_bar, View.INVISIBLE);

                appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
            }
        }

        //sets text in case of empty list
        if (intent.getAction().equals(SET_EMPTY_LIST_TEXT)) {

            System.err.println("LIST IS EMPTY ACTION");

            for (int i = 0; i < appWidgetIds.length; ++i) {

                remoteViews.setTextViewText(R.id.widget_view_text, "The list is empty.");
            remoteViews.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.progress_bar, View.INVISIBLE);

            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
            }
        }

        //sets text in case no location services are available
        if (intent.getAction().equals(SET_NO_LOCATION_TEXT)) {

            System.err.println("REFRESHING STOPPED");

            for (int i = 0; i < appWidgetIds.length; ++i) {

                remoteViews.setTextViewText(R.id.widget_view_text, "Please turn on location services.");
            remoteViews.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            remoteViews.setViewVisibility(R.id.progress_bar, View.INVISIBLE);

            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
            }
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        System.err.println("APPWIDGETPROVIDER ENABLED");
        startAlarm(context);
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

        //only stop alarm if last widget is disabled
        if (appWidgetIds.length <= 1) {
            stopAlarm(context);
        }

        super.onDisabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    public void stopAlarm(Context context)
    {
        Intent alarmIntent = new Intent(WidgetProvider.AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        System.err.println("ALARM STOPPED.");

    }


    public void startAlarm(Context context)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MILLISECOND, INTERVAL_MILLIS);

        Intent alarmIntent = new Intent(WidgetProvider.AUTO_UPDATE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, ALARM_ID, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // RTC does not wake the device up to perform update
        alarmManager.setInexactRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), INTERVAL_MILLIS, pendingIntent);

        System.err.println("ALARM STARTED");

    }

    public int[] getWidgetIds (Context context, AppWidgetManager appWidgetManager){
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);
        return appWidgetManager.getAppWidgetIds(nessWidget);
    }

}
