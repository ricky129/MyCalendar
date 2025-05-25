package com.mycompany.mycalendar.JSON;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author ricky
 */
public class JSONResponse {
    @SerializedName("place_id")
    private long placeId;
    
    @SerializedName("licence")
    private String licence;
    
    @SerializedName("osm_type")
    private String osmType;
    
    @SerializedName("osm_id")
    private long osmId;
    
    @SerializedName("lat")
    private String lat;
    
    @SerializedName("lon")
    private String lon;
    
    @SerializedName("display_name")
    private String displayName;
    
    @SerializedName("address")
    private Address address;

    public long getPlaceId() {
        return placeId;
    }

    public String getLicence() {
        return licence;
    }

    public String getOsmType() {
        return osmType;
    }

    public long getOsmId() {
        return osmId;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Address getAddress() {
        return address;
    }
    
}
