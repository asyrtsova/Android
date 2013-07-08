package ness.android.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

/**
 * Created by administrator on 7/7/13.
 */
public class WidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
        RemoteViews remoteViews = new RemoteViews( context.getPackageName(), R.layout.widget_layout );
        ComponentName nessWidget = new ComponentName( context, WidgetProvider.class );

        int[] allWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);

        for (int widgetId: allWidgetIds) {
//            remoteViews.setTextViewText(R.id.text_view, "test");
            appWidgetManager.updateAppWidget(nessWidget, remoteViews);
        }

    }

}
