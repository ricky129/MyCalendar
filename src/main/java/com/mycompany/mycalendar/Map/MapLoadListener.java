package com.mycompany.mycalendar.Map;

/**
 *This allows any class to listen for map loading events without MapsController needing to know the details of the listener’s implementation.
 * @author ricky
 */
public interface MapLoadListener {
    public void onMapLoaded ();
}
