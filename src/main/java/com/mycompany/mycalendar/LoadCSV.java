package com.mycompany.mycalendar;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ricky
 */
public class LoadCSV {
    public List<Event> eventi = new ArrayList<>();

    public List<Event> getEventi() {
        return eventi;
    }
public void printEvents() {
        if (eventi == null || eventi.isEmpty()) {
            System.out.println("No events loaded.");
            return;
        }

        //System.out.println("Loaded Events:");
        for (Event event : eventi) {
            //System.out.println(event); // Calls Event's toString() method
            // Or for more detailed output:
            //System.out.println("Date: " + event.getDate() + ", Name: " + event.getName() + ", Description: " + event.getDescription());
        }
    }
    public void setEventi(List<Event> eventi) {
        this.eventi = eventi;
    }
    public void loadCSV(){
    // Path to the CSV file
        String csvFile = "events.csv";
        String line;
        
        
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Skip header line
            br.readLine();

            // Read each line of the CSV file
            while ((line = br.readLine()) != null) {
                // Split the line by comma to get user data
                String[] EventData = line.split(",");
                
                // Parse the date string using a formatter
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate date = LocalDate.parse(EventData[0], formatter);
            
                eventi.add(new Event(date, EventData[1], EventData[2]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
