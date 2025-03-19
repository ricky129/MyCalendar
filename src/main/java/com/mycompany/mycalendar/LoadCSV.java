package com.mycompany.mycalendar;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ricky
 */
public class LoadCSV {
    private DatabaseConnection db = new DatabaseConnection();
    private List<Event> events = new ArrayList<>();

    public List<Event> getEvents() {
        return events;
    }
public void printEvents() {
        if (events == null && events.isEmpty()) {
            System.out.println("No events loaded.");
            return;
        }

        for (Event event : events)
            System.out.println("Date: " + event.getDate() + ", Name: " + event.getName() + ", Description: " + event.getDescription());
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }
    
    public void loadCSV(){
        String sql = "SELECT FROM Events";
        
        try(Connection con = db.getConnection();
            PreparedStatement pstmt = con.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()){
            
            events.clear();
            
            while(rs.next()){
                Event event = new Event(
                rs.getInt("Id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getTimestamp("date").toLocalDateTime(),
                rs.getDouble("latitude"),
                rs.getDouble("longitude")
                );
            events.add(event);
            }
        } catch(SQLException e ){
            e.printStackTrace();
        }
        /*
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
            
                events.add(new Event(date, EventData[1], EventData[2]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/
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
    
    public void addEvents(Event E1){
        writeOnCSV(E1);
        events.add(E1);
    }
}
