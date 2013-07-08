package ness.android.widget;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    GPSTracker gps;
    String gpsStatus;
    double longitude;
    double latitude;

    TextView textview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getGPSlocation();

        textview = (TextView)findViewById(R.id.text_view);
        setContentView(R.layout.widget_layout);

        textview.setText("Your Location is - \nLat: " + latitude + "\nLong: " + longitude);
    }

    private void getGPSlocation() {

        gps = new GPSTracker(MainActivity.this);

        if(gps.canGetLocation()){

            latitude = gps.getLatitude();
            longitude = gps.getLongitude();


        }else {
            gpsStatus = "GPS/network not enabled.";
        }
    }

}

