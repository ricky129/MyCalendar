package com.mycompany.mycalendar;

import com.mycompany.mycalendar.Map.CoordinatesListListener;
import com.mycompany.mycalendar.Event.Event;
import java.time.Month;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ricky
 */
public class FrameController {

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
        return 51.505; // Default if no coordinates
    }

    public double getCurrentLon() {
        if (CoordinatesCurrentIndex + 1 < CoordinatesList.size())
            return CoordinatesList.get(CoordinatesCurrentIndex + 1);
        return -0.09; // Default if no coordinates
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

    /**
     * Notifies all registered CoordinatesListListeners about a change
     * in the coordinates list or the current index.
     * This method is typically called after operations that alter `CoordinatesList` or `CoordinatesCurrentIndex`.
     */
    private void notifyListeners() {
        synchronized (indexListeners) {
            int size = CoordinatesList.size();
            for (CoordinatesListListener listener : indexListeners) {
                listener.onCoordinatesListChanged(size, CoordinatesCurrentIndex);
            }
        }
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

    /**
     * Populates the internal coordinates list with latitudes and longitudes from a list of events.
     * Clears any existing coordinates before adding new ones. Sets the current index to the first event
     * if the list is not empty, and then notifies listeners.
     * @param events A list of Event objects from which to extract coordinates.
     */
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
