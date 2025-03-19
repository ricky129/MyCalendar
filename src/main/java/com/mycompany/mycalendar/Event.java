package com.mycompany.mycalendar;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author ricky
 */
@Entity
@Table(name = "Events")
public class Event {
    @Id
    private int Id;
    
    @Column (name = "name", nullable = false, length = 20)
    private String name, description;
    private LocalDateTime date;
    private double latitude;
    private double longitude;

    public Event(int Id, String name, String description, LocalDateTime date, double latitude, double longitude) {
        this.Id = Id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getId() {
        return Id;
    }

    public void setId(int Id) {
        this.Id = Id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return "Event{" +
                "id=" + Id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}