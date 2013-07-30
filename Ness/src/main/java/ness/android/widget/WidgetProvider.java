package ness.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

/**
 * Created by administrator on 7/7/13.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static String REFRESH_ACTION = "ness.android.widget.REFRESH_ACTION";
    public static String OPEN_BROWSER = "ness.android.widget.OPEN_BROWSER";
    public static String EXTRA_ITEM = "ness.android.widget.EXTRA_ITEM";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        remoteViews.setViewVisibility(R.id.refresh_button, View.INVISIBLE);
        remoteViews.setViewVisibility(R.id.progress_bar, View.VISIBLE);

        //sets up refresh button
        Intent refreshIntent = new Intent(context, WidgetProvider.class);
        refreshIntent.setAction(WidgetProvider.REFRESH_ACTION);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent);


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
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

        //calls onUpdate method if refresh button is pressed
        if (intent.getAction().equals(REFRESH_ACTION)) {

            appWidgetManager.updateAppWidget(appWidgetIds, null);

            for (int i = 0; i < appWidgetIds.length; ++i) {

                System.err.println("REFRESH ACTION IS IDENTIFIED");

                appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

            }

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
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }


}
