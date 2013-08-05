package ness.android.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
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
    public double longitude;
    public double latitude;
    public boolean gpsStatusOn;

    public String userAddress;

    //tags used to parse JSON
    public static final String TAG_ENTITIES = "entities";
    public static final String TAG_NAME = "name";
    public static final String TAG_NESS_URI = "nessWebUri";
    public static final String TAG_COVERPHOTO = "coverPhoto";
    public static final String TAG_THUMBNAIL_URL = "thumbnail";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_LATITUDE = "lat";
    public static final String TAG_LONGITUDE = "lon";
    public static final String TAG_TOP_MENU_ITEM = "topMenuItem";
    public static final String TAG_TOP_MENU_ITEM_PHOTO = "photo";


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
        System.err.println("SERVICE IS CREATED");
    }

    public RemoteViews getViewAt(int position) {

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName nessWidget = new ComponentName(mContext, WidgetProvider.class);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);
        RemoteViews remoteViewWidget = WidgetProvider.remoteViews;

        Intent serviceIntent = new Intent(mContext, UpdateWidgetService.class);

        //embed extras
        serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));

        //sets an empty view to be displayed when the collection has no items
        remoteViewWidget.setEmptyView(R.id.stack_view, R.id.empty_layout);

        //sets up pending intent template, allowing individualized behavior for each item
        Intent intentSetUris = new Intent(mContext, WidgetProvider.class);
        intentSetUris.setAction(WidgetProvider.OPEN_BROWSER);
        intentSetUris.setData(Uri.parse(intentSetUris.toUri(Intent.URI_INTENT_SCHEME)));

        PendingIntent browserPendingIntent = PendingIntent.getBroadcast(mContext, 0, intentSetUris, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViewWidget.setPendingIntentTemplate(R.id.stack_view, browserPendingIntent);
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_layout);

        if (position <= getCount()) {

            System.err.println("SIZE OF ENTITYARRAY:" + entityArray.size() + ", POSITION/INDEX:" + position);

            Entity entity = entityArray.get(position);

            double distance = getDistanceFromEntity(entity);

            Bitmap imgBitmap = getBitmapFromURL(entity.dishPhotoUrl);

            DecimalFormat distanceFormat = new DecimalFormat("#0.0");

            remoteViews.setTextViewText(R.id.text_dish, entity.topDish);
            remoteViews.setTextViewText(R.id.text_entity, entity.name);
            remoteViews.setTextViewText(R.id.text_distance, " | " + distanceFormat.format(distance) + " mi");

            remoteViews.setImageViewBitmap(R.id.image_view, imgBitmap);

            //Set fill-intent to fill in the pending intent template in WidgetProvider
            Bundle extras = new Bundle();
            extras.putString(WidgetProvider.EXTRA_ITEM, "https://likeness.com" + entity.nessUri);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            remoteViews.setOnClickFillInIntent(R.id.item_layout, fillInIntent);

            System.err.println("INSIDE GET VIEW AT for" + entity.name);

        }

        remoteViewWidget.setViewVisibility(R.id.refresh_button, View.VISIBLE);
        remoteViewWidget.setViewVisibility(R.id.progress_bar, View.INVISIBLE);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViewWidget);

        return remoteViews;
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
        System.err.println("GETS LOADING VIEW");
        return new RemoteViews(mContext.getPackageName(), R.layout.loading_item_layout);
    }

    public int getViewTypeCount() {
        return 2;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        entityArray.clear();
        if (Looper.myLooper() != Looper.getMainLooper() && entityArray.size() == 0) {
            getGPSlocation();
            getOnlineData();
            getUserAddress();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName nessWidget = new ComponentName(mContext, WidgetProvider.class);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);
        RemoteViews remoteViewWidget = WidgetProvider.remoteViews;

        if (entityArray.size() == 0) {
            remoteViewWidget.setTextViewText(R.id.empty_text, "The list is empty.");

            remoteViewWidget.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            remoteViewWidget.setViewVisibility(R.id.progress_bar, View.INVISIBLE);
        }

        if (!gpsStatusOn) {
            remoteViewWidget.setTextViewText(R.id.empty_text, "Location services are off.");

            remoteViewWidget.setViewVisibility(R.id.refresh_button, View.VISIBLE);
            remoteViewWidget.setViewVisibility(R.id.progress_bar, View.INVISIBLE);
        }

        //sets up refresh button
        Intent refreshIntent = new Intent(mContext, WidgetProvider.class);
        refreshIntent.setAction(WidgetProvider.REFRESH_ACTION);
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(mContext, 0, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViewWidget.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetIds, remoteViewWidget);
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

            gpsStatusOn = true;

        } else {

            gpsStatusOn = false;
        }

        if (latitude < 0.01) {
            latitude = 40.766;
            longitude = -73.975;
            ;
        }

    }


    private void getOnlineData() {

        url = "https://api-v3-s.trumpet.io/json-api/v3/search?options=HIDE_CLOSED_PLACES&lat=" + latitude + "&sortBy=BEST&location=Los%20Altos,CA&lon=" + longitude + "&category=restaurant&localtime=2012-07-20T16%3A40-0700&userId=00000000-000e-c25d-c000-000000026810&magicScreen=MENU";

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

                String uriWeb = ent.getString(TAG_NESS_URI);

                JSONObject loc = ent.getJSONObject(TAG_LOCATION);
                double entLat = loc.getDouble(TAG_LATITUDE);
                double entLon = loc.getDouble(TAG_LONGITUDE);

                JSONObject topItem = ent.getJSONObject(TAG_TOP_MENU_ITEM);
                String topItemName = topItem.getString(TAG_NAME);
                String topItemPhoto = topItem.getString(TAG_TOP_MENU_ITEM_PHOTO);

                String urlImg = null;
                if (ent.has(TAG_COVERPHOTO)) {
                    JSONObject coverphoto = ent.getJSONObject(TAG_COVERPHOTO);
                    urlImg = coverphoto.getString(TAG_THUMBNAIL_URL);
                }

                if (urlImg != null) {
                    //create Entity object with these variables and add it to an array
                    Entity objEntity = new Entity(name, uriWeb, urlImg, topItemName, topItemPhoto, entLat, entLon);
                    entityArray.add(objEntity);

                    System.err.println("ENT:" + objEntity.name);
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



