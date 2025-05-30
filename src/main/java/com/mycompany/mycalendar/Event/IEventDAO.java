package com.mycompany.mycalendar.Event;

import java.time.LocalDateTime;
import java.util.List;
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
public interface IEventDAO {
    Event findById(int id);
    boolean save(LocalDateTime dateFromUser, EntityManagerFactory emf, JTextField NewEventName, JTextArea NewEventDescription);
    void update(Event event);
    boolean delete(Event event, EntityManagerFactory emf);
    List<Event> getEvents(LocalDateTime dateFromUser);
    List<Event> findAll();
}
