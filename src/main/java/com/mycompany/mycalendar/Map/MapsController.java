package com.mycompany.mycalendar.Map;

import com.mycompany.mycalendar.FrameController;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javax.swing.JPanel;
import netscape.javascript.JSObject;

/**
 *
 * @author ricky
 */
public class MapsController {

    private MapCallback mapCallback;
    FrameController FC1 = FrameController.getInstance();
    boolean isMapLoaded = false;
    private final List<MapLoadListener> mapLoadListeners = new ArrayList<>();
    private final List<Runnable> pendingMapActions = new ArrayList<>();

    private double selectedLongitude = 0.0;
    private double selectedLatitude = 0.0;

    private void queueMapAction(Runnable action) {
        if (isMapLoaded)
            action.run();
        else
            synchronized (pendingMapActions) {
                pendingMapActions.add(action);
                System.out.println("Queued map action, pending actions: " + pendingMapActions.size());
            }
    }
    
    private void executePendingMapActions() {
        synchronized (pendingMapActions) {
            for (Runnable action : pendingMapActions)
                action.run();
            pendingMapActions.clear();
            System.out.println("Executed all pending actions");
        }
    }

    public void moveMapNext(WebView webview) {
        if (FC1.moveToNextCoordinates() && isMapLoaded) {
            double lat = FC1.getCurrentLat();
            double lon = FC1.getCurrentLon();
            System.out.println("lat:" + lat + " lon " + lon);
            if (webview != null)
                Platform.runLater(() -> {
                    webview.getEngine().executeScript("map.setView([" + lat + ", " + lon + "], 13);"
                            + "marker.setLatLng([" + lat + ", " + lon + "]);");
                });
            else
                System.out.println("webview is null");
        }
    }

    public void moveMapPrevious(WebView webview) {
        if (FC1.moveToPreviousCoordinates() && isMapLoaded) {
            double lat = FC1.getCurrentLat();
            double lon = FC1.getCurrentLon();
            System.out.println("lat:" + lat + " lon " + lon);
            if (webview != null)
                Platform.runLater(() -> {
                    webview.getEngine().executeScript("map.setView([" + lat + ", " + lon + "], 13);"
                            + "marker.setLatLng([" + lat + ", " + lon + "]);");
                });
            else
                System.out.println("webview is null");
        }
    }

    public void setMapToCurrentCoordinates(WebView webview) {
        queueMapAction(() -> {
            double lat = FC1.getCurrentLat();
            double lon = FC1.getCurrentLon();
            if (webview != null)
                Platform.runLater(() -> {
                    webview.getEngine().executeScript("map.setView([" + lat + ", " + lon + "], 13);"
                            + "marker.setLatLng([" + lat + ", " + lon + "]);");
                });
        });
    }

    // Method to initialize the JavaFX WebView with the OSM map
    public void initializeMap(WebView webView, JFXPanel fxPanel, JPanel mapPanel, MapCallback callback) {
        this.mapCallback = callback;

        Platform.runLater(() -> {
            webView.getEngine().setUserAgent("MyCalendarApp/1.0 (riccardomarchesini036@gmail.com)");

            //enable JavaScritp console logging for debugging
            webView.getEngine().setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));

            URL resourceUrl = getClass().getResource("/html/map.html");
            if (resourceUrl == null) {
                System.err.println("Error: Could not find /html/map.html in resources");
                webView.getEngine().loadContent("<h1>Error: Map file not found</h1>");
                setMapLoaded(true);
            } else {
                System.out.println("Loading map from: " + resourceUrl.toExternalForm());
                webView.getEngine().load(resourceUrl.toExternalForm());
                webView.getEngine().setOnError(event -> {
                    System.err.println("WebView error: " + event.getMessage());
                });
                webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webView.getEngine().executeScript("window");
                        window.setMember("javaCallback", this.mapCallback);
                        webView.getEngine().executeScript("if (typeof map !== 'undefined') map.invalidateSize();");
                        System.out.println("Map loaded successfully in WebView");
                        setMapLoaded(true);
                    } else if (newState == Worker.State.FAILED) {
                        System.err.println("Map failed to load");
                        setMapLoaded(true);
                    }
                });
            }
            // Set a larger initial size
            Scene scene = new Scene(webView, 800, 600);
            fxPanel.setScene(scene);
            fxPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        });
        mapPanel.add(fxPanel, java.awt.BorderLayout.CENTER);
        mapPanel.setVisible(false);
    }

    // Getter for isMapLoaded
    public boolean isMapLoaded() {
        return isMapLoaded;
    }

    // Setter for isMapLoaded that notifies listeners
    public void setMapLoaded(boolean isMapLoaded) {
        if (this.isMapLoaded != isMapLoaded) {  // Only notify if the state actually changes
            this.isMapLoaded = isMapLoaded;
            System.out.println("isMapLoaded set to: " + isMapLoaded);
            notifyMapLoaded();
            if(isMapLoaded)
                executePendingMapActions();
        }
    }

    public void addMapLoadListener(MapLoadListener listener) {
        synchronized (mapLoadListeners) {
            mapLoadListeners.add(listener);
        }
    }

    public void removeMapLoadListener(MapLoadListener listener) {
        synchronized (mapLoadListeners) {
            mapLoadListeners.remove(listener);
        }
    }

    private void notifyMapLoaded() {
        synchronized (mapLoadListeners) {
            for (MapLoadListener listener : mapLoadListeners) {
                listener.onMapLoaded();
            }
        }
    }

    public double getSelectedLongitude() {
        return selectedLongitude;
    }

    public void setSelectedLongitude(double selectedLongitude) {
        this.selectedLongitude = selectedLongitude;
    }

    public double getSelectedLatitude() {
        return selectedLatitude;
    }

    public void setSelectedLatitude(double selectedLatitude) {
        this.selectedLatitude = selectedLatitude;
    }

}
