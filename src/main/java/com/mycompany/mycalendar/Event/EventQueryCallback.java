package com.mycompany.mycalendar.Event;

import java.util.List;

/**
 *
 * @author ricky
 */
public interface EventQueryCallback {
    void onEventsLoaded(List<Event> events);
}

/**
 * Since the map loading is asynchronous, we need a way to handle the 
 * query results once the map is loaded. A callback allows us to execute 
 * he UI update (e.g., showing events) after the query completes.
 */