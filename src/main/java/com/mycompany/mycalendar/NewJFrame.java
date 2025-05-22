package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Map.MapCallback;
import com.mycompany.mycalendar.Map.MapLoadListener;
import com.mycompany.mycalendar.Map.MapsController;
import com.mycompany.mycalendar.Event.Event;
import com.mycompany.mycalendar.Event.EventController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebView;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ricky
 */
public class NewJFrame extends javax.swing.JFrame implements MapCallback, MapLoadListener {

    boolean inNewEventCreation = false;
    private static boolean alreadyBuilt = false;

    //EntityManagerFactory as a static field to avoid recreating it repeatedly
    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private final FrameController FC1 = FrameController.getInstance();

    // JFXPanel to embed the JavaFX WebView (OSM map) in Swing
    private JFXPanel fxPanel;
    private WebView webView;
    private LocalDateTime clickedDate;

    private final EventController EC1 = new EventController();
    MapsController MC1 = new MapsController();
    private static final NewJFrame instance = new NewJFrame();

    public static NewJFrame getInstance() {
        return instance;
    }

    public void updateCalendar() {
        // Clear the table
        for (int row = 0; row < CalendarJTable.getRowCount(); row++) {
            for (int col = 0; col < 7; col++) {
                ((DefaultTableModel) CalendarJTable.getModel()).setValueAt(null, row, col);
            }
        }

        Month selectedMonth = Month.of(MonthSelectorJComboBox.getSelectedIndex() + 1);
        int daysInMonth = FC1.getNumberOfDays(selectedMonth);

        // Calculate starting day of the week (Monday = 0)
        LocalDateTime firstDayOfMonth = LocalDateTime.of(FC1.getCurrentYear(), selectedMonth, 1, 00, 00);
        int startingDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() - 1;

        int day = 1;
        int row = 0;
        int col = startingDayOfWeek;

        while (day <= daysInMonth) {
            if (col >= 0 && col < 7) {
                ((DefaultTableModel) CalendarJTable.getModel()).setValueAt(day, row, col);
                day++;
            }
            col++;
            if (col >= 7) {
                col = 0;
                row++;
            }
        }
    }

    private NewJFrame() {
        if (!alreadyBuilt) {
            this.mapPanel = new JPanel(new java.awt.BorderLayout());
            initComponents();
            SwingUtilities.invokeLater(()
                    -> updateCalendar()
            );
            addTableClickListener();
            NewEventDescription.setEnabled(false);
            NewEventName.setEnabled(false);
            PreviousMap.setEnabled(false);
            NextMap.setEnabled(false);

            MC1.addMapLoadListener(this);
            initializeMap();

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
            alreadyBuilt = true;
        }
    }

    private void addTableClickListener() {
        CalendarJTable.addMouseListener(new MouseAdapter() {   //extends MouseAdapter
            @Override
            public void mouseClicked(MouseEvent e) {
                if (CalendarJTable.isEnabled()) {
                    /**
                     * This gets the JTable object that was clicked. e.getSource() returns the component that triggered the event, which is the jTable1 in this case.
                     */
                    JTable target = (JTable) e.getSource();

                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();

                    Object value = target.getValueAt(row, column);
                    if (row != -1 && column != -1 && value != null) {
                        System.out.println("Clicked on cell: Row " + row + ", Column " + column + ", Value: " + value);
                        int day = (Integer) value;
                        Month selectedMonth = Month.of(MonthSelectorJComboBox.getSelectedIndex() + 1);
                        int year = Integer.parseInt(YearSelectorJCombobox.getSelectedItem().toString());
                        clickedDate = LocalDateTime.of(year, selectedMonth, day, 00, 00, 00);
                        if(inNewEventCreation)
                            ShowSelectedDate.setText(clickedDate.toString());
                        System.out.println("Data clickata: " + clickedDate);
                        if (!inNewEventCreation) {
                            List<Event> events = FC1.getEventsForDate(clickedDate);
                            updateEventsPanel(events);
                            if (events.isEmpty()) {
                                System.out.println("No events found for date " + clickedDate.toLocalDate());
                                jPanel1.removeAll();
                                jPanel1.revalidate();
                                jPanel1.repaint();
                                mapPanel.setVisible(false);
                            } else {
                                MC1.setMapToCurrentCoordinates(webView);
                                mapPanel.setVisible(true);
                            }
                        }
                    } else
                        mapPanel.setVisible(false);
                }
            }
        });
    }

