package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Map.MapCallback;
import com.mycompany.mycalendar.Map.MapLoadListener;
import com.mycompany.mycalendar.Map.MapsController;
import com.mycompany.mycalendar.Event.Event;
import com.mycompany.mycalendar.Event.EventDAOImpl;
import com.mycompany.mycalendar.JSON.JSONResponse;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.web.WebView;
import javax.persistence.EntityManagerFactory;
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
import netscape.javascript.JSException;
import netscape.javascript.JSObject;

/**
 *
 * @author ricky
 */
public class NewJFrame extends javax.swing.JFrame implements MapCallback, MapLoadListener {

    private Month selectedMonth;
    private Year selectedYear;

    private boolean inNewEventCreation = false;

    private static final EntityManagerFactory emf = MyCalendar.getEmf();
    private final FrameController FC1 = FrameController.getInstance();

    // JFXPanel to embed the JavaFX WebView (OSM map) in Swing
    private JFXPanel fxPanel;
    private WebView webView;
    private LocalDateTime PreviousclickedDate = null;
    private LocalDateTime clickedDateChecked = null;

    private final MapsController MC1;
    private static final NewJFrame instance = new NewJFrame();

    private final EventDAOImpl EDI1;

    /**
     * Returns the singleton instance of NewJFrame.
     *
     * @return The NewJFrame instance.
     */
    public static NewJFrame getInstance() {
        return instance;
    }

