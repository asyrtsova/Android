package ness.android.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;


/**
 * Created by administrator on 7/8/13.
 */
public class UpdateWidgetService extends Service {

    GPSTracker gps;
    String gpsStatus;
    double longitude;
    double latitude;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        ComponentName nessWidget = new ComponentName(getApplicationContext(), WidgetProvider.class);

        for (int widgetId : allWidgetIds) {

            getGPSlocation();

            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.widget_layout);


            remoteViews.setTextViewText(R.id.text_view, gpsStatus + "\nYour Location is - \nLat: " + latitude + "\nLong: " + longitude);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        return Service.START_NOT_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private void getGPSlocation() {

        gps = new GPSTracker(getApplicationContext());

        if(gps.canGetLocation()){

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            gpsStatus = "GPS/network is enabled!";



        }else {
            gpsStatus = "GPS/network not enabled.";
        }
    }



}
