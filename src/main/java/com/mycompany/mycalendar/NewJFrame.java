/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.mycalendar;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import netscape.javascript.JSObject;

/**
 *
 * @author ricky
 */
public class NewJFrame extends javax.swing.JFrame implements MapCallback {

    //EntityManagerFactory as a static field to avoid recreating it repeatedly
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private final FormController FC1 = new FormController();
    // JFXPanel to embed the JavaFX WebView (OSM map) in Swing
    private JFXPanel fxPanel;
    private double selectedLongitude;
    private double selectedLatitude;
    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
        initComponents();
        FC1.updateCalendar(jTable1, jComboBox2, Month.of(jComboBox2.getSelectedIndex() + 1));
        addTableClickListener();
        NewEventDescription.setEnabled(false);
        NewEventName.setEnabled(false);
        initializeMap();
        
        //LISTENER AL TEXT FIELD
        NewEventDescription.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                NewEvent.setText("Add Event");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!NewEventDescription.getText().isEmpty()) {
                    NewEvent.setText("Add Event");
                } else {
                    NewEvent.setText("NuovoEvento");
                    NewEventDescription.setEnabled(false);
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!NewEventDescription.getText().isEmpty()) {
                    NewEvent.setText("Add Event");
                } else {
                    NewEvent.setText("NuovoEvento");
                }
            }
        });
    }

    private void addTableClickListener() {
        jTable1.addMouseListener(new MouseAdapter() {   //extends MouseAdapter
            @Override
            public void mouseClicked(MouseEvent e) {
                /*This gets the JTable object that was clicked.  
                e.getSource() returns the component that triggered the event, which is the jTable1 in this case.
                 */
                JTable target = (JTable) e.getSource();
                EventName.setText("");
                EventInfo.setText("");

                int row = target.getSelectedRow();
                int column = target.getSelectedColumn();

                if (row != -1 && column != -1) { // Check if a cell was actually clicked
                    Object value = target.getValueAt(row, column);
                    System.out.println("Clicked on cell: Row " + row + ", Column " + column + ", Value: " + value);
                    int day = (Integer) value;
                    Month selectedMonth = Month.of(jComboBox2.getSelectedIndex() + 1);
                    int year = Integer.parseInt(jComboBox1.getSelectedItem().toString());
                    LocalDateTime selectedDate = LocalDateTime.of(year, selectedMonth, day, 00, 00, 00);
                    System.out.println("Data clickata: " + selectedDate);
                    if (!FC1.updateInfoBox(selectedDate, EventName, EventInfo)) {
                        EventName.setText("");
                        EventInfo.setText("No event found!");
                    }
                }
            }
        });
    }
    
    // Method to initialize the JavaFX WebView with the OSM map
    private void initializeMap() {
        fxPanel = new JFXPanel(); // Create a JFXPanel to host JavaFX content in Swing
        Platform.runLater(() -> { // Run JavaFX code on the JavaFX Application Thread
            WebView webView = new WebView(); // Create a WebView to render the map HTML
            webView.getEngine().load(getClass().getResource("/html/map.html").toExternalForm()); // Load the OSM map HTML
            // Listener to set up the Java-JavaScript bridge once the page loads
            webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) { // When the map loads successfully
                    JSObject window = (JSObject) webView.getEngine().executeScript("window"); // Access the JavaScript window object
                    window.setMember("javaCallback", this); // Bind this Java object to JavaScript’s javaCallback
                }
            });
            fxPanel.setScene(new Scene(webView, 600, 400)); // Set the WebView scene with a 600x400 size
        });
    }
    
    // Method to display the OSM map in a dialog for location selection
    private void showMapDialog() {
        JDialog mapDialog = new JDialog(this, "Select Event Location", true);
        mapDialog.setLayout(new java.awt.BorderLayout());
        mapDialog.add(fxPanel, java.awt.BorderLayout.CENTER);
        
        javax.swing.JButton saveButton = new javax.swing.JButton("Save Location");
        saveButton.addActionListener(e -> {
            saveEventWithCoordinates();
            mapDialog.dispose();
        });
        mapDialog.add(saveButton, java.awt.BorderLayout.SOUTH);
        
        mapDialog.pack();
        mapDialog.setLocationRelativeTo(this);
        mapDialog.setVisible(true);
    }
    
    // Implementation of MapCallback interface to receive coordinates from the map
    @Override
    public void setCoordinates(double latitude, double longitude) {
        this.selectedLatitude = latitude;
        this.selectedLongitude = longitude;
        System.out.println("Selected coordinates: " + latitude + "," + longitude);
    }
    
    // Method to save the event with the selected coordinates to the database
    private void saveEventWithCoordinates() {
        Instant instant = ((Date) NewDate.getValue()).toInstant(); // Convert the spinner date to an Instant
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
        LocalDateTime date = zonedDateTime.toLocalDateTime();
        
        EntityManager em = emf.createEntityManager();
        try{
            em.getTransaction().begin();
            Event newEvent = new Event(
            0,
            NewEventName.getText(),
            NewEventDescription.getText(),
            date,
            selectedLatitude,
            selectedLongitude,
            null    //TODO add reverse logic to get address and/or name
            );
            em.persist(newEvent); //persist the event to the database
            em.getTransaction().commit();
            
            //Reset UI after saving
            NewEventName.setText("");
            NewEventDescription.setText("");
            NewEvent.setText("");
            NewEventDescription.setEnabled(false);
            NewEventName.setEnabled(false);
            FC1.updateCalendar(jTable1, jComboBox2, Month.of(jComboBox2.getSelectedIndex() + 1));
        } catch(Exception e){
            em.getTransaction().rollback();
            e.printStackTrace();
            System.out.println("Failed to add event to database");
        } finally {
            em.close();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NewEventName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox1 = new javax.swing.JComboBox<>();
        NewDate = new javax.swing.JSpinner();
        EventName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        EventInfo = new javax.swing.JTextArea();
        NewEvent = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        NewEventDescription = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        NewEventName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewEventNameActionPerformed(evt);
            }
        });
        NewEventName.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                NewEventNameKeyTyped(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
            }
        ));
        jTable1.setCellSelectionEnabled(true);
        jTable1.setShowGrid(true);
        jScrollPane1.setViewportView(jTable1);

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        jComboBox2.setToolTipText("");
        jComboBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox2ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2025", "2026" }));

        NewDate.setModel(new javax.swing.SpinnerDateModel());

        EventInfo.setColumns(20);
        EventInfo.setRows(5);
        jScrollPane2.setViewportView(EventInfo);

        NewEvent.setText("New Event");
        NewEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NewEventMouseClicked(evt);
            }
        });

        NewEventDescription.setColumns(20);
        NewEventDescription.setRows(5);
        jScrollPane3.setViewportView(NewEventDescription);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(NewEvent)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(NewDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(EventName, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(EventName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 462, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(NewEvent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(NewDate, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void NewEventNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewEventNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NewEventNameActionPerformed

    private void NewEventNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NewEventNameKeyTyped

    }//GEN-LAST:event_NewEventNameKeyTyped

    private void jComboBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox2ActionPerformed
        FC1.updateCalendar(jTable1, jComboBox2, Month.of(jComboBox2.getSelectedIndex() + 1));
    }//GEN-LAST:event_jComboBox2ActionPerformed

    private void NewEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewEventMouseClicked
        if (NewEvent.getText().equals("New Event")) {
            NewEventDescription.setEnabled(true);
            NewEventName.setEnabled(true);
            showMapDialog();
        } else if (NewEvent.getText().equals("Add Event"))
            saveEventWithCoordinates();
    }//GEN-LAST:event_NewEventMouseClicked

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea EventInfo;
    private javax.swing.JTextField EventName;
    private javax.swing.JSpinner NewDate;
    private javax.swing.JButton NewEvent;
    private javax.swing.JTextArea NewEventDescription;
    private javax.swing.JTextField NewEventName;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
