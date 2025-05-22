package com.mycompany.mycalendar;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author ricky
 */
public class MyCalendar {
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");

    public static void main(String args[]) {
        try {
            System.out.println("Hibernate initialized.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Set Nimbus look and feel
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(NewJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
}