package ness.android.widget;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class onRestartReciever extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        System.err.println("ON RESTART RECIEVER");

        // reInitialize appWidgets
        AppWidgetManager appWidgetManager=AppWidgetManager.getInstance(context);

        WidgetProvider widget =new WidgetProvider();
        widget.onUpdate (context, AppWidgetManager.getInstance(context), widget.getWidgetIds(context, appWidgetManager));

    }
}