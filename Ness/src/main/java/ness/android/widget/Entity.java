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

    Bitmap photoBitmap;

    public Entity (String sName, String sAddress, String sType, String sPriceNum, String uriWeb, Bitmap img) {

        name = sName;
        address = sAddress;
        type = sType;
        priceNum = sPriceNum;
        nessUri = uriWeb;
        photoBitmap = img;

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

        allInfo = name + "\n" + address + ", " + priceSign + "\n";

        return allInfo;
    }
}
