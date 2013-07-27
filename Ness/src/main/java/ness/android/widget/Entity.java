package ness.android.widget;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by administrator on 7/11/13.
 */
public class Entity {

    String allInfo;

    String name = "name";
    String nessUri = "nessUri";
    String photoUri = "imgUri";
    double latitude;
    double longitude;

    public Entity (String sName, String uriWeb, String imgUri, double entLatitude, double entLongitude) {

        name = sName;
        nessUri = uriWeb;
        photoUri = imgUri;
        latitude = entLatitude;
        longitude = entLongitude;

    }

    public String toString() {

        allInfo = name + "\n" + nessUri + "\n" + photoUri + "\n" + latitude + "\n" + longitude;

        return allInfo;
    }
}
