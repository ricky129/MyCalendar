package com.mycompany.mycalendar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
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
        if (eventi == null && eventi.isEmpty()) {
            System.out.println("No events loaded.");
            return;
        }

        for (Event event : eventi)
            System.out.println("Date: " + event.getDate() + ", Name: " + event.getName() + ", Description: " + event.getDescription());
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
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                LocalDateTime date = LocalDateTime.parse(EventData[0], formatter);
            
                eventi.add(new Event(date, EventData[1], EventData[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void writeOnCSV(Event E1){
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("events.csv", true))) {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            String dateString = E1.getDate().format(formatter);
            String newLine = dateString + "," + E1.getName() + "," + E1.getDescription();
            writer.newLine();
            writer.write(newLine);
        } catch(IOException ex){
            ex.printStackTrace();
        }
    }
    
    public void addEventi(Event E1){
        writeOnCSV(E1);
        eventi.add(E1);
    }
}
