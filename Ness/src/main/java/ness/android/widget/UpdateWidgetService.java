package ness.android.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by administrator on 7/8/13.
 */
public class UpdateWidgetService extends Service {

    GPSTracker gps;
    String gpsStatus;
    double longitude;
    double latitude;

    public static final String TAG_ENTITY_NAME = "entity.name";
    public static final String TAG_ADDRESS = "entity.address";


    public static String url = "https://googledrive.com/host/0B-QhweyqDtobQ2o1ZHpNS1BpLUU/recommendations.json";
    JSONArray entities = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this
                .getApplicationContext());

        int[] allWidgetIds = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

        ComponentName nessWidget = new ComponentName(getApplicationContext(), WidgetProvider.class);

        getGPSlocation();

        getOnlineData();


        for (int widgetId : allWidgetIds) {


            RemoteViews remoteViews = new RemoteViews(this
                    .getApplicationContext().getPackageName(),
                    R.layout.widget_layout);


            remoteViews.setTextViewText(R.id.text_view, gpsStatus + "\nYour Location is - \nLat: " + latitude + "\nLong: " + longitude);

            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
        stopSelf();

        return Service.START_NOT_STICKY;
    }

    private void getOnlineData() {

        //Creating JSON Parser instance
        JSONParser jParser = new JSONParser();

        // getting JSON string from URL
        JSONObject json;// = jParser.getJSONFromUrl(url);

//        try {
//            // Getting Array of Places
//            entities = json.getJSONArray(TAG_ENTITY_NAME);
//
//            // looping through All Contacts
//            for(int i = 0; i < entities.length(); i++){
//                JSONObject c = entities.getJSONObject(i);
//
//                // Storing each json item in variable
//                String name = c.getString(TAG_ENTITY_NAME);
//                String address = c.getString(TAG_ADDRESS);
//
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
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



