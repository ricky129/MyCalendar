package com.mycompany.mycalendar.Event;

import com.mycompany.mycalendar.FrameController;
import com.mycompany.mycalendar.Map.MapsController;
import com.mycompany.mycalendar.MyCalendar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import static org.hibernate.type.InstantType.FORMATTER;

/**
 *
 * @author ricky
 */
public class EventDAOImpl implements IEventDAO {

    private static final EventDAOImpl instance = new EventDAOImpl();

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");

    // Create a logger for this class. It's common to name the logger after the class.
    private static final Logger logger = Logger.getLogger(EventDAOImpl.class.getName());

    private EntityManager getEntityManger() {
        return emf.createEntityManager();
    }

    public static EventDAOImpl getInstance() {
        return instance;
    }

    @Override
    public Event findById(int id) {
        EntityManager em = getEntityManger();
        try {
            return em.find(Event.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public void update(Event event) {
        EntityManager em = getEntityManger();
        try {
            em.getTransaction().begin();
            em.merge(event);
            em.getTransaction().commit();
            System.out.println("Record updated");
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
        } finally {
            em.close();
        }
    }

    @Override
    public boolean delete(Event event, EntityManagerFactory emf) {
        if (deleteEventSQL(event, emf))
            return true;
        else
            return deleteEventCSV(event);
    }

    //Close EMF when application shuts down
    public static void shutdown() {
        if (emf != null && emf.isOpen())
            emf.close();
    }

    @Override
    public boolean save(LocalDateTime dateFromUser, EntityManagerFactory emf, JTextField NewEventName, JTextArea NewEventDescription, double selectedLatitude, double selectedLongitude, JButton NewEvent, JTable CalendarJTable, JComboBox MonthSelectorComboBox) {
        if (saveEventSQL(dateFromUser, emf, NewEventName, NewEventDescription, selectedLatitude, selectedLongitude, NewEvent, CalendarJTable, MonthSelectorComboBox))
            return true;
        else
            return saveEventCSV(dateFromUser, NewEventName, NewEventDescription, selectedLatitude, selectedLongitude, NewEvent);
    }

    @Override
    public List<Event> getEvents(LocalDateTime dateFromUser) {
        List<Event> ret = getEventsForDateFromSQL(dateFromUser);
        if (ret != null)
            return ret;
        else {
            return getEventsForDateFromCSV(dateFromUser);
        }
    }

    @Override
    public List<Event> findAll() {
        EntityManager em = getEntityManger();
        try {
            TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e", Event.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }
    
    private static List<Event> getEventsForDateFromSQL(LocalDateTime dateFromUser) {
        FrameController FC1 = FrameController.getInstance();
        EntityManager em = MyCalendar.getEmf().createEntityManager();

        System.out.println("Reading events on dateFromuser: " + dateFromUser + " in SQL...");
        try {
            LocalDateTime startOfDay = dateFromUser.truncatedTo(ChronoUnit.DAYS); // e.g., 2025-01-01 00:00:00
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            TypedQuery<Event> query = em.createQuery("SELECT e FROM Event e WHERE e.date >= :start AND e.date < :end", Event.class);
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            List<Event> events = query.getResultList();
            FC1.addCoordinatesToList(events);
            return events;
        } finally {
            em.close();
        }
    }

    private static List<Event> getEventsForDateFromCSV(LocalDateTime dateFromUser) {
        List<Event> events = new ArrayList<>();
        String line;
        String csvFile = "events.csv"; // Assuming events.csv is in the project root or accessible path
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime startOfDay = dateFromUser.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        System.out.print("Reading events on dateFromuser: " + dateFromUser + " in CSV...");
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Read the header line
            br.readLine(); // Skip the header row: "name,description,date,latitude,longitude,location"
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) {
                    // Ensure all fields are present
                    String name = data[0];
                    String description = data[1];
                    LocalDateTime eventDate = LocalDateTime.parse(data[2], formatter);
                    double latitude = Double.parseDouble(data[3]);
                    double longitude = Double.parseDouble(data[4]);
                    String location = data[5];
                    // Check if the event date falls within the desired day
                    if (!eventDate.isBefore(startOfDay) && eventDate.isBefore(endOfDay)) {
                        Event event = new Event(name, description, eventDate, latitude, longitude, location);
                        events.add(event);
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
        }
        // The addCoordinatesToList method is not provided, so it's commented out.
        // If it's still needed, you would call it here after populating the events list.
        // addCoordinatesToList(events);
        return events;
    }

    // Method to save the event with the selected coordinates to the database
    private static boolean saveEventSQL(LocalDateTime dateFromUser, EntityManagerFactory emf, JTextField NewEventName, JTextArea NewEventDescription, double selectedLatitude, double selectedLongitude, JButton NewEvent, JTable CalendarJTable, JComboBox MonthSelectorComboBox) {
        EntityManager em = emf.createEntityManager();
        MapsController MC1 = MapsController.getInstance();

        System.out.println("Saving event " + NewEventName.getName() + " to SQL...");
        try {
            em.getTransaction().begin();
            Event newEvent = new Event(NewEventName.getText(), NewEventDescription.getText(), dateFromUser, selectedLatitude, selectedLongitude, MC1.getAddressFromCoordinates(selectedLatitude, selectedLongitude).getDisplayName());
            em.persist(newEvent); //persist the event to the database
            em.getTransaction().commit();
            //Reset UI after saving
            NewEventName.setText("");
            NewEventDescription.setText("");
            //NewEvent.setText("");
        } catch (Exception e) {
            em.getTransaction().rollback();
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
            System.out.println("Failed to add event to database");
            return false;
        } finally {
            em.close();
        }
        return true;
    }

    private boolean deleteEventSQL(Event eventToDelete, EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();

        System.out.println("Deleting event " + eventToDelete.getName() + " in SQL...");
        try {
            em.getTransaction().begin();
            Event toDelete = em.find(Event.class, eventToDelete.getId());
            if (toDelete != null)
                em.remove(toDelete);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
            return false;
        } finally {
            em.close();
        }
        return true;
    }

    // Method to convert Event object to CSV string
    public static String toCsvString(Event event) {
        return String.format("%s,%s,%s,%.4f,%.4f,\"%s\"",
                event.getName(),
                event.getDescription(),
                event.getDate().format(FORMATTER),
                event.getLatitude(),
                event.getLongitude(),
                event.getLocation().replace("\"", "\"\"") // Handle quotes in location string
        );
    }

    private static boolean saveEventCSV(LocalDateTime dateFromUser, JTextField NewEventName, JTextArea NewEventDescription, double selectedLatitude, double selectedLongitude, JButton NewEvent) {
        MapsController MC1 = MapsController.getInstance();

        System.out.println("Saving event to CSV with Event: " + NewEventName.getName() + "...");
        try {
            // Get the address from coordinates (assuming MapsController can do this without a database)
            String locationAddress = MC1.getAddressFromCoordinates(selectedLatitude, selectedLongitude).getDisplayName();

            // Create a new Event object
            Event newEvent = new Event(
                    NewEventName.getText(),
                    NewEventDescription.getText(),
                    dateFromUser,
                    selectedLatitude,
                    selectedLongitude,
                    locationAddress
            );

            // Read all existing lines from the CSV
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader("events.csv"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    lines.add(line);
                }
            } catch (FileNotFoundException e) {
                // If file doesn't exist, create it with a header
                lines.add("name,description,date,latitude,longitude,location");
            }

            // Append the new event as a CSV line
            lines.add(toCsvString(newEvent));

            // Write all lines back to the CSV file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("events.csv"))) {
                for (String line : lines) {
                    bw.write(line);
                    bw.newLine();
                }
            }

            // Reset UI after saving
            NewEventName.setText("");
            NewEventDescription.setText("");
            NewEvent.setText(""); // This might not be appropriate for a JButton's text
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
            System.out.println("Failed to add event to CSV file");
            return false;
        }
    }

    private boolean deleteEventCSV(Event eventToDelete) {
        List<String> lines = new ArrayList<>();
        boolean eventFoundAndDeleted = false;

        System.out.println("Deleting event + " + eventToDelete.getName() + " in CSV...");
        try (BufferedReader br = new BufferedReader(new FileReader("events.csv"))) {
            String header = br.readLine(); // Read header
            if (header != null)
                lines.add(header); // Keep header

            String line;
            while ((line = br.readLine()) != null) {
                // Parse the line to an Event object to compare
                // This is a simplified parsing. In a real application, you'd parse carefully.
                String[] data = line.split(",");
                if (data.length == 6)
                    try {
                    String name = data[0];
                    String description = data[1];
                    LocalDateTime date = LocalDateTime.parse(data[2], FORMATTER);
                    double latitude = Double.parseDouble(data[3]);
                    double longitude = Double.parseDouble(data[4]);
                    String location = data[5];

                    // Create a temporary event to compare
                    // This comparison assumes exact match on all fields, or you might need a unique ID in CSV
                    if (name.equals(eventToDelete.getName())
                            && description.equals(eventToDelete.getDescription())
                            && date.isEqual(eventToDelete.getDate())
                            && latitude == eventToDelete.getLatitude()
                            && longitude == eventToDelete.getLongitude()
                            && location.equals(eventToDelete.getLocation()))
                        eventFoundAndDeleted = true; // Skip adding this line to the new list, effectively deleting it
                    else
                        lines.add(line); // Keep other lines
                } catch (NumberFormatException parseException) {
                    System.err.println("Error parsing CSV line: " + line + " - " + parseException.getMessage());
                    lines.add(line); // Add problematic line back to avoid data loss
                    return false;
                } else
                    lines.add(line); // Add malformed lines back
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
            System.out.println("Failed to read CSV file for deletion");
            return false;
        }

        if (eventFoundAndDeleted)
            // Write the modified list back to the CSV file
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("events.csv"))) {
            for (String line : lines) {
                bw.write(line);
                bw.newLine();
            }
            System.out.println("Event deleted successfully from CSV.");
        } catch (IOException e) {
            logger.log(Level.SEVERE, "An error occurred during an operation.", e);
            System.out.println("Failed to write updated CSV file after deletion");
            return false;
        } else
            System.out.println("Event not found in CSV for deletion.");
        return true;
    }
}
