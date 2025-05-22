package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Map.MapCallback;
import com.mycompany.mycalendar.Map.MapLoadListener;
import com.mycompany.mycalendar.Map.MapsController;
import com.mycompany.mycalendar.Event.Event;
import com.mycompany.mycalendar.Event.EventController;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
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

    private NewJFrame() {
        if (!alreadyBuilt) {
            this.mapPanel = new JPanel(new java.awt.BorderLayout());
            initComponents();
            
            setupEscapeKeyBinding();
            
            SwingUtilities.invokeLater(()
                    -> updateCalendar()
            );
            addTableClickListener();

            mapPanel.setVisible(false);

            //set components to their initial state
            componentsSetEnabled(false);

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
                        if (inNewEventCreation)
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
        MC1.setSelectedLatitude(latitude);
        MC1.setSelectedLongitude(longitude);
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
        ShowSelectedLocation = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

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

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane4.setViewportView(jPanel1);

        EscBtn.setText("ESC");
        EscBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                EscBtnMouseClicked(evt);
            }
        });
        EscBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EscBtnActionPerformed(evt);
            }
        });
        EscBtn.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                EscBtnKeyPressed(evt);
            }
        });

        ShowSelectedDate.setEditable(false);
        ShowSelectedDate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ShowSelectedDateActionPerformed(evt);
            }
        });

        ShowSelectedLocation.setEditable(false);

        jLabel1.setText("Selected date");

        jLabel2.setText("Selected location");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(NewEvent)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(EscBtn))
                                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(MonthSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(YearSelectorJCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(12, 12, 12))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(27, 27, 27)))
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 699, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(PreviousMap, javax.swing.GroupLayout.PREFERRED_SIZE, 549, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(NextMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(mapPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(ShowSelectedDate, javax.swing.GroupLayout.PREFERRED_SIZE, 169, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ShowSelectedLocation, javax.swing.GroupLayout.PREFERRED_SIZE, 1066, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2))
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(MonthSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(YearSelectorJCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(EscBtn)
                            .addComponent(NewEvent))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 342, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(58, 58, 58)
                .addComponent(mapPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 448, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ShowSelectedDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(jLabel2)
                .addGap(7, 7, 7)
                .addComponent(ShowSelectedLocation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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

    private void MonthSelectorJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_MonthSelectorJComboBoxActionPerformed
        updateCalendar();
    }//GEN-LAST:event_MonthSelectorJComboBoxActionPerformed

    private void componentsSetEnabled(boolean setEnabled) {
        //mapPanel is not included in this method since it follows a different logic and flow from these components
        ShowSelectedDate.setEnabled(setEnabled);
        ShowSelectedLocation.setEnabled(setEnabled);
        NewEventDescription.setEnabled(setEnabled);
        NewEventDescription.setText(null);
        NewEventName.setEnabled(setEnabled);
        NewEventName.setText(null);
        if (!inNewEventCreation) {
            PreviousMap.setEnabled(setEnabled);
            NextMap.setEnabled(setEnabled);
        }
    }

    private void NewEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewEventMouseClicked
        inNewEventCreation = true;

        if (NewEvent.getText().equals("Add Event")) {
            //EVENT ADDING
            EC1.saveEvent(clickedDate, emf, NewEventName, NewEventDescription,
                    MC1.getSelectedLatitude(), MC1.getSelectedLongitude(), NewEvent, CalendarJTable, MonthSelectorJComboBox);

            //showing the just-added event
            List<Event> events = FC1.getEventsForDate(clickedDate);
            updateEventsPanel(events);

            //set components to their previous state
            componentsSetEnabled(false);
            inNewEventCreation = false;
            ShowSelectedDate.setText(null);
            ShowSelectedLocation.setText(null);
            NewEvent.setText("New Event");
        } else {
            mapPanel.setVisible(true);
            componentsSetEnabled(true);
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
        /*if(inNewEventCreation){
            //set components to their previous state
            componentsSetEnabled(false);
            inNewEventCreation = false;
            ShowSelectedDate.setText(null);
            ShowSelectedLocation.setText(null);
            inNewEventCreation = false;
            mapPanel.setVisible(false);
            NewEvent.setText("New Event");
            return;
        }
        
        updateEventsPanel(null);
        resetMap();
        mapPanel.setVisible(false);*/
    }//GEN-LAST:event_EscBtnMouseClicked

    private void ShowSelectedDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ShowSelectedDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ShowSelectedDateActionPerformed

    private void EscBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EscBtnActionPerformed
        if(inNewEventCreation){
            //set components to their previous state
            componentsSetEnabled(false);
            inNewEventCreation = false;
            ShowSelectedDate.setText(null);
            ShowSelectedLocation.setText(null);
            inNewEventCreation = false;
            mapPanel.setVisible(false);
            NewEvent.setText("New Event");
            return;
        }
        
        updateEventsPanel(null);
        resetMap();
        mapPanel.setVisible(false);
    }//GEN-LAST:event_EscBtnActionPerformed

    private void EscBtnKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_EscBtnKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_EscBtnKeyPressed

    private JPanel createEventPanel(Event event) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteButton);

        panel.add(label);
        panel.add(buttonPanel);
        
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
    
    private void setupEscapeKeyBinding() {
        /* 
        Get the InputMap for the content pane of the frame.
        JComponent.WHEN_IN_FOCUSED_WINDOW means the action will be triggered
        when the window is focused, regardless of which component has focus.
        */
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        
        // Get the ActionMap for the content pane.
        ActionMap actionMap = getRootPane().getActionMap();

        // Define the KeyStroke for the Escape key.
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);

        // Map the KeyStroke to a unique String identifier.
        String escapeActionKey = "pressEscapeButton";
        inputMap.put(escapeKeyStroke, escapeActionKey);

        // Map the String identifier to an AbstractAction.
        actionMap.put(escapeActionKey, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Simulate a click on the EscBtn when the Escape key is pressed.
                EscBtn.doClick();
            }
        });
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JPanel mapPanel;
    // End of variables declaration//GEN-END:variables
}