    /**
     * Private constructor for the Singleton pattern. Initializes components and sets up listeners.
     */
    private NewJFrame() {
        this.MC1 = MapsController.getInstance();
        this.EDI1 = EventDAOImpl.getInstance();
        
        this.mapPanel = new JPanel(new java.awt.BorderLayout());
        initComponents();
        
        /**
         * A row is added to the calendar table. This is so much 
         * less hassle than creating a new model just to use the
         * same with one more row. This edit is necessary for months
         * which span to more than 5 weeks, like March 2026.
         */
        ((DefaultTableModel) CalendarJTable.getModel()).setRowCount(6);

        setupEscapeKeyBinding();

        SwingUtilities.invokeLater(()
                -> updateCalendar()
        );

        addTableClickListener();

        selectedMonth = Month.of(MonthSelectorJComboBox.getSelectedIndex() + 1);
        selectedYear = Year.of(YearSelectorJComboBox.getSelectedIndex() + 1);

        mapPanel.setVisible(false);

        //set components to their initial state
        componentsSetEnabled(false);

        SwingUtilities.invokeLater(() -> {
            MC1.addMapLoadListener(this);
            initializeMap();
            updateCalendar();
        });

        /**
         * Adds a listener to the FrameController's coordinates list. This listener is invoked when the size or current index of the coordinates list changes (when MapsController.notifyListeners() is called), which in turn updates the enabled state of the "Next Map" and "Previous Map" buttons.
         *
         * @param size The current size of the coordinates list.
         * @param currentIndex The current index in the coordinates list.
         */
        FC1.addCoordinatesListListener((size, currentIndex) -> {
            SwingUtilities.invokeLater(() -> {
                NextMap.setEnabled(FC1.hasNextCoordinates());
                PreviousMap.setEnabled(FC1.hasPreviousCoordinates());
            });
        });

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

    /**
     * Updates the calendar table with days for the currently selected month and year.
     */
    public void updateCalendar() {
        // Clear the table
        for (int row = 0; row < CalendarJTable.getRowCount(); row++) {
            for (int col = 0; col < 7; col++) {
                ((DefaultTableModel) CalendarJTable.getModel()).setValueAt(null, row, col);
            }
        }
        
        int daysInMonth = FC1.getNumberOfDays(selectedMonth, selectedYear);

        // Calculate starting day of the week (Monday = 0)
        LocalDateTime firstDayOfMonth = LocalDateTime.of(selectedYear.getValue(), selectedMonth, 1, 00, 00);
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

    private void MonthSelectorJComboBoxActionPerformed() {
        selectedMonth = Month.of(MonthSelectorJComboBox.getSelectedIndex() + 1);
        SwingUtilities.invokeLater(()
                -> updateCalendar()
        );
    }

    /**
     * Adds a mouse listener to the calendar table to handle cell clicks.
     */
    private void addTableClickListener() {
        CalendarJTable.addMouseListener(new MouseAdapter() {   //extends MouseAdapter
            @Override
            public void mouseClicked(MouseEvent e) {
                if (CalendarJTable.isEnabled()) {
                    /**
                     * Gets the JTable object that was clicked. e.getSource() returns the component that triggered the event, which is the jTable1 in this case.
                     */
                    JTable target = (JTable) e.getSource();

                    int row = target.getSelectedRow();
                    int column = target.getSelectedColumn();

                    Object value = target.getValueAt(row, column);
                    if (row != -1 && column != -1 && value != null) {
                        System.out.println("Clicked on cell: Row " + row + ", Column " + column + ", Value: " + value);
                        int day = (Integer) value;
                        Month selectedMonth = Month.of(MonthSelectorJComboBox.getSelectedIndex() + 1);
                        int year = Integer.parseInt(YearSelectorJComboBox.getSelectedItem().toString());

                        // Create the new clicked date
                        LocalDateTime newClickedDate = LocalDateTime.of(year, selectedMonth, day, 0, 0, 0);
                        System.out.println("Clicked date: " + newClickedDate);

                        // Check if the clicked date is the same as the currently selected date
                        if (clickedDateChecked != null && newClickedDate.equals(clickedDateChecked) && !inNewEventCreation) {
                            System.out.println("Same date clicked again: " + newClickedDate + ", ignoring click");
                            return; // Ignore repeated clicks
                        }

                        // Update date tracking
                        PreviousclickedDate = clickedDateChecked; // Save the previous date
                        clickedDateChecked = newClickedDate; // Set the current date as selected

                        // Update UI with the selected date
                        if (inNewEventCreation) {
                            System.out.println("Updating ShowSelectedDate to: " + clickedDateChecked);
                            ShowSelectedDate.setText(clickedDateChecked.toString());
                        }

                        System.out.println("Selected date: " + clickedDateChecked + ", Previous date: " + PreviousclickedDate);
                        if (inNewEventCreation)
                            ShowSelectedDate.setText(clickedDateChecked.toString());

                        if (!inNewEventCreation) {

                            List<Event> events = EDI1.getEvents(clickedDateChecked);
                            if (events == null) {
                                System.err.println("Event fetching failed.");
                                return;
                            }

                            updateEventsPanel(events);
                        }
                    } else
                        mapPanel.setVisible(false);
                }
            }
        });
    }

    /**
     * Implementation of MapCallback interface to receive coordinates from the map. This method uses a JavaFX Task to fetch the address asynchronously to prevent UI freezing.
     *
     * @param latitude The selected latitude.
     * @param longitude The selected longitude.
     */
    @Override
    public void setCoordinates(double latitude, double longitude) {
        System.out.println("Selected coordinates: " + latitude + ", " + longitude);

        if (inNewEventCreation) {
            ShowSelectedLocation.setText("Fetching location...");
            MC1.setSelectedLatitude(latitude);
            MC1.setSelectedLongitude(longitude);

            //Create a Task to fetch the address in a background thread
            Task<JSONResponse> fetchAddressTask = MC1.createFetchAddressTask(latitude, longitude);

            //Run the task on a new background thread
            new Thread(fetchAddressTask).start();

            //Define what happens when the task succeeds (on JavaFX Application Thread)
            fetchAddressTask.setOnSucceeded(e -> {
                JSONResponse response = fetchAddressTask.getValue();
                if (response != null && response.getDisplayName() != null) {
                    //Update the Swing UI component on the Swing Event Dispatch Thread
                    SwingUtilities.invokeLater(() -> {
                        ShowSelectedLocation.setText(response.getDisplayName());
                    });
                    System.out.println("Fetched address: " + response.getDisplayName());
                } else {
                    SwingUtilities.invokeLater(() -> {
                        ShowSelectedLocation.setText("Address not found.");
                    });
                    System.err.println("Address not found for coordinates: " + latitude + ", " + longitude);
                }
            });

            //Define what happens if the task fails (on JavaFX Application Thread)
            fetchAddressTask.setOnFailed(e -> {
                Throwable cause = fetchAddressTask.getException();
                System.out.println("Failed to fetch address: " + cause.getMessage());
                SwingUtilities.invokeLater(() -> {
                    ShowSelectedLocation.setText("Error fetching address");
                });
            });
        }
    }

    /**
     * Initializes the JavaFX WebView component for displaying the map.
     */
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
        YearSelectorJComboBox = new javax.swing.JComboBox<>();
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
                NewEvent.doClick();
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
                MonthSelectorJComboBoxActionPerformed();
            }
        });

        YearSelectorJComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "2025", "2026" }));
        YearSelectorJComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                YearSelectorJComboBoxActionPerformed(evt);
            }
        });

        NewEvent.setText("New Event");
        NewEvent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                NewEventMouseClicked(evt);
            }
        });
        NewEvent.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                NewEventActionPerformed(evt);
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
                EscBtn.doClick();
            }
        });
        EscBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                EscBtnActionPerformed(evt);
            }
        });

        ShowSelectedDate.setEditable(false);

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
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(NewEvent)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(EscBtn))
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createSequentialGroup()
                                    .addComponent(MonthSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(YearSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addComponent(NewEventName, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12)
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
                            .addComponent(YearSelectorJComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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

    /**
     * Handles mouse click events on the "New Event" button. Toggles between "New Event" and "Add Event" modes and manages event saving.
     *
     * @param evt The MouseEvent.
     */
    private void NewEventMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NewEventMouseClicked
        if (NewEvent.getText().equals("Add Event")) {

            if (!EDI1.save(clickedDateChecked, emf, NewEventName, NewEventDescription)) {
                System.err.println("Event saving failed.");
                return;
            }

            List<Event> events = EDI1.getEvents(clickedDateChecked);

            if (events == null) {
                System.err.println("Event fetching failed.");
                return;
            }

            //showing the just-added event
            updateEventsPanel(events);

            //set components to their previous state
            componentsSetEnabled(false);
            inNewEventCreation = false;
            NewEvent.setText("New Event");
            resetMap();
        } else {
            inNewEventCreation = true;
            componentsSetEnabled(true);
            if (MC1.isMapLoaded())
                mapPanel.setVisible(true);
        }
    }//GEN-LAST:event_NewEventMouseClicked

    /**
     * Handles mouse click events on the "Next Map" button. Moves the map to the next set of coordinates if enabled.
     *
     * @param evt The MouseEvent.
     */
    private void NextMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_NextMapMouseClicked
        if (NextMap.isEnabled())
            MC1.moveMapNext(webView);
    }//GEN-LAST:event_NextMapMouseClicked

    /**
     * Handles mouse click events on the "Previous Map" button. Moves the map to the previous set of coordinates if enabled.
     *
     * @param evt The MouseEvent.
     */
    private void PreviousMapMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_PreviousMapMouseClicked
        if (PreviousMap.isEnabled())
            MC1.moveMapPrevious(webView);
    }//GEN-LAST:event_PreviousMapMouseClicked

    /**
     * Resets the map's selected coordinates and disables navigation buttons.
     */
    public void resetMap() {
        MC1.setSelectedLatitude(51.505);
        MC1.setSelectedLongitude(-0.09);
        /*PreviousMap.setEnabled(false);
        NextMap.setEnabled(false);*/
        FC1.getCoordinatesList().clear();
        FC1.resetCoordinates();
    }

    /**
     * Enables or disables various UI components related to event creation and map navigation. mapPanel is not included in this method since it follows a different logic and flow from these components
     *
     * @param setEnabled true to enable, false to disable.
     */
    private void componentsSetEnabled(boolean setEnabled) {
        ShowSelectedDate.setEnabled(setEnabled);
        ShowSelectedDate.setText(null);

        ShowSelectedLocation.setEnabled(setEnabled);
        ShowSelectedLocation.setText(null);

        NewEventDescription.setEnabled(setEnabled);
        NewEventDescription.setText(null);

        NewEventName.setEnabled(setEnabled);
        NewEventName.setText(null);

        if (!inNewEventCreation) {
            PreviousMap.setEnabled(setEnabled);
            NextMap.setEnabled(setEnabled);
        }
    }

    /**
     * Handles action events on the "ESC" button. Exits new event creation mode or clears event display.
     *
     * @param evt The ActionEvent.
     */
    private void EscBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EscBtnActionPerformed
        if (inNewEventCreation) {
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
    }//GEN-LAST:event_EscBtnActionPerformed

    private void NewEventActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_NewEventActionPerformed

    }//GEN-LAST:event_NewEventActionPerformed

    private void YearSelectorJComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_YearSelectorJComboBoxActionPerformed
        selectedYear = Year.of(Integer.parseInt(YearSelectorJComboBox.getSelectedItem().toString()));
        SwingUtilities.invokeLater(()
                -> updateCalendar()
        );
    }//GEN-LAST:event_YearSelectorJComboBoxActionPerformed

    /**
     * Creates a JPanel displaying details of a single event, including a delete button.
     *
     * @param event The Event object to display.
     * @return A JPanel representing the event.
     */
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
            if (!EDI1.delete(event, emf)) {
                System.err.println("Event removing failed.");
                return;
            }

            List<Event> updatedEvents = EDI1.getEvents(clickedDateChecked);

            if (updatedEvents == null) {
                System.err.println("Event fetching failed.");
                return;
            }

            updateEventsPanel(updatedEvents);
        });

        //wrapper panel for the delete button to prevent stretching
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(deleteButton);

        panel.add(label);
        panel.add(buttonPanel);

        panel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return panel;
    }

    /**
     * Updates the panel that displays events. Clears existing events and adds new ones.
     *
     * @param events The list of events to display. If null, the panel is cleared.
     */
    private void updateEventsPanel(List<Event> events) {
        jPanel1.removeAll();
        if (events != null && !events.isEmpty()) {

            for (Event event : events) {
                JPanel eventPanel = createEventPanel(event);
                jPanel1.add(eventPanel);
            }

            MC1.setMapToCurrentCoordinates(webView);
            mapPanel.setVisible(true);

        } else {
            System.out.println("No events found for date " + clickedDateChecked.toLocalDate());
            jPanel1.removeAll();
            mapPanel.setVisible(false);
        }

        jPanel1.revalidate();
        jPanel1.repaint();
    }

    /**
     * Callback method from MapLoadListener, invoked when the map is loaded.
     */
    @Override
    public void onMapLoaded() {
        System.out.println("Map loaded, processing queued actions if any");
        if (inNewEventCreation)
            SwingUtilities.invokeLater(() -> {
                mapPanel.setVisible(true);
                MC1.setMapToCurrentCoordinates(webView);
            });
    }

    /**
     * Sets up a key binding for the Escape key to simulate a click on the "EscBtn".
     */
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

    public void invalidateMapSize() {
        Platform.runLater(() -> {
            if (webView != null && MC1.isMapLoaded()) // isMapLoaded should be true when map.html is fully loaded
                try {
                    JSObject jsWindow = (JSObject) webView.getEngine().executeScript("window");
                    jsWindow.eval("map.invalidateSize(true); console.log('Invalidating map size from Java method call');");
                } catch (JSException ex) {
                    System.err.println("Error calling JavaScript invalidateSize: " + ex.getMessage());
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
    private javax.swing.JComboBox<String> YearSelectorJComboBox;
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
