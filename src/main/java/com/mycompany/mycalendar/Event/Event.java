package com.mycompany.mycalendar.Event;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.*;
import javax.persistence.Table;

/**
 *
 * @author ricky
 */
@Entity
@Table(name = "Events")
public class Event implements Serializable {
    /*
    public String getAddress() {
        MapsController MC1 = new MapsController();
        return MC1.getAddressFromCoordinates().getDisplayName();
    }
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int Id;
    
    @Column (name = "name", nullable = false, length = 50)
    private String name;
    @Column (name = "description", nullable = false, length = 200)
    private String description;
    @Column (name = "date", nullable = false)
    private LocalDateTime date;
    @Column (name = "latitude", nullable = false)
    private double latitude;
    @Column (name = "longitude", nullable = false)
    private double longitude;
    @Column (name = "location", nullable = true)
    private String location;

    public Event(int Id, String name, String description, LocalDateTime date, double latitude, double longitude, String location) {
        this.Id = Id;
        this.name = name;
        this.description = description;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.location = location;
    }

    public Event() {
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

    public void setLocation(String location) {
    }

    public String getLocation() {
        return location;
    }
}