package ness.android.widget;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName nessWidget = new ComponentName(mContext, WidgetProvider.class);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);
        RemoteViews remoteViewWidget = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout);
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.item_layout);

        if (position <= getCount()) {

            Entity entity = entityArray.get(position);

            double distance = getDistanceFromEntity(entity);

            Bitmap imgBitmap = getBitmapFromURL(entity.photoUri);

            DecimalFormat distanceFormat = new DecimalFormat("#0.0");

            remoteViews.setTextViewText(R.id.text_dish, "Dish Name Here");
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
        if (Looper.myLooper() != Looper.getMainLooper() && entityArray.size() == 0) {
            getGPSlocation();
            getOnlineData();
            getUserAddress();
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
        ComponentName nessWidget = new ComponentName(mContext, WidgetProvider.class);

        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(nessWidget);
        RemoteViews remoteViewWidget = new RemoteViews(mContext.getPackageName(), R.layout.widget_layout);

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

                String uriWeb = ent.getString(TAG_NESS_URI);

                JSONObject loc = ent.getJSONObject(TAG_LOCATION);
                double entLat = loc.getDouble(TAG_LATITUDE);
                double entLon = loc.getDouble(TAG_LONGITUDE);

                String urlImg = null;
                if (ent.has(TAG_COVERPHOTO)) {
                    JSONObject coverphoto = ent.getJSONObject(TAG_COVERPHOTO);
                    urlImg = coverphoto.getString(TAG_THUMBNAIL_URL);

                    //create Entity object with these variables and add it to an array
                    Entity objEntity = new Entity(name, uriWeb, urlImg, entLat, entLon);
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