    // Implementation of MapCallback interface to receive coordinates from the map
    @Override
    public void setCoordinates(double latitude, double longitude) {
        System.out.println("Selected coordinates: " + latitude + "," + longitude);
        MC1.setSelectedLongitude(longitude);
        MC1.setSelectedLatitude(latitude);
        ShowSelectedLocation.setText(MC1.getAddressFromCoordinates(latitude, longitude).getDisplayName());
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
        CalendarJTable = new javax.swing.JTable();
        MonthSelectorJComboBox = new javax.swing.JComboBox<>();
        YearSelectorJCombobox = new javax.swing.JComboBox<>();
        NewEvent = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        NewEventDescription = new javax.swing.JTextArea();
        mapPanel = new javax.swing.JPanel();
        NextMap = new javax.swing.JButton();
        PreviousMap = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        EscBtn = new javax.swing.JButton();
        ShowSelectedDate = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        ShowSelectedLocation = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();

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

        CalendarJTable.setModel(new javax.swing.table.DefaultTableModel(
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
        CalendarJTable.setCellSelectionEnabled(true);
        CalendarJTable.setShowGrid(true);
        jScrollPane1.setViewportView(CalendarJTable);
        CalendarJTable.getColumnModel().getSelectionModel().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        MonthSelectorJComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December" }));
        MonthSelectorJComboBox.setToolTipText("");
        MonthSelectorJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                MonthSelectorJComboBoxActionPerformed(evt);
            }
        });

        YearSelectorJCombobox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2025", "2026" }));

