package ness.android.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;


/**
 * Created by administrator on 7/8/13.
 */
public class UpdateWidgetService extends Service {

    private static final String LOG = "ness.android.widget";


    private final IBinder mBinder = new MyBinder();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(LOG, "Called");

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        ComponentName nessWidget = new ComponentName(getApplicationContext(), WidgetProvider.class);

        int[] allWidgetIds2 = appWidgetManager.getAppWidgetIds(nessWidget);
        Log.w(LOG, "From Intent" + String.valueOf(allWidgetIds.length));
        Log.w(LOG, "Direct" + String.valueOf(allWidgetIds2.length));

        for (int widgetId : allWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.widget_layout);


            remoteViews.setTextViewText(R.id.text_view, "testing service!");

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        return Service.START_NOT_STICKY;
    }

    public class MyBinder extends Binder {
        UpdateWidgetService getService() {
            return UpdateWidgetService.this;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


}
