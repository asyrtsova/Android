package ness.android.widget;

import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by administrator on 7/8/13.
 */
public class UpdateWidgetService extends Service {

    GPSTracker gps;
    String gpsStatus;
    double longitude;
    double latitude;

    String userAddress;

    String timeDay;
    int timeHour;
    int timeMinute;

    //tags used to parse JSON
    public static final String TAG_ENTITIES = "entities";
    public static final String TAG_NAME = "name";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_CITY = "city";
    public static final String TAG_STATE = "state";
    public static final String TAG_TYPES = "types";
    public static final String TAG_PRICE_LEVEL = "priceLevel";
    public static final String TAG_NESS_URI = "nessWebUri";


    public String url;
    JSONArray entities = null;

    private Intent mIntent;

    ArrayList<Entity> entityArray = new ArrayList<Entity>();
    String entityListString;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mIntent = intent;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Handler handler = new Handler();

                getGPSlocation();

                final DecimalFormat df = new DecimalFormat("#.000");

                url = "https://api-v3-p.trumpet.io/json-api/v3/search?rangeQuantity=&localtime=&rangeUnit=&maxResults=20&queryOptions=&queryString=&q=&price=&location=&sortBy=BEST&lat=" + df.format(latitude) + "&lon=" + df.format(longitude) + "&userRequested=true&quickrate=false&showPermClosed=false";

                getOnlineData();
                getUserAddress();
                getTime();

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

                            remoteViews.setTextViewText(R.id.text_body, entityListString);
                            remoteViews.setTextViewText(R.id.text_user_location, userAddress);
                            remoteViews.setTextViewText(R.id.text_time, timeDay + ", " + timeHour + ":" + timeMinute);

                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("https://likeness.com" + entityArray.get(0).nessUri));
                            PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), widgetId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            remoteViews.setOnClickPendingIntent(R.id.relative_layout, pendingIntent);

                            appWidgetManager.updateAppWidget(widgetId, remoteViews);
                        }

                    }
                });
                Looper.loop();
            }
        };
        new Thread(runnable).start();

        stopSelf();

        return Service.START_NOT_STICKY;
    }

    private void getTime() {
        Calendar cal = Calendar.getInstance();

        int timeDayNum = cal.get(Calendar.DAY_OF_WEEK);
        timeHour = cal.get(Calendar.HOUR);
        timeMinute = cal.get(Calendar.MINUTE);

        if(timeDayNum == 1){
            timeDay = "Sunday";
        } else if (timeDayNum==2){
            timeDay = "Monday";
        } else if (timeDayNum==3){
            timeDay = "Tuesday";
        } else if (timeDayNum==4){
            timeDay = "Wednesday";
        } else if (timeDayNum==5){
            timeDay = "Thursday";
        } else if (timeDayNum==6){
            timeDay = "Friday";
        } else if (timeDayNum==7){
            timeDay = "Saturday";
        }

    }

    private void getUserAddress() {

        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> userAddressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (userAddressList.size() > 0 && userAddressList.get(0).getAddressLine(1) != null) {
                userAddress = userAddressList.get(0).getAddressLine(1);
                userAddress = userAddress.substring(0, userAddress.length() - 6);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getGPSlocation() {

        gps = new GPSTracker(getApplicationContext());

        if (gps.canGetLocation()) {

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            gpsStatus = "GPS/network is enabled!";


        } else {
            gpsStatus = "GPS/network not enabled.";
            latitude = 37.4;
            longitude = -122.1;
        }
    }

    private void getOnlineData() {

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
                String uri = ent.getString(TAG_NESS_URI);
                JSONObject add = ent.getJSONObject(TAG_ADDRESS);
                String city = add.getString(TAG_CITY);
                String state = add.getString(TAG_STATE);
                JSONArray entityTypes = ent.getJSONArray(TAG_TYPES);
                String type = entityTypes.getString(0);

                //create Entity object with these variables and add it to an array
                Entity objEntity = new Entity(name, city + ", " + state, type, price, uri);
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



