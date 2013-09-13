package ness.android.widget;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;

import java.util.concurrent.Semaphore;

/**
 * Created by administrator on 9/6/13.
 */
public class GPLocationServices implements  GooglePlayServicesClient.ConnectionCallbacks,  GooglePlayServicesClient.OnConnectionFailedListener  {

    public boolean servicesAvailable;
    public Location location;
    public final Semaphore dataAvailable = new Semaphore(0);

    public double lat;
    public double lon;

    private Context mContext;
    private LocationClient locationClient;

    public GPLocationServices(Context context){
        mContext = context;
        if (checkAvailability()) {
            locationClient = new LocationClient(context, this, this);
            locationClient.connect();
        }


    }

    public boolean checkAvailability() {

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext) == ConnectionResult.SUCCESS) {
            System.err.println("GOOGLE PLAY SERVICES CONNECTION AVAILABLE");
            servicesAvailable = true;
            return true;
        } else {
            servicesAvailable = false;
            return false;
        }

    }

    public void disconnect() {
        if(locationClient != null) {
            locationClient.disconnect();
        }
    }


    @Override
    public void onConnected(Bundle bundle) {
        System.err.println("GOOGLE PLAY SERVICES CONNECTED");

        location = locationClient.getLastLocation();
        lat = location.getLatitude();
        lon = location.getLongitude();
        System.err.println("GP: LAT: " + lat + " LON:" + lon);

        dataAvailable.release(Integer.MAX_VALUE);

        disconnect();
    }

    @Override
    public void onDisconnected() {
        System.err.println("GOOGLE PLAY SERVICES DISCONNECTED");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
