package javaapplication1;

import java.util.Date;
import java.util.List;

/**
 *
 * @author ricky
 */
public class Event {
    String name, description;
    List<String> partecipants;
    Date date;

    public Event(String name, String description, List<String> partecipants, Date date) {
        this.name = name;
        this.description = description;
        this.partecipants = partecipants;
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

    public List<String> getPartecipants() {
        return partecipants;
    }

    public void setPartecipants(List<String> partecipants) {
        this.partecipants = partecipants;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
}
