/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mycalendar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ricky
 */
public class EventDAOImpl implements IEventDAO {

    @Override
    public Event findById(int id, Connection con) {
        try {
            String sql = "SELECT * FROM Events WHERE id = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next()) {
                int Id = result.getInt("id");
                String name = result.getString("name");
                String description = result.getString("description");
                LocalDateTime date = result.getObject(4, LocalDateTime.class);
                double latitude = result.getDouble("latitude");
                double longitude = result.getDouble("longitude");
                return new Event(Id, name, description, date, latitude, longitude);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void save(Event event, Connection con) {
        try{
            String sql = "INSERT INTO Events (Id, name, description, date, latitude, longitude) VALUES (=, ?, ?, ?, ?, ?)";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, event.getId());
            statement.setString(2, event.getName());
            statement.setString(3, event.getDescription());
            statement.setObject(4, event.getDate());
            statement.setDouble(5, event.getLatitude());
            statement.setDouble(6, event.getLongitude());
            statement.executeUpdate();
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void update(Event event, Connection con) {
       try{
            String sql = "UPDATE Events SET name = ?, description = ?, date = ?, latitude = ?, longitude = ? WHERE Id = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.setInt(1, event.getId()); //DA CHIEDERE A GROK SE L'ORIDNE E' IMPORTANTE
            statement.setString(2, event.getName());
            statement.setString(3, event.getDescription());
            statement.setObject(4, event.getDate());
            statement.setDouble(5, event.getLatitude());
            statement.setDouble(6, event.getLongitude());
            statement.executeUpdate();
            System.out.println("Record Updated");
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void delete(Event vent, Connection con) {
        try{
            String sql = "DELETE FROM Events WHERE Id = ?";
            PreparedStatement statement = con.prepareStatement(sql);
            statement.executeUpdate();
            System.out.println("User deleted");
        } catch(SQLException e){
            e.printStackTrace();
        }
    }

    @Override
    public List<Event> findAll(Connection con) {
        List<Event> events = new ArrayList<>();
        try{
            String sql = "SELECT FROM Events";
            PreparedStatement statement = con.prepareStatement(sql);
            ResultSet result = statement.executeQuery();
            while(result.next()){
                int Id = result.getInt("id");
                String name = result.getString("name");
                String description = result.getString("description");
                LocalDateTime date = result.getObject(4, LocalDateTime.class);
                double latitude = result.getDouble("latitude");
                double longitude = result.getDouble("longitude");
                Event event = new Event(Id, name, description, date, latitude, longitude);
                events.add(event);
            }
        } catch(SQLException e){
            e.printStackTrace();
        }
        return events;
    }
}
