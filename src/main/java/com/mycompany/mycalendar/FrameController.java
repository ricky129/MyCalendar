package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Map.CoordinatesListListener;
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

/**
 *
 * @author ricky
 */
public class FrameController {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("MyCalendarPU");
    private static final FrameController instance = new FrameController();
    private final int currentYear = Year.now().getValue();
    private final List<Double> CoordinatesList = new ArrayList<>();
    private final List<CoordinatesListListener> indexListeners = new ArrayList<>();
    private int CoordinatesCurrentIndex = -1;

    public boolean moveToNextCoordinates() {
        if (hasNextCoordinates()) {
            CoordinatesCurrentIndex += 2;
            notifyListeners();
            return true;
        }
        return false;
    }

    public boolean moveToPreviousCoordinates() {
        if (hasPreviousCoordinates()) {
            CoordinatesCurrentIndex -= 2;
            notifyListeners();
            return true;
        }
        return false;
    }

    public double getCurrentLat() {
        if (CoordinatesCurrentIndex >= 0 && CoordinatesCurrentIndex < CoordinatesList.size())
            return CoordinatesList.get(CoordinatesCurrentIndex);
        return 0.0; // Default if no coordinates
    }

    public double getCurrentLon() {
        if (CoordinatesCurrentIndex + 1 < CoordinatesList.size())
            return CoordinatesList.get(CoordinatesCurrentIndex + 1);
        return 0.0; // Default if no coordinates
    }

    public boolean hasNextCoordinates() {
        return CoordinatesCurrentIndex + 2 < CoordinatesList.size();
    }

    public boolean hasPreviousCoordinates() {
        return CoordinatesCurrentIndex - 2 >= 0;
    }

    private FrameController() {
    }

    public static FrameController getInstance() {
        return instance;
    }

    public void addCoordinatesListListener(CoordinatesListListener listener) {
        synchronized (indexListeners) {
            indexListeners.add(listener);
        }
    }

    public void removeCoordinatesListListener(CoordinatesListListener listener) {
        synchronized (indexListeners) {
            indexListeners.remove(listener);
        }
    }

    private void notifyListeners() {
        synchronized (indexListeners) {
            int size = CoordinatesList.size();
            for (CoordinatesListListener listener : indexListeners) {
                listener.onCoordinatesListChanged(size, CoordinatesCurrentIndex);
            }
        }
    }

    public static EntityManagerFactory getEmf() {
        return emf;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public List<Double> getCoordinatesList() {
        return CoordinatesList;
    }

    private boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    public int getNumberOfDays(Month month) {
        return month.length(isLeapYear(currentYear));
    }

    public List<Event> getEventsForDateFromSQL(LocalDateTime dateFromUser) {
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

            List<Event> events = query.getResultList();

            addCoordinatesToList(events);
            
            return events;
        } finally {
            em.close();
        }
    }

    public void addCoordinatesToList(List<Event> events) {
        synchronized (CoordinatesList) {
            CoordinatesList.clear();
            for (Event event : events) {
                CoordinatesList.add((double) event.getLatitude());
                CoordinatesList.add((double) event.getLongitude());
                System.out.println("CoordinatesList: " + CoordinatesList.toString());
            }
        }
        CoordinatesCurrentIndex = events.isEmpty() ? -1 : 0;
        notifyListeners();
    }

    public void resetCoordinates() {
        synchronized (CoordinatesList) {
            CoordinatesList.clear();
        }
        CoordinatesCurrentIndex = -1;
        notifyListeners();
    }

    public int getCoordinatesCurrentIndex() {
        return CoordinatesCurrentIndex;
    }

    public void setCoordinatesCurrentIndex(int CoordinatesCurrentIndex) {
        this.CoordinatesCurrentIndex = CoordinatesCurrentIndex;
    }
}
