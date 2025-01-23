package javaapplication1;

import java.time.LocalDate;
import java.util.List;

/**
 *
 * @author ricky
 */
public class Event {
    String name, description;
    List<String> participants;
    LocalDate date;

    public Event(String name, String description, List<String> participants, LocalDate date) {
        this.name = name;
        this.description = description;
        this.participants = participants;
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

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
    
}