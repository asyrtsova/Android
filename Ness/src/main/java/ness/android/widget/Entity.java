package ness.android.widget;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by administrator on 7/11/13.
 */
public class Entity {

    String allInfo;

    String name = "name";
    String address = "address";
    String type = "type";
    String nessUri = "nessUri";
    String priceNum = "priceNum";
    String priceSign = "priceSign";
    String photoUri = "imgUri";
    double latitude;
    double longitude;

    public Entity (String sName, String sAddress, String sType, String sPriceNum, String uriWeb, String imgUri, double entLatitude, double entLongitude) {

        name = sName;
        address = sAddress;
        type = sType;
        priceNum = sPriceNum;
        nessUri = uriWeb;
        photoUri = imgUri;
        latitude = entLatitude;
        longitude = entLongitude;

        switch (java.lang.Integer.parseInt(priceNum)) {
            case 1: priceSign = "$";
                break;
            case 2: priceSign = "$$";
                break;
            case 3: priceSign = "$$$";
                break;
            case 4: priceSign = "$$$$";
                break;
            case 5: priceSign = "$$$$$";
                break;
            default: priceSign = "?";
        }

    }

    public String toString() {

        allInfo = name + "\n" + address + ", " + priceSign + "\n" + nessUri + "\n" + photoUri + "\n" + latitude + "\n" + longitude;

        return allInfo;
    }
}
