package com.mycompany.mycalendar;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
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
            //Query events for the given date, ignoring time
            TypedQuery<Event> query = em.createQuery(
                    //FUNCTION('DATE', ...) to extract only the date part from the date column in the database, matching it with date.toLocalDate()
                    "SELECT e FROM Event e WHERE FUNCTION('DATE', e.date) = :date",
                    Event.class
            );
            query.setParameter("date", dateFromUser);
            var events = query.getResultList();

            if (events == null || events.isEmpty()) {
                System.out.print("No events found for date " + dateFromUser.toLocalDate());
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
                                    .append("\n");

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
                                    .append(event.getDescription());

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
                                    .append("\n");

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
