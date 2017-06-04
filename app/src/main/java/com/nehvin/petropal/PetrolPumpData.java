package com.nehvin.petropal;

/**
 * Created by Vineet K Jain on 6/3/2017.
 */

public class PetrolPumpData {

    private String pp_lat;
    private String pp_lng;
    private String pp_name;
    private String pp_Address;

    public PetrolPumpData(String pp_lat, String pp_lng, String pp_name, String pp_Address) {
        this.pp_lat = pp_lat;
        this.pp_lng = pp_lng;
        this.pp_name = pp_name;
        this.pp_Address = pp_Address;
    }

    public String getPp_lat() {
        return pp_lat;
    }

    public void setPp_lat(String pp_lat) {
        this.pp_lat = pp_lat;
    }

    public String getPp_lng() {
        return pp_lng;
    }

    public void setPp_lng(String pp_lng) {
        this.pp_lng = pp_lng;
    }

    public String getPp_name() {
        return pp_name;
    }

    public void setPp_name(String pp_name) {
        this.pp_name = pp_name;
    }

    public String getPp_Address() {
        return pp_Address;
    }

    public void setPp_Address(String pp_Address) {
        this.pp_Address = pp_Address;
    }
}
