package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Event.Event;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
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
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author ricky
 */
public class FrameController {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private static final FrameController instance = new FrameController();
    private final int currentYear = Year.now().getValue();
    private final List<Double> moreCoordinatesList = new ArrayList<>();
    private final List<CoordinatesListListener> listeners = new ArrayList<>();
    private int MoreCoordinatesCurrentIndex = -1;

    public int getMoreCoordinatesCurrentIndex(int modifier) {
        MoreCoordinatesCurrentIndex += modifier;
        System.out.println("MoreCoordinatesCurrentIndex: " + MoreCoordinatesCurrentIndex + ", retrieved: " + moreCoordinatesList.get(MoreCoordinatesCurrentIndex));
        /*(MoreCoordinatesCurrentIndex >= 0 && MoreCoordinatesCurrentIndex < moreCoordinatesList.size() ? 
             moreCoordinatesList.get(MoreCoordinatesCurrentIndex) : "out of bounds"));*/
        notifyListeners(); // Notify listeners after index change
        return MoreCoordinatesCurrentIndex;
    }

    public void setMoreCoordinatesCurrentIndex(int MoreCoordinatesCurrentIndex) {
        this.MoreCoordinatesCurrentIndex = MoreCoordinatesCurrentIndex;
        notifyListeners();
    }

    private FrameController() {
    }

    public static FrameController getInstance() {
        return instance;
    }

    public void addCoordinatesListListener(CoordinatesListListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeCoordinatesListListener(CoordinatesListListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    private void notifyListeners() {
        synchronized (listeners) {
            int size = moreCoordinatesList.size();
            for (CoordinatesListListener listener : listeners) {
                listener.onCoordinatesListChanged(size, MoreCoordinatesCurrentIndex);
            }
        }
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public List<Double> getMoreCoordinatesList() {
        return moreCoordinatesList;
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

    public boolean updateInfoBox(LocalDateTime dateFromUser, JTextField eventName, JTextArea eventInfo) {
        EntityManager em = emf.createEntityManager();

        synchronized (moreCoordinatesList) {
            moreCoordinatesList.clear();
        }
        MoreCoordinatesCurrentIndex = -1;

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

            List<Event> events = query.getResultList();
            System.out.println(events.toString());

            /*
            synchronized (moreCoordinatesList) {
                moreCoordinatesList.clear();
                MoreCoordinatesCurrentIndex = -1;
                notifyListeners();
            }*/
            if (events.isEmpty())
                return false;

            // Date formatter for consistent output
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d HH:mm");

            // Process events based on eventNameField content
            String eventNameText = eventName.getText().trim();
            boolean isFirstEvent = eventNameText.isEmpty();
            boolean isMoreEvents = eventNameText.equals("More events");

            StringBuilder sb = new StringBuilder();
            if (!isFirstEvent && !isMoreEvents)
                // Preserve existing content for default case
                sb.append(eventNameText).append("\n").append(eventInfo.getText()).append("\n");

            for (Event event : events) {
                LocalDateTime date = event.getDate();

                sb.append("\n")
                        .append(event.getName())
                        .append("\n")
                        .append(date.format(formatter))
                        .append("\n")
                        .append(event.getDescription())
                        .append("\n")
                        .append("Lat: ").append(event.getLatitude())
                        .append(", Lon: ").append(event.getLongitude())
                        .append("\n\n");

                synchronized (moreCoordinatesList) {
                    moreCoordinatesList.add((double) event.getLatitude());
                    moreCoordinatesList.add((double) event.getLongitude());
                    System.out.println(moreCoordinatesList.toString());
                }
            }

            notifyListeners();

            //Update UI
            String finalEventNameText = events.size() > 1 ? "More events" : eventNameText;
            String finalEventInfoText = sb.toString();
            SwingUtilities.invokeLater(() -> {
                eventName.setText(finalEventNameText);
                if (isMoreEvents)
                    eventInfo.append(finalEventNameText);
                else
                    eventInfo.setText(finalEventInfoText);
            });

            return true;
        } catch (Exception e) {
            System.err.println("Error updating info box: " + e.getMessage());
            SwingUtilities.invokeLater(() -> eventInfo.setText("Error loading events."));
            return false;
        } finally {
            em.close();
        }
    }

    /**
     * @return true if calling getMoreCoordinatesCurrentIndex(-2) would still be in‑bounds
     */
    public boolean hasPreviousCoordinates() {
        return MoreCoordinatesCurrentIndex > 1;
    }

    /**
     * @return true if calling getMoreCoordinatesCurrentIndex(+2) would still be in‑bounds
     */
    public boolean hasNextCoordinates() {
        return MoreCoordinatesCurrentIndex + 2 < moreCoordinatesList.size();
    }
}
