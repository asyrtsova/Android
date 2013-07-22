package ness.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by administrator on 7/7/13.
 */
public class WidgetProvider extends AppWidgetProvider {

    public static String REFRESH_BUTTON = "ness.android.widget.REFRESH_BUTTON";
    public static String OPEN_BROWSER = "ness.android.widget.OPEN_BROWSER";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; ++i) {

            Intent serviceIntent = new Intent(context, UpdateWidgetService.class);

            //add app widget ID to the intent extras
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            //embed extras
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //set up the RemoteViews object to use a RemoteViews adapter, which connects to
            // a RemoveViewsService through the specified intent. This populates the data.
            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, serviceIntent);

            //sets an empty view to be displayed when the collection has no items
            remoteViews.setEmptyView(R.id.stack_view, R.id.empty_view);

            //sets up pending intent template, allowing individualized behavior for each item
            Intent intentSetUris = new Intent(Intent.ACTION_VIEW);
            intentSetUris.setAction(WidgetProvider.OPEN_BROWSER);
            intentSetUris.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetIds[i]);
            intentSetUris.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

            PendingIntent browserPendingIntent = PendingIntent.getBroadcast(context,0,intentSetUris, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setPendingIntentTemplate(R.id.stack_view, browserPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.id.item_layout);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        //calls onUpdate method if refresh button is pressed
        if (intent.getAction().equals(REFRESH_BUTTON)) {

            ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);

            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(nessWidget));

        }

        //opens browser if widget item is clicked
        if(intent.getAction().equals(OPEN_BROWSER)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String url = intent.getStringExtra(OPEN_BROWSER);
            System.err.println("URL IN ONRECIEVE:" + url);
            intent.setData(Uri.parse(url));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.stack_view, pendingIntent);

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
