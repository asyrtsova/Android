package ness.android.widget;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by administrator on 7/11/13.
 */
public class Entity {

    String name = "name";
    String nessUri = "nessUri";
    String photoUri = "imgUri";
    String topDish = "topDish";
    String dishPhotoUrl = "dishPhotoUrl";

    double latitude;
    double longitude;

    public Entity (String sName, String uriWeb, String imgUri, String sTopDish, String sDishUrl, double entLatitude, double entLongitude) {

        name = sName == null? "name": sName;
        nessUri = uriWeb == null? "nessUri": uriWeb;
        photoUri = imgUri == null? "imgUri": imgUri;
        topDish = sTopDish == null?"sTopDish": sTopDish;
        dishPhotoUrl = sDishUrl == null? "sDishUrl": sDishUrl;
        latitude = entLatitude;
        longitude = entLongitude;

        dishPhotoUrl = getSmallerImg(dishPhotoUrl);
    }

    //5 instead of 7 at end of Ness' img Url gets smaller image
    private String getSmallerImg(String initialUrl) {
        return initialUrl.substring(0, initialUrl.length() - 5) + "5.jpg";
    }

}
