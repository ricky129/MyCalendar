package com.mycompany.mycalendar.Event;

import com.mycompany.mycalendar.FrameController;
import com.opencsv.CSVReader;
import java.io.FileReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ricky
 */
public class EventService {

    List<Event> events = new ArrayList<>();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    FrameController FC1 = FrameController.getInstance();

    public List<Event> getEventsForDateFromCSV(LocalDateTime dateFromUser) {
        LocalDateTime startOfDay = dateFromUser.truncatedTo(ChronoUnit.DAYS);// e.g., 2025-01-01 00:00:00
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        try (CSVReader csvReader = new CSVReader(new FileReader("events.csv"))) {
            //skip header row
            csvReader.readNext();

            String[] record;

            System.out.println("Reading CSV with dateFromUser: " + dateFromUser);
            
            while ((record = csvReader.readNext()) != null) {
                
                Event event = new Event();
                event.setDate(LocalDateTime.parse(record[2], formatter));
                try {
                    if (!event.getDate().isBefore(startOfDay) && event.getDate().isBefore(endOfDay)) {
                        event.setName(record[0]);
                        event.setDescription(record[1]);
                        event.setLatitude(Float.parseFloat(record[3]));
                        event.setLongitude(Float.parseFloat(record[4]));
                        event.setLocation(record[5]);
                        
                        events.add(event);
                    }
                } catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
            FC1.addCoordinatesToList(events);
        } catch (Exception ex) {
            Logger.getLogger(EventService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return events;
    }

}
