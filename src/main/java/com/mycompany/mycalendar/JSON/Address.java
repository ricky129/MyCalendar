package com.mycompany.mycalendar.JSON;

import com.google.gson.annotations.SerializedName;

/**
 *
 * @author ricky
 */
public class Address {
    @SerializedName("house_number")
    private String house_number;
    
    @SerializedName("building")
    private String road;
    
    @SerializedName("neighbourhood")
    private String neighbourhood;
    
    @SerializedName("suburb")
    private String suburb;
    
    @SerializedName("city")
    private String city;
    
    @SerializedName("town")
    private String town;
    
    @SerializedName("village")
    private String village;
    
    @SerializedName("county")
    private String county;
    
    @SerializedName("state")
    private String state;
    
    @SerializedName("postcode")
    private String postcode;
    
    @SerializedName("country")
    private String country;
    
    @SerializedName("country_code")
    private String countryCode;
    
    public String getLocality(){
        if(city != null)
            return city;
        if(town != null)
            return town;
        if(village != null)
            return village;
        return null;        
    }

    public String getHouse_number() {
        return house_number;
    }

    public String getRoad() {
        return road;
    }

    public String getNeighbourhood() {
        return neighbourhood;
    }

    public String getSuburb() {
        return suburb;
    }

    public String getCity() {
        return city;
    }

    public String getTown() {
        return town;
    }

    public String getVillage() {
        return village;
    }

    public String getCounty() {
        return county;
    }

    public String getState() {
        return state;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCountry() {
        return country;
    }

    public String getCountryCode() {
        return countryCode;
    }
}
