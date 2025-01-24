package javaapplication1;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author ricky
 */
public class Event {
    String name, description;
    LocalDate date;

    public Event(LocalDate date, String name, String description) {
        this.name = name;
        this.description = description;
        this.date = date;
    }

    // Getters and Setters (same as before)
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
    
    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    
    @Override
    public String toString() {
        return "Date: " + date + ", Name: " + name + ", Description: " + description;
    }
}