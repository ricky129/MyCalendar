package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Event.EventDAOImpl;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author ricky
 */
public class MyCalendar {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private static final Logger logger = Logger.getLogger(EventDAOImpl.class.getName());

    public static void main(String args[]) {
        System.out.println("Hibernate initialized.");

        // Set Nimbus look and feel
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, "An error occurred during an operation.", ex);
        }

        // Use the Singleton instance instead of creating a new one
        java.awt.EventQueue.invokeLater(() -> {
            NewJFrame.getInstance().setVisible(true);
        });

        // Shutdown hook to close EMF
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (emf != null && emf.isOpen()) {
                emf.close();
                System.out.println("EntityManagerFactory closed.");
            }
        }));
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }
}
