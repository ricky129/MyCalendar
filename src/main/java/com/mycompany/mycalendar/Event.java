package com.mycompany.mycalendar;

import java.time.LocalDateTime;

/**
 *
 * @author ricky
 */
public class Event {
    String name, description;
    LocalDateTime date;

    public Event(LocalDateTime date, String name, String description) {
        this.name = name;
        this.description = description;
        this.date = date;
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
        return "Date: " + date + ", Name: " + name + ", Description: " + description;
    }
}