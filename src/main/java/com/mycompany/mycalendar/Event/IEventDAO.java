package com.mycompany.mycalendar.Event;

import java.util.List;

/**
 *
 * @author ricky
 */
public interface IEventDAO {
    Event findById(int id);
    void save(Event event);
    void update(Event event);
    void delete(Event vent);
    List<Event> findAll();
}
