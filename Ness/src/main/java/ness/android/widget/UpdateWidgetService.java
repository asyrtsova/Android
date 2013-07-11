package ness.android.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.LinearLayout;
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

    String gpsInfo;
    String entityNames;


    public static String url = "https://api-v3-p.trumpet.io/json-api/v3/search?rangeQuantity=&localtime=&rangeUnit=&maxResults=20&queryOptions=&queryString=&q=&price=&location=&sortBy=BEST&lat=37.405&userRequested=true&lon=-122.119&quickrate=false&showPermClosed=true";
    JSONArray entities = null;

    private Handler handler = new Handler();
    private Intent mIntent;

    LinearLayout layout;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mIntent = intent;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                    getGPSlocation();
//                    getOnlineData();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

                            int[] allWidgetIds = mIntent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

                            for (int widgetId : allWidgetIds) {


                                RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                                        R.layout.widget_layout);


                                remoteViews.setTextViewText(R.id.text_view, "test handler and thread");

                                appWidgetManager.updateAppWidget(widgetId, remoteViews);
                            }

                        }
                    });
            }
        };
        new Thread(runnable).start();

        stopSelf();

        return Service.START_NOT_STICKY;
    }

    private void getGPSlocation() {

        gps = new GPSTracker(getApplicationContext());

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            gpsStatus = "GPS/network is enabled!";


        } else {
            gpsStatus = "GPS/network not enabled.";
        }
    }

    private void getOnlineData() {

        //Creating JSON Parser instance
        JSONParser jParser = new JSONParser();

        // getting JSON string from URL
        JSONObject json = jParser.getJSONFromUrl(url);

        try {
            // Getting Array of Places
            entities = json.getJSONArray(TAG_ENTITY_NAME);

            // looping through All Contacts
            for (int i = 0; i < entities.length(); i++) {
                JSONObject c = entities.getJSONObject(i);

                // Storing each json item in variable
                String name = c.getString(TAG_ENTITY_NAME);
                String address = c.getString(TAG_ADDRESS);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}



