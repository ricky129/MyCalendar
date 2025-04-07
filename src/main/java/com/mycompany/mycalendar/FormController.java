package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Event.Event;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ricky
 */
public class FormController {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private int currentYear = Year.now().getValue();
    private List moreCoodinatesList = new ArrayList<>();
    private int counter = 0;

    public FormController() {
    }
    
    public int getCounter() {
        return counter;
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public List getMoreCoodinatesList() {
        return moreCoodinatesList;
    }
    
    public double getCoordinatesTemp(){
        double ret = 0;
        if (counter < moreCoodinatesList.size()){
            ret = (double) moreCoodinatesList.get(counter);
            counter ++;
        }
        return ret;
    }

    private boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    public int getNumberOfDays(Month month) {
        return month.length(isLeapYear(currentYear));
    }

    public void updateCalendar(JTable JTB1, JComboBox JCB1, Month month) {
        // Clear the table
        for (int row = 0; row < JTB1.getRowCount(); row++) {
            for (int col = 0; col < 7; col++) {
                ((DefaultTableModel) JTB1.getModel()).setValueAt(null, row, col);
            }
        }

        Month selectedMonth = Month.of(JCB1.getSelectedIndex() + 1);
        int daysInMonth = getNumberOfDays(selectedMonth);

        // Calculate starting day of the week (Monday = 0)
        LocalDateTime firstDayOfMonth = LocalDateTime.of(currentYear, selectedMonth, 1, 00, 00);
        int startingDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() - 1;

        int day = 1;
        int row = 0;
        int col = startingDayOfWeek;

        while (day <= daysInMonth) {
            if (col >= 0 && col < 7) {
                ((DefaultTableModel) JTB1.getModel()).setValueAt(day, row, col);
                day++;
            }
            col++;
            if (col >= 7) {
                col = 0;
                row++;
            }
        }
    }
    
    public boolean updateInfoBox(LocalDateTime dateFromUser, JTextField EventName, JTextArea EventInfo) {
        boolean ret = false;
        String eventInfoTemp;
        EntityManager em = emf.createEntityManager();

        try {
            LocalDateTime startOfDay = dateFromUser.truncatedTo(ChronoUnit.DAYS);// e.g., 2025-01-01 00:00:00
            LocalDateTime endOfDay = startOfDay.plusDays(1);
            
            TypedQuery<Event> query = em.createQuery(
                "SELECT e FROM Event e WHERE e.date >= :start AND e.date < :end",
                Event.class
            );

            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);
            System.out.println("Querying with dateFromUser: " + dateFromUser);
            var events = query.getResultList();
            System.out.println(events.toString());

            if (events == null || events.isEmpty()) {
                System.out.println("No events found for date " + dateFromUser.toLocalDate());
                return false;
            }

            for (Event event : events) {
                LocalDateTime date = event.getDate();
                if (date.toLocalDate().equals(date.toLocalDate())) {

                    switch (EventName.getText()) {
                        case "" -> {
                            EventName.setText(event.getName());

                            StringBuilder sb = new StringBuilder();
                            sb.append(date.getMonth())
                                    .append(" ")
                                    .append(date.getDayOfMonth())
                                    .append(" ")
                                    .append(date.getHour())
                                    .append(":")
                                    .append(date.getMinute())
                                    .append("\n")
                                    .append(event.getDescription())
                                    .append("\n")
                                    .append("Lat: ").append(event.getLatitude())
                                    .append(", Lon: ").append(event.getLongitude())
                                    .append("\n");
                            
                            moreCoodinatesList.add(event.getLatitude());
                            moreCoodinatesList.add(event.getLongitude());
                            EventInfo.setText(sb.toString());
                            ret = true;
                        }
                        case "More events" -> {
                            StringBuilder sb = new StringBuilder();
                            sb.append("\n")
                                    .append(event.getName())
                                    .append("\n")
                                    .append(date.getMonth())
                                    .append(" ")
                                    .append(date.getDayOfMonth())
                                    .append(" ")
                                    .append(date.getHour())
                                    .append(":")
                                    .append(date.getMinute())
                                    .append("\n")
                                    .append(event.getDescription())
                                    .append("\n")
                                    .append("Lat: ").append(event.getLatitude())
                                    .append(", Lon: ").append(event.getLongitude())
                                    .append("\n");

                            moreCoodinatesList.add(event.getLatitude());
                            moreCoodinatesList.add(event.getLongitude());
                            EventInfo.append(sb.toString());
                            ret = true;
                        }
                        default -> {
                            eventInfoTemp = EventInfo.getText();

                            StringBuilder sb = new StringBuilder();
                            sb.append(EventName.getText())
                                    .append("\n")
                                    .append(eventInfoTemp)
                                    .append("\n")
                                    .append(event.getName())
                                    .append("\n")
                                    .append(date.getMonth())
                                    .append(" ")
                                    .append(date.getDayOfMonth())
                                    .append(" ")
                                    .append(date.getHour())
                                    .append(":")
                                    .append(date.getMinute())
                                    .append("\n")
                                    .append(event.getDescription())
                                    .append("\n")
                                    .append("Lat: ").append(event.getLatitude())
                                    .append(", Lon: ").append(event.getLongitude())
                                    .append("\n");

                            moreCoodinatesList.add(event.getLatitude());
                            moreCoodinatesList.add(event.getLongitude());
                            EventInfo.setText(sb.toString());
                            EventName.setText("More events");
                            ret = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return ret;
    }
}
