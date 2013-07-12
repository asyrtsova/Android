package ness.android.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;


/**
 * Created by administrator on 7/8/13.
 */
public class UpdateWidgetService extends Service {

    GPSTracker gps;
    String gpsStatus;
    double longitude;
    double latitude;

    //tags used to parse JSON
    public static final String TAG_ENTITIES = "entities";
    public static final String TAG_NAME = "name";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_TYPES = "types";
    public static final String TAG_PRICE_LEVEL = "priceLevel";


    public static String url = "https://api-v3-p.trumpet.io/json-api/v3/search?rangeQuantity=&localtime=&rangeUnit=&maxResults=20&queryOptions=&queryString=&q=&price=&location=&sortBy=BEST&lat=37.405&userRequested=true&lon=-122.119&quickrate=false&showPermClosed=true";
    JSONArray entities = null;

    private Handler handler = new Handler();
    private Intent mIntent;

    ArrayList<Entity> entityArray = new ArrayList<Entity>();
    String entityListString;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mIntent = intent;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
//              getGPSlocation();
                getOnlineData();

                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < entityArray.size(); i++) {
                    sb.append(entityArray.get(i).toString()).append("\n");
                }
                entityListString = sb.toString();
                handler.post(new Runnable() {
                    @Override
                    public void run() {

                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

                        int[] allWidgetIds = mIntent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

                        for (int widgetId : allWidgetIds) {


                            RemoteViews remoteViews = new RemoteViews(getApplicationContext().getPackageName(),
                                    R.layout.widget_layout);

                            remoteViews.setTextViewText(R.id.text_view, entityListString);

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

//        entityNames.add("Kambucha!");

        //Creating JSON Parser instance
        JSONParser jParser = new JSONParser();

        // getting JSON string from URL
        JSONObject json = jParser.getJSONFromUrl(url);

        try {
            // Getting Array of Places
            entities = json.getJSONArray(TAG_ENTITIES);

            // looping through all entities
            for (int i = 0; i < entities.length(); i++) {
                JSONObject ent = entities.getJSONObject(i);

                // Storing each json item in variable
                String name = ent.getString(TAG_NAME);
                String price = ent.getString(TAG_PRICE_LEVEL);
                JSONObject add = ent.getJSONObject(TAG_ADDRESS);
                String address = add.getString(TAG_ADDRESS);
                JSONArray entityTypes = ent.getJSONArray(TAG_TYPES);
                String type = entityTypes.getString(0);

                //create Entity object with these variables and add it to an array
                Entity objEntity = new Entity(name, address, type, price);
                entityArray.add(objEntity);
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



