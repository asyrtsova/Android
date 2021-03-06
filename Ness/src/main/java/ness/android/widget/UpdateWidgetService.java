package ness.android.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


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
    public boolean gpsStatusOn;
    public boolean networkStatusOn;
    public boolean refreshing;

    //tags used to parse JSON
    public static final String TAG_ENTITIES = "entities";
    public static final String TAG_NAME = "name";
    public static final String TAG_NESS_WEB_URI = "nessWebUri";
    public static final String TAG_COVERPHOTO = "coverPhoto";
    public static final String TAG_THUMBNAIL = "thumbnail";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_LATITUDE = "lat";
    public static final String TAG_LONGITUDE = "lon";
    public static final String TAG_TOP_MENU_ITEM = "topMenuItem";
    public static final String TAG_PHOTO = "photo";

    public String queryUrl;
    public JSONArray entities = null;
    public ArrayList<Entity> entityArray = new ArrayList<Entity>();

    public Intent mIntent;
    public Context mContext;
    public int mAppWidgetId;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        mIntent = intent;
    }

    public void onCreate() {
        System.err.println("SERVICE IS CREATED");
    }

    public RemoteViews getViewAt(int position) {

        RemoteViews remoteViewsItem = new RemoteViews(mContext.getPackageName(), R.layout.item_layout);

        if (position <= getCount() && entityArray.size() != 0) {
            Entity entity = entityArray.get(position);

            double distance = getDistanceFromEntity(entity);

            System.err.println("ENTITY DISH URL:" + entity.dishPhotoUrl + ", PHOTO URL: " + entity.photoUri);
            Bitmap bitmapImg = getBitmapFromURL(entity.dishPhotoUrl, entity.photoUri);
            Bitmap bitmapBlank = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.blank_image);

            DecimalFormat distanceFormat = new DecimalFormat("#0.0");

            remoteViewsItem.setTextViewText(R.id.text_dish, entity.topDish);
            remoteViewsItem.setTextViewText(R.id.text_entity, entity.name);
            remoteViewsItem.setTextViewText(R.id.text_distance, " | " + distanceFormat.format(distance) + " mi");

            if (bitmapImg != null) {
                remoteViewsItem.setImageViewBitmap(R.id.image_view, bitmapImg);
            } else {
                remoteViewsItem.setImageViewBitmap(R.id.image_view, bitmapBlank);
            }

            //Set fill-intent to fill in the pending intent template in WidgetProvider
            Bundle extras = new Bundle();
            extras.putString(WidgetProvider.URL_EXTRA, "https://likeness.com" + entity.nessUri);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            remoteViewsItem.setOnClickFillInIntent(R.id.item_layout, fillInIntent);

            if (entityArray.size() != 0)
                System.err.println("INSIDE GET VIEW AT for" + entityArray.get(position).name);

        }
        return remoteViewsItem;
    }

    public RemoteViews getLoadingView() {
        System.err.println("GETS LOADING VIEW");
        return new RemoteViews(mContext.getPackageName(), R.layout.loading_item_layout);
    }

    public void onDataSetChanged() {

        refreshing = true;

        if (Looper.myLooper() != Looper.getMainLooper()) {
            entityArray.clear();

            networkStatusOn = checkNetworkConnection();

            getGPSlocation();

            if (!gpsStatusOn) {
                //send intent to show "Please turn on location services."
                Intent intentChangeMainText = new Intent();
                intentChangeMainText.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                intentChangeMainText.setAction(WidgetProvider.SET_NO_LOCATION_TEXT);
                mContext.sendBroadcast(intentChangeMainText);
                refreshing = false;
            }

            if (gpsStatusOn && !networkStatusOn) {
                System.err.println("SEND NETWORK IS OFF INTENT");
                //send intent to show "No network connection available."
                Intent intentChangeMainText = new Intent();
                intentChangeMainText.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                intentChangeMainText.setAction(WidgetProvider.SET_NO_CONNECTION_TEXT);
                mContext.sendBroadcast(intentChangeMainText);
                refreshing = false;
            }

            if (gpsStatusOn && networkStatusOn) {
                getOnlineData();

                if (entityArray.size() == 0) {
                    //send intent to show "The list is empty."
                    Intent intentChangeMainText = new Intent();
                    intentChangeMainText.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    intentChangeMainText.setAction(WidgetProvider.SET_EMPTY_LIST_TEXT);
                    mContext.sendBroadcast(intentChangeMainText);
                    refreshing = false;
                }
            }

            System.err.println("ENTITY ARRAY SIZE:" + entityArray.size());
        }

        if (refreshing) {
            Intent stopRefreshIntent = new Intent();
            stopRefreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            stopRefreshIntent.setAction(WidgetProvider.STOP_REFRESH);
            mContext.sendBroadcast(stopRefreshIntent);
            refreshing = false;
        }

    }

    private boolean checkNetworkConnection() {

        ConnectivityManager conManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo i = conManager.getActiveNetworkInfo();
        if (i == null)
            return false;
        if (!i.isConnected())
            return false;
        if (!i.isAvailable())
            return false;
        return true;
    }

    public int getCount() {
        return entityArray.size();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDestroy() {
        System.err.println("SERVICE DESTROYED");
        entityArray.clear();
    }

    //parses Ness restful API to get entity data
    private void getOnlineData() {

        networkStatusOn = true;

        String urlTime = getTime();

        queryUrl = "https://api-v3-p.trumpet.io/json-api/v3/search?options=HIDE_CLOSED_PLACES&lat=" + latitude + "&sortBy=BEST&lon=" + longitude + "&category=restaurant&localtime=" + urlTime + "&userId=00000000-000e-c25d-c000-000000026810&magicScreen=MENU";

        System.err.println("QUERY URL:" + queryUrl);

        try {
            // getting JSON string from URL
            JSONObject json = JSONParser.getJSONFromUrl(queryUrl);
            System.err.println("PASSED QUERY");

            // Getting Array of Places
            entities = json.getJSONArray(TAG_ENTITIES);

            // looping through all entities
            System.err.println("ENTITIES LENGTH:" + entities.length());
            for (int i = 0; i < entities.length(); i++) {
                JSONObject ent = entities.getJSONObject(i);

                if (ent.has(TAG_TOP_MENU_ITEM)) {
                    // Storing each json item in variable

                    String name = ent.getString(TAG_NAME);

                    String uriWeb = ent.getString(TAG_NESS_WEB_URI);

                    JSONObject loc = ent.getJSONObject(TAG_LOCATION);
                    double entLat = loc.getDouble(TAG_LATITUDE);
                    double entLon = loc.getDouble(TAG_LONGITUDE);

                    JSONObject topItem = ent.getJSONObject(TAG_TOP_MENU_ITEM);
                    String topItemName = topItem.getString(TAG_NAME);
                    JSONObject topItemPhoto = topItem.getJSONObject(TAG_PHOTO);
                    String topItemImgUrl = null;
                    if (topItemPhoto.has(TAG_THUMBNAIL)) {
                        topItemImgUrl = topItemPhoto.getString(TAG_THUMBNAIL);
                    }

                    String urlImg = null;
                    if (ent.has(TAG_COVERPHOTO)) {
                        JSONObject coverphoto = ent.getJSONObject(TAG_COVERPHOTO);
                        urlImg = coverphoto.getString(TAG_THUMBNAIL);
                    }

                    //if entity has a photo, create Entity object with these variables and add it to an array
                    if (urlImg != null || topItemPhoto != null) {
                        Entity objEntity = new Entity(name, uriWeb, urlImg, topItemName, topItemImgUrl, entLat, entLon);
                        entityArray.add(objEntity);

                        System.err.println("ENT:" + objEntity.name);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    private double getDistanceFromEntity(Entity entity) {
        float[] distanceInMeters = new float[1];
        Location.distanceBetween(latitude, longitude, entity.latitude, entity.longitude, distanceInMeters);

        //get distance in miles from distance in meters
        return distanceInMeters[0] * 0.000621371;
    }

    private void getGPSlocation() {

        GPLocationServices gpLocationServices = new GPLocationServices(mContext);
        if (gpLocationServices.servicesAvailable) {
            try {
                gpLocationServices.dataAvailable.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            latitude = gpLocationServices.lat;
            longitude = gpLocationServices.lon;
            System.err.println("IN SERVICE : LAT: " + gpLocationServices.lat + " LON:" + gpLocationServices.lon);

            gpsStatusOn = true;

        } else if (!gpLocationServices.servicesAvailable) {

            gps = new GPSTracker(mContext);

            if (gps.canGetLocation()) {
                latitude = gps.getLatitude();
                longitude = gps.getLongitude();

                gpsStatusOn = true;
            }

        } else {
            gpsStatusOn = false;
        }

        //for emulator use
//        gpsStatusOn = true;
//        latitude = 40.766;
//        longitude = -73.975;

    }


    //returns time, formatted to insert into url
    private String getTime() {
        Date date = new Date();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        String formattedDate = dateFormat.format(date);

        try {
            return URLEncoder.encode(formattedDate, "utf-8");
        } catch (Exception e) {
            return "2013-08-06T15%3A24-0700";
        }
    }

    public static Bitmap getBitmapFromURL(String src, String backupSrc) {
        try {
            //gets dish photo
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();

            return BitmapFactory.decodeStream(input);

        } catch (IOException e) {
            e.printStackTrace();

            //use entity photo if dish photo is null
            try {
                URL url = new URL(backupSrc);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                return BitmapFactory.decodeStream(input);

            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }

        }
    }


}



