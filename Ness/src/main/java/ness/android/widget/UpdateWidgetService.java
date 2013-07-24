package ness.android.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
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
import java.util.Random;


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
    public double longitude;
    public double latitude;
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
    public static final String TAG_TYPES = "types";
    public static final String TAG_PRICE_LEVEL = "priceLevel";
    public static final String TAG_NESS_URI = "nessWebUri";
    public static final String TAG_COVERPHOTO = "coverPhoto";
    public static final String TAG_PHOTO_URL = "url";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_LATITUDE = "lat";
    public static final String TAG_LONGITUDE = "lon";


    public String url;
    public JSONArray entities = null;

    public Intent mIntent;

    public ArrayList<Entity> entityArray = new ArrayList<Entity>();

    public Context mContext;
    public int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        mIntent = intent;
    }

    public void onCreate() {


    }

    public RemoteViews getViewAt(int position) {

        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_layout);
        Entity entity = entityArray.get(position);

        if (position <= getCount()) {

            double distance = getDistanceFromEntity(entity);

            Bitmap imgBitmap = getBitmapFromURL(entity.photoUri);

            DecimalFormat timeFormat = new DecimalFormat("00");
            DecimalFormat distanceFormat = new DecimalFormat("#0.0");

            String prefix = prefixGenerator();

            remoteViews.setTextViewText(R.id.text_title, entity.name);
            remoteViews.setTextViewText(R.id.text_entity_info, prefix + entity.address + " | " + distanceFormat.format(distance) + " mi");
            remoteViews.setTextViewText(R.id.text_user_location, userAddress);
            remoteViews.setTextViewText(R.id.text_time, defineMealtime());
            remoteViews.setTextViewText(R.id.text_debug_refresh, timeHour + ":" + timeFormat.format(timeMinute));

            remoteViews.setImageViewBitmap(R.id.image_view, imgBitmap);

            //Set fill-intent to fill in the pending intent template in WidgetProvider
            Bundle extras = new Bundle();
            extras.putString(WidgetProvider.EXTRA_ITEM, "https://likeness.com" + entity.nessUri);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            remoteViews.setOnClickFillInIntent(R.id.item_layout, fillInIntent);

            //Set fill-intent for refresh button
            Intent refreshIntent = new Intent();
            remoteViews.setOnClickFillInIntent(R.id.refresh_button, refreshIntent);

            System.err.println("INSIDE GET VIEW AT for" + entity.name);

        }

        return remoteViews;
    }

    private String prefixGenerator() {
        String[] prefixes = {"Head to: ", "Try: ", "Eat at: ", "Go to: ", "Don't miss: ", "People love: ", "What's hot: ", "Grab a bite: ", "Top pick: "};
        Random rand = new Random();
        int choice = rand.nextInt(prefixes.length);
        return prefixes[choice];
    }

    private String defineMealtime() {
        String mealtime = "Mealtime";
        if (0 <= timeHour && timeHour < 5) {
            mealtime = "Late Night";
        } else if (5 <= timeHour && timeHour <= 10) {
            if (timeHour == 10 && timeMinute < 30) {
                mealtime = "Breakfast";
            } else if (timeHour == 10 && timeMinute >= 30) {
                mealtime = "Lunch";
            }
        } else if (11 <= timeHour && timeHour < 14) {
            mealtime = "Lunch";
        } else if (14 <= timeHour && timeHour < 16) {
            mealtime = "Snack";
        } else if (16 <= timeHour) {
            mealtime = "Dinner";
        }
            return mealtime;
}

    private double getDistanceFromEntity(Entity entity) {
        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude, entity.latitude, entity.longitude, results);
        //get distance in miles from distance in meters
        double distanceInMiles = results[0] * 0.000621371;
        return distanceInMiles;
    }


    public void onDestroy() {
        entityArray.clear();
    }

    public int getCount() {
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
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getGPSlocation();
            getOnlineData();
            getUserAddress();
            getTime();
        }
    }


    private void getTime() {
        Calendar cal = Calendar.getInstance();

        int timeDayNum = cal.get(Calendar.DAY_OF_WEEK);
        timeHour = cal.get(Calendar.HOUR_OF_DAY);
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

        } else {

            gpsStatus = "GPS/network not enabled.";
        }

        if (latitude < 0.01) {
            latitude = 40.766;
            longitude = -73.975;
            ;
        }

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

                JSONObject loc = ent.getJSONObject(TAG_LOCATION);
                double entLat = loc.getDouble(TAG_LATITUDE);
                double entLon = loc.getDouble(TAG_LONGITUDE);

                JSONArray entityTypes = ent.getJSONArray(TAG_TYPES);
                String type = entityTypes.getString(0);

                String urlImg = null;
                Bitmap imgBitmap = null;
                if (ent.has(TAG_COVERPHOTO)) {
                    JSONObject coverphoto = ent.getJSONObject(TAG_COVERPHOTO);
                    urlImg = coverphoto.getString(TAG_PHOTO_URL);

                    //create Entity object with these variables and add it to an array
                    Entity objEntity = new Entity(name, city, type, price, uriWeb, urlImg, entLat, entLon);
                    entityArray.add(objEntity);
                }
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



