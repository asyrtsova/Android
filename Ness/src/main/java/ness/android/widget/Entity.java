package ness.android.widget;

import java.util.ArrayList;

/**
 * Created by administrator on 7/11/13.
 */
public class Entity {

    String allInfo;

    String name = "name";
    String address = "address";
    String type = "type";
    String price = "price";

    String typeString = " ";

    public Entity (String sName, String sAddress, String sType, String sPrice) {

        name = sName;
        address = sAddress;
        type = sType;
        price = sPrice;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getTypes() {
        return type;
    }

    public void setTypes(String type) {
        this.type = type;
    }

    public String toString() {

        allInfo = name + "\n" + type + ", Price Level:" + price + "\n";

        return allInfo;
    }

}