        NewEvent.setText("New Event");
        NewEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NewEventMouseClicked(evt);
            }
        });

        NewEventDescription.setColumns(20);
        NewEventDescription.setRows(5);
        jScrollPane3.setViewportView(NewEventDescription);

        mapPanel.setPreferredSize(new java.awt.Dimension(0, 488));
        mapPanel.setLayout(new java.awt.BorderLayout());

        NextMap.setText("Next");
        NextMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NextMapMouseClicked(evt);
            }
        });
        NextMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NextMapActionPerformed(evt);
            }
        });

        PreviousMap.setText("Previous");
        PreviousMap.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                PreviousMapMouseClicked(evt);
            }
        });

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane4.setViewportView(jPanel1);

        EscBtn.setText("ESC");
        EscBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EscBtnMouseClicked(evt);
            }
        });

        ShowSelectedDate.setEditable(false);

        jLabel1.setText("Event name");

        jLabel2.setText("Event description");

        jLabel3.setText("Selected date");

        jLabel4.setText("Debug Button");

        ShowSelectedLocation.setEditable(false);

        jLabel5.setText("Selected location");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(MonthSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(YearSelectorJCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(380, 380, 380)
                        .addComponent(jLabel4))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(NewEvent)
                        .addGap(285, 285, 285)
                        .addComponent(EscBtn))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(jLabel1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jLabel2))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 450, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 628, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(mapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 1090, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addComponent(PreviousMap, javax.swing.GroupLayout.PREFERRED_SIZE, 530, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(NextMap, javax.swing.GroupLayout.PREFERRED_SIZE, 560, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel3))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(ShowSelectedDate, javax.swing.GroupLayout.PREFERRED_SIZE, 490, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(jLabel5))
            .addGroup(layout.createSequentialGroup()
                .addGap(10, 10, 10)
                .addComponent(ShowSelectedLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 1070, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(MonthSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(YearSelectorJCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel4)
                        .addGap(6, 6, 6)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(NewEvent)
                            .addComponent(EscBtn))
                        .addGap(6, 6, 6)
                        .addComponent(jLabel1)
                        .addGap(6, 6, 6)
                        .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jLabel2)
                        .addGap(6, 6, 6)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 404, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(12, 12, 12)
                .addComponent(mapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 488, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PreviousMap)
                    .addComponent(NextMap))
                .addGap(7, 7, 7)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(ShowSelectedDate, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(3, 3, 3)
                .addComponent(ShowSelectedLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void NewEventNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewEventNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NewEventNameActionPerformed

    private void NewEventNameKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NewEventNameKeyTyped

    }//GEN-LAST:event_NewEventNameKeyTyped

    private void MonthSelectorJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MonthSelectorJComboBoxActionPerformed
        updateCalendar();
    }//GEN-LAST:event_MonthSelectorJComboBoxActionPerformed

    private void NewEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewEventMouseClicked
        inNewEventCreation = true;
        if (!NewEvent.getText().equals("Add Event")) {
            ShowSelectedDate.setEnabled(true);
            NewEventDescription.setEnabled(true);
            NewEventName.setEnabled(true);
            mapPanel.setVisible(true);
        } else {
            EC1.saveEvent(
                    clickedDate, emf, NewEventName, NewEventDescription,
                    MC1.getSelectedLatitude(), MC1.getSelectedLongitude(), NewEvent, CalendarJTable, MonthSelectorJComboBox
            );
            NewEvent.setText("New Event");
            updateCalendar();
            List<Event> events = FC1.getEventsForDate(clickedDate);
            updateEventsPanel(events);
            
            if (!events.isEmpty()) {
                MC1.setMapToCurrentCoordinates(webView);
                mapPanel.setVisible(true);
            } else
                mapPanel.setVisible(false);
            
            ShowSelectedDate.setText(null);
            ShowSelectedDate.setEnabled(false);
            inNewEventCreation = false;
            FC1.getCoordinatesList().clear();
            NewEventDescription.setText(null);
            NewEventName.setText(null);
            NewEventName.setEnabled(false);
            NewEventDescription.setEnabled(false);
        }
    }//GEN-LAST:event_NewEventMouseClicked

    private void NextMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NextMapMouseClicked
        if (NextMap.isEnabled())
            MC1.moveMapNext(webView);
    }//GEN-LAST:event_NextMapMouseClicked

    private void PreviousMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PreviousMapMouseClicked
        if (PreviousMap.isEnabled())
            MC1.moveMapPrevious(webView);
    }//GEN-LAST:event_PreviousMapMouseClicked

    public void resetMap() {
        MC1.setSelectedLongitude(0.0);
        MC1.setSelectedLatitude(0.0);
        PreviousMap.setEnabled(false);
        NextMap.setEnabled(false);
        FC1.getCoordinatesList().clear();
        FC1.resetCoordinates();
    }

    private void EscBtnMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_EscBtnMouseClicked
        updateEventsPanel(null);
        resetMap();
        mapPanel.setVisible(false);
    }//GEN-LAST:event_EscBtnMouseClicked

    private void NextMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NextMapActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_NextMapActionPerformed

    private JPanel createEventPanel(Event event) {
        JPanel panel = new JPanel(new BorderLayout());
        String html = "<html><b>Name:</b> " + event.getName() + "<br>"
                + "<b>Date:</b> " + event.getDate().format(DateTimeFormatter.ofPattern("MMMM d HH:mm")) + "<br>"
                + "<b>Description:</b> " + event.getDescription() + "<br>"
                + "<b>Coordinates:</b> Lat: " + event.getLatitude() + ", Lon: " + event.getLongitude() + "<br>"
                + "<b>Location:</b> " + event.getLocation() + "</html>";
        
        JLabel label = new JLabel(html);
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));  //padding added for readability

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(e -> {
            EC1.deleteEvent(event, emf);
            List<Event> updatedEvents = FC1.getEventsForDate(clickedDate);
            updateEventsPanel(updatedEvents);
            if (updatedEvents.isEmpty())
                mapPanel.setVisible(false);
            else
                MC1.setMapToCurrentCoordinates(webView);
        });

        //wrapper panel for the delete button to prevent stretching
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 50));
        buttonPanel.add(deleteButton);
        deleteButton.setAlignmentY(Component.TOP_ALIGNMENT);

        panel.add(label, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.EAST);
        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return panel;
    }

    private void updateEventsPanel(List<Event> events) {
        jPanel1.removeAll();
        if (events != null)
            for (Event event : events) {
                JPanel eventPanel = createEventPanel(event);
                jPanel1.add(eventPanel);
            }
        jPanel1.revalidate();
        jPanel1.repaint();

    }

    public FrameController getFC1() {
        return FC1;
    }

    public void enableCalendar() {
        SwingUtilities.invokeLater(() -> {
            CalendarJTable.setEnabled(true);
            CalendarJTable.setVisible(true);
            System.out.println("Calendar enabled: " + CalendarJTable.isEnabled());
        });
    }

    @Override
    public void onMapLoaded() {
        System.out.println("Map loaded, processing queued actions");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable CalendarJTable;
    private javax.swing.JButton EscBtn;
    private javax.swing.JComboBox<String> MonthSelectorJComboBox;
    private javax.swing.JButton NewEvent;
    private javax.swing.JTextArea NewEventDescription;
    private javax.swing.JTextField NewEventName;
    private javax.swing.JButton NextMap;
    private javax.swing.JButton PreviousMap;
    private javax.swing.JTextField ShowSelectedDate;
    private javax.swing.JTextField ShowSelectedLocation;
    private javax.swing.JComboBox<String> YearSelectorJCombobox;
    private javax.swing.JButton jButton1;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel mapPanel;
    // End of variables declaration//GEN-END:variables
}
