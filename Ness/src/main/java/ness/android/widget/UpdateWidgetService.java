package ness.android.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


/**
 * Created by administrator on 7/8/13.
 */
public class UpdateWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    public GPSTracker gps;
    public double longitude = -122;
    public double latitude = 37;
    public String gpsStatus;

    public String userAddress;

    public String timeDay;
    public int timeHour;
    public int timeMinute;

    //tags used to parse JSON
    public static final String TAG_ENTITIES = "entities";
    public static final String TAG_NAME = "name";
    public static final String TAG_ADDRESS = "address";
    public static final String TAG_CITY = "city";
    public static final String TAG_STATE = "state";
    public static final String TAG_TYPES = "types";
    public static final String TAG_PRICE_LEVEL = "priceLevel";
    public static final String TAG_NESS_URI = "nessWebUri";
    public static final String TAG_COVERPHOTO = "coverPhoto";
    public static final String TAG_PHOTO_URL = "url";


    public String url;
    public JSONArray entities = null;

    public Intent mIntent;

    public ArrayList<Entity> entityArray = new ArrayList<Entity>();

    public Context mContext;
    public int mAppWidgetId;

    int debug = 0;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        mIntent = intent;
    }

    public void onCreate() {
    }

    public RemoteViews getViewAt(int position) {

        System.err.println("INSIDE GETVIEWAT!!!" + position);

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_layout);

        if (position <= getCount()) {
            Entity entity = entityArray.get(position);

            DecimalFormat df = new DecimalFormat("00");

            remoteViews.setTextViewText(R.id.text_body, entity.name);
            remoteViews.setTextViewText(R.id.text_user_location, userAddress);
            remoteViews.setTextViewText(R.id.text_time, timeDay + ", " + timeHour + ":" + df.format(timeMinute));
            Bitmap bitmap = entity.photoBitmap;

            remoteViews.setImageViewBitmap(R.id.image_view, bitmap);

//            Intent intentSetUris = new Intent(Intent.ACTION_VIEW);
//            intentSetUris.setData(Uri.parse("https://likeness.com" + entity.nessUri));
//            remoteViews.setOnClickFillInIntent(R.id.item_layout, intentSetUris);

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);

            appWidgetManager.updateAppWidget(mAppWidgetId, remoteViews);

        }

        return remoteViews;
    }


    public void onDestroy() {
        entityArray.clear();
    }

    public int getCount() {
        System.err.println("size=" + entityArray.size());
        return entityArray.size();
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {

//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                Handler handler = new Handler();
//
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {

        getGPSlocation();
        getOnlineData();
        getUserAddress();
        getTime();

        System.err.println("INSIDE datasetchanged");

//                    }
//                });
//                Looper.loop();
//            }
//        };
//        new Thread(runnable).start();

    }

    private void getTime() {
        Calendar cal = Calendar.getInstance();

        int timeDayNum = cal.get(Calendar.DAY_OF_WEEK);
        timeHour = cal.get(Calendar.HOUR);
        timeMinute = cal.get(Calendar.MINUTE);

        if (timeHour == 0) {
            timeHour = 12;
        }

        if (timeDayNum == 1) {
            timeDay = "Sun";
        } else if (timeDayNum == 2) {
            timeDay = "Mon";
        } else if (timeDayNum == 3) {
            timeDay = "Tues";
        } else if (timeDayNum == 4) {
            timeDay = "Wed";
        } else if (timeDayNum == 5) {
            timeDay = "Thurs";
        } else if (timeDayNum == 6) {
            timeDay = "Fri";
        } else if (timeDayNum == 7) {
            timeDay = "Sat";
        }

    }

    private void getUserAddress() {

        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
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

        gps = new GPSTracker(mContext);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            gpsStatus = "GPS/network is enabled!";

        }

        if (latitude < 0.1) {
            latitude = 37.4;
            longitude = -122.1;

            gpsStatus = "GPS/network not enabled.";
        }

        System.err.println("lat:" + latitude + ", long:" + longitude + gpsStatus);
    }

    private void getOnlineData() {

        final DecimalFormat df = new DecimalFormat("#.000");

        url = "https://api-v3-p.trumpet.io/json-api/v3/search?rangeQuantity=&localtime=&rangeUnit=&maxResults=20&queryOptions=&queryString=&q=&price=&location=&sortBy=BEST&lat=" + df.format(latitude) + "&lon=" + df.format(longitude) + "&userRequested=true&quickrate=false&showPermClosed=false";

        //Creating JSON Parser instance
        JSONParser jParser = new JSONParser();

        try {
            // getting JSON string from URL
            JSONObject json = jParser.getJSONFromUrl(url);
            // Getting Array of Places
            entities = json.getJSONArray(TAG_ENTITIES);

            // looping through all entities
            for (int i = 0; i < entities.length(); i++) {
                JSONObject ent = entities.getJSONObject(i);

                // Storing each json item in variable
                String name = ent.getString(TAG_NAME);

                String price = ent.has(TAG_PRICE_LEVEL) ? ent.getString(TAG_PRICE_LEVEL) : "-1";

                String uriWeb = ent.getString(TAG_NESS_URI);

                JSONObject add = ent.getJSONObject(TAG_ADDRESS);
                String city = add.getString(TAG_CITY);
                String state = add.getString(TAG_STATE);

                JSONArray entityTypes = ent.getJSONArray(TAG_TYPES);
                String type = entityTypes.getString(0);

                Bitmap imgBitmap = null;
                if (ent.has(TAG_COVERPHOTO)) {
                    JSONObject coverphoto = ent.getJSONObject(TAG_COVERPHOTO);
                    String urlImg = coverphoto.getString(TAG_PHOTO_URL);

                    imgBitmap = getBitmapFromURL(urlImg);
                }

                //create Entity object with these variables and add it to an array
                Entity objEntity = new Entity(name, city + ", " + state, type, price, uriWeb, imgBitmap);
                entityArray.add(objEntity);

                System.err.println("INSIDE GETONLINE DATA");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}



