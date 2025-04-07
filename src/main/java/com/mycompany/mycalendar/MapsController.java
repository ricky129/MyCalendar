/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.mycalendar;

import javafx.application.Platform;
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
    FormController FC1 = FormController.getInstance();

    private double selectedLongitude = 0.0;
    private double selectedLatitude = 0.0;

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

    public void moveMapNext(WebView webview) {
        double lat = FC1.getCoordinatesTEMP();
        double lon = FC1.getCoordinatesTEMP();
        if (lat == -200 && lon == -200)
            System.out.println("Coordinates not valid");
        else {
            if (webview != null) {
                Platform.runLater(() -> {
                    webview.getEngine().executeScript("map.setView([" + lat + ", " + lon + "], 13);");
                });
            } else
                System.out.println("webview is null");
        }
    }

    public void moveMapPrevious(WebView webview) {
        double lat = FC1.getCoordinatesTEMP();
        double lon = FC1.getCoordinatesTEMP();
        if (webview != null) {
            Platform.runLater(() -> {
                webview.getEngine().executeScript("map.setView([" + lat + ", " + lon + "], 13);");
            });
        }
    }

    // Method to initialize the JavaFX WebView with the OSM map
    public void initializeMap(WebView webView, JFXPanel fxPanel, JPanel mapPanel, MapCallback callback) {
        this.mapCallback = callback;

        Platform.runLater(() -> {
            webView.getEngine().setUserAgent("MyCalendarApp/1.0 (riccardomarchesini036@gmail.com)");

            //enable JavaScritp console logging for debugging
            webView.getEngine().setOnAlert(event -> System.out.println("JS Alert: " + event.getData()));

            java.net.URL resourceUrl = getClass().getResource("/html/map.html");
            if (resourceUrl == null) {
                System.err.println("Error: Could not find /html/map.html in resources");
                webView.getEngine().loadContent("<h1>Error: Map file not found</h1>");
            } else {
                System.out.println("Loading map from: " + resourceUrl.toExternalForm());
                webView.getEngine().load(resourceUrl.toExternalForm());
                webView.getEngine().setOnError(event -> {
                    System.err.println("WebView error: " + event.getMessage());
                });
                webView.getEngine().getLoadWorker().stateProperty().addListener((obs, old, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webView.getEngine().executeScript("window");
                        window.setMember("javaCallback", this.mapCallback);
                        webView.getEngine().executeScript("if (typeof map !== 'undefined') map.invalidateSize();");
                        System.out.println("Map loaded successfully in WebView");
                    }
                });
            }
            // Set a larger initial size
            Scene scene = new Scene(webView, 800, 600);
            fxPanel.setScene(scene);
            fxPanel.setPreferredSize(new java.awt.Dimension(800, 600));
        });
        mapPanel.add(fxPanel, java.awt.BorderLayout.CENTER);
        mapPanel.setVisible(true);
    }
}
