/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
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
