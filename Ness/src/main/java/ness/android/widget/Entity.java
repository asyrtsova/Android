package ness.android.widget;

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

    public Entity (String sName, String sAddress, String sType, String sPriceNum, String uri) {

        name = sName;
        address = sAddress;
        type = sType;
        priceNum = sPriceNum;
        nessUri = uri;

        switch (java.lang.Integer.parseInt(priceNum)) {
            case 1: priceSign = "$";
                break;
            case 2: priceSign = "$$";
                break;
            case 3: priceSign = "$$$";
                break;
            default: priceSign = "?";
        }

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

    public String getPriceNum() {
        return priceNum;
    }

    public void setPriceNum(String price) {
        this.priceNum = price;
    }

    public String getTypes() {
        return type;
    }

    public void setTypes(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNessUri() {
        return nessUri;
    }

    public void setNessUri(String nessUri) {
        this.nessUri = nessUri;
    }


    public String toString() {

        allInfo = name + "\n" + type + ", " + priceSign + "\n";

        return allInfo;
    }

}
