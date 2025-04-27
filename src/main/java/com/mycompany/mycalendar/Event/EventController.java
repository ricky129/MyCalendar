package com.mycompany.mycalendar.Event;

import com.mycompany.mycalendar.FormController;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author ricky
 */
public class EventController {
    
    FormController FC1 = FormController.getInstance();

    public EventController() {
    }
    
    // Method to save the event with the selected coordinates to the database
    public void saveEventWithCoordinates(
            JSpinner NewDate,
            EntityManagerFactory emf,
            JTextField NewEventName,
            JTextArea NewEventDescription,
            double selectedLatitude,
            double selectedLongitude,
            JButton NewEvent,
            JTable jTable1,
            JComboBox jComboBox2
            ) {
        Instant instant = ((Date) NewDate.getValue()).toInstant(); // Convert the spinner date to an Instant
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDateTime date = zonedDateTime.toLocalDateTime();

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Event newEvent = new Event(
                    0,
                    NewEventName.getText(),
                    NewEventDescription.getText(),
                    date,
                    selectedLatitude,
                    selectedLongitude,
                    null //TODO add reverse logic to get address and/or name
            );
            em.persist(newEvent); //persist the event to the database
            em.getTransaction().commit();

            //Reset UI after saving
            NewEventName.setText("");
            NewEventDescription.setText("");
            NewEvent.setText("");
            
            FC1.updateCalendar(jTable1, jComboBox2, Month.of(jComboBox2.getSelectedIndex() + 1));
        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            System.out.println("Failed to add event to database");
        } finally {
            em.close();
        }
    }
}
