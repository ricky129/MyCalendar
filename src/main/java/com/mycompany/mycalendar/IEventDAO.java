/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mycompany.mycalendar;

import java.sql.Connection;
import java.util.List;

/**
 *
 * @author ricky
 */
public interface IEventDAO {
    Event findById(int id, Connection con);
    void save(Event event, Connection con);
    void update(Event event, Connection con);
    void delete(Event vent, Connection con);
    List<Event> findAll(Connection con);
}
