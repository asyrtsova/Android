package ness.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; ++i) {

            Intent intentStartService = new Intent(context, UpdateWidgetService.class);

            //add app widget ID to the intent extras
            intentStartService.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intentStartService.setData(Uri.parse(intentStartService.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

            //set up the RemoteViews object to use a RemoteViews adapter, which connects to
            // a RemoveViewsService through the specified intent. This populates the data.
            remoteViews.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intentStartService);

            //sets an empty view to be displayed when the collection has no items
            remoteViews.setEmptyView(R.id.stack_view, R.id.empty_view);

            appWidgetManager.updateAppWidget(appWidgetIds[i], remoteViews);

        }

    }

    @Override
    public void onReceive(Context context, Intent intent) {

        //calls onUpdate method if refresh button is pressed
        if (REFRESH_BUTTON.equals(intent.getAction())) {

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);

            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(nessWidget));

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
