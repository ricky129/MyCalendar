package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Event.EventController;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.Month;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebView;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 *
 * @author ricky
 */
public class NewJFrame extends javax.swing.JFrame implements MapCallback {

    //EntityManagerFactory as a static field to avoid recreating it repeatedly
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private final FormController FC1 = FormController.getInstance();
    // JFXPanel to embed the JavaFX WebView (OSM map) in Swing
    private JFXPanel fxPanel;
    private WebView webView;
    private LocalDateTime clickedDate;

    private final EventController EC1 = new EventController();

    MapsController MC1 = new MapsController();

    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
        this.mapPanel = new JPanel(new java.awt.BorderLayout());
        initComponents();
        SwingUtilities.invokeLater(()
                -> FC1.updateCalendar(jTable1, jComboBox2, Month.of(jComboBox2.getSelectedIndex() + 1))
        );
        addTableClickListener();
        NewEventDescription.setEnabled(false);
        NewEventName.setEnabled(false);
        PreviousMap.setEnabled(false);
        NextMap.setEnabled(false);
        initializeMap();

        //int moreCoordinatesListIndex listener
        FC1.addCoordinatesListListener((size, currentIndex) -> {
            SwingUtilities.invokeLater(() -> {
                NextMap.setEnabled(FC1.hasNextCoordinates());
                PreviousMap.setEnabled(FC1.hasPreviousCoordinates());
            });
        });

        //JTextField NewEventDescription listener
        NewEventDescription.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                NewEvent.setText("Add Event");
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                NewEvent.setText(!NewEventDescription.getText().isEmpty() ? "Add Event" : "New Event");
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                NewEvent.setText(!NewEventDescription.getText().isEmpty() ? "Add Event" : "New Event");
            }
        });
    }

    private void addTableClickListener() {
        jTable1.addMouseListener(new MouseAdapter() {   //extends MouseAdapter
            @Override
            public void mouseClicked(MouseEvent e) {
                /*This gets the JTable object that was clicked.  
                e.getSource() returns the component that triggered 
                the event, which is the jTable1 in this case.
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
                    clickedDate = LocalDateTime.of(year, selectedMonth, day, 00, 00, 00);
                    System.out.println("Data clickata: " + clickedDate);
                    if (!FC1.updateInfoBox(clickedDate, EventName, EventInfo)) {
                        System.out.println("No events found for date " + clickedDate.toLocalDate());
                        EventName.setText("");
                        EventInfo.setText("No event found!");
                        mapPanel.setVisible(false);
                    } else{
                        MC1.moveMapNext(webView);
                        mapPanel.setVisible(true);
                    }
                }
            }
        });
    }
    
    // Implementation of MapCallback interface to receive coordinates from the map
    @Override
    public void setCoordinates(double latitude, double longitude) {
        System.out.println("Selected coordinates: " + latitude + "," + longitude);
    }

    // Method to initialize the JavaFX WebView with the OSM map
    private void initializeMap() {
        fxPanel = new JFXPanel();
        Platform.runLater(() -> {
            webView = new WebView();
            MC1.initializeMap(webView, fxPanel, mapPanel, this);
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jButton1 = new javax.swing.JButton();
        NewEventName = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox1 = new javax.swing.JComboBox<>();
        EventName = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        EventInfo = new javax.swing.JTextArea();
        NewEvent = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        NewEventDescription = new javax.swing.JTextArea();
        mapPanel = new javax.swing.JPanel();
        NextMap = new javax.swing.JButton();
        PreviousMap = new javax.swing.JButton();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        jButton1.setText("jButton1");

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

        mapPanel.setLayout(new java.awt.BorderLayout());

        NextMap.setText("Next");
        NextMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NextMapMouseClicked(evt);
            }
        });

        PreviousMap.setText("Previous");
        PreviousMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PreviousMapMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(PreviousMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NextMap, javax.swing.GroupLayout.PREFERRED_SIZE, 429, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(mapPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(NewEvent)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(EventName, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE))))
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
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(NewEvent)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 335, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(33, 33, 33)
                .addComponent(mapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NextMap)
                    .addComponent(PreviousMap))
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
            mapPanel.setVisible(true);
        } else if (NewEvent.getText().equals("Add Event")) {
            EC1.saveEventWithCoordinates(clickedDate, emf, NewEventName, NewEventDescription, 
                MC1.getSelectedLatitude(), MC1.getSelectedLongitude(), NewEvent, jTable1, jComboBox2);
            NewEvent.setText("New Event");
            if (!FC1.updateInfoBox(clickedDate, EventName, EventInfo)) {
                EventName.setText("");
                EventInfo.setText("No event found!");
                mapPanel.setVisible(false);
            } else {
                MC1.moveMapNext(webView);
                mapPanel.setVisible(true);
            }
        }
    }//GEN-LAST:event_NewEventMouseClicked

    private void NextMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NextMapMouseClicked
        if (NextMap.isEnabled()) {
            MC1.moveMapNext(webView);
        }
    }//GEN-LAST:event_NextMapMouseClicked

    private void PreviousMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PreviousMapMouseClicked
        if (PreviousMap.isEnabled()) {
            MC1.moveMapPrevious(webView);
        }
    }//GEN-LAST:event_PreviousMapMouseClicked

    public String getEventName() {
        return EventName.getText();
    }

    public FormController getFC1() {
        return FC1;
    }
    /**
     * @param args the command line arguments
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea EventInfo;
    private javax.swing.JTextField EventName;
    private javax.swing.JButton NewEvent;
    private javax.swing.JTextArea NewEventDescription;
    private javax.swing.JTextField NewEventName;
    private javax.swing.JButton NextMap;
    private javax.swing.JButton PreviousMap;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel mapPanel;
    // End of variables declaration//GEN-END:variables
}
