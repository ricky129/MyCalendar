package com.mycompany.mycalendar.Event;

import com.mycompany.mycalendar.JSONResponse;
import com.mycompany.mycalendar.Map.MapsController;
import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author ricky
 */
public class EventController {
    MapsController MC1 = new MapsController();
    JSONResponse response;

    // Method to save the event with the selected coordinates to the database
    public void saveEvent(
            LocalDateTime dateFromUser,
            EntityManagerFactory emf,
            JTextField NewEventName,
            JTextArea NewEventDescription,
            double selectedLatitude,
            double selectedLongitude,
            JButton NewEvent,
            JTable CalendarJTable,
            JComboBox MonthSelectorComboBox
    ) {
        
        response = MC1.getAddressFromCoordinates();
        
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            
            Event newEvent = new Event(
                    0,
                    NewEventName.getText(),
                    NewEventDescription.getText(),
                    dateFromUser,
                    selectedLatitude,
                    selectedLongitude,
                    response.getDisplayName()
            );
            em.persist(newEvent); //persist the event to the database
            em.getTransaction().commit();

            //Reset UI after saving
            NewEventName.setText("");
            NewEventDescription.setText("");
            NewEvent.setText("");

        } catch (Exception e) {
            em.getTransaction().rollback();
            e.printStackTrace();
            System.out.println("Failed to add event to database");
        } finally {
            em.close();
        }
    }

    public void deleteEvent(Event event, EntityManagerFactory emf) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Event toDelete = em.find(Event.class, event.getId());
            if (toDelete != null)
                em.remove(toDelete);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive())
                em.getTransaction().rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
