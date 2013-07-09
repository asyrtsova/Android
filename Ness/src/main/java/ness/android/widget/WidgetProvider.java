package ness.android.widget;

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

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        ComponentName nessWidget = new ComponentName(context, WidgetProvider.class);

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

//        remoteViews.setTextViewText(R.id.text_view, "widget testing");
        Intent intent = new Intent(context.getApplicationContext(), UpdateWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
//        appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);

        context.startService(intent);


    }


}
