package com.mycompany.mycalendar;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
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

    private int currentYear = Year.now().getValue();

    /*
    In this case the class does not use its instance but takes it 
    from the constructor caller, NewJFrame.java in this case.
     */
    LoadCSV LCSV1;

    /**
     * It asks for the LoadCSV.java class object as a parameter, as to use the main instance of the program and not its
     *
     * @param LCSV
     */
    public FormController(LoadCSV LCSV) {
        this.LCSV1 = LCSV;
    }

    private boolean isLeapYear(int year) {
        return Year.of(year).isLeap();
    }

    public int getNumberOfDays(Month month) {
        return month.length(isLeapYear(currentYear));
    }

    public void updateCalendar(JTable JTB1, JComboBox JCB1, Month month) {

        // Clear the table
        for (int row = 0; row < JTB1.getRowCount(); row++)
            for (int col = 0; col < 7; col++)
                ((DefaultTableModel) JTB1.getModel()).setValueAt(null, row, col);

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

    public boolean getEvent(LocalDateTime date, JTextField EventName, JTextArea EventInfo) {
        boolean ret = false;
        String eventInfoTemp;

        if (LCSV1.getEvents() == null) {
            System.out.println("Event list is null");
            return false;
        }

        if (LCSV1.getEvents().isEmpty()) {
            System.out.println("Event list is empty");
            return false;
        }

        for (Event evento : LCSV1.getEvents()) {
            LocalDateTime dateLCSV = evento.getDate();

            if (dateLCSV.toLocalDate().equals(date.toLocalDate())) {

                switch (EventName.getText()) {
                    case "" -> {
                        EventName.setText(evento.getName());

                        StringBuilder sb = new StringBuilder();
                        sb.append(dateLCSV.getMonth())
                                .append(" ")
                                .append(dateLCSV.getDayOfMonth())
                                .append(" ")
                                .append(dateLCSV.getHour())
                                .append(":")
                                .append(dateLCSV.getMinute())
                                .append("\n")
                                .append(evento.getDescription())
                                .append("\n");

                        EventInfo.setText(sb.toString());
                        ret = true;
                    }
                    case "More events" -> {
                        StringBuilder sb = new StringBuilder();
                        sb.append("\n")
                                .append(evento.getName())
                                .append("\n")
                                .append(dateLCSV.getMonth())
                                .append(" ")
                                .append(dateLCSV.getDayOfMonth())
                                .append(" ")
                                .append(dateLCSV.getHour())
                                .append(":")
                                .append(dateLCSV.getMinute())
                                .append("\n")
                                .append(evento.getDescription());

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
                                .append(evento.getName())
                                .append("\n")
                                .append(dateLCSV.getMonth())
                                .append(" ")
                                .append(dateLCSV.getDayOfMonth())
                                .append(" ")
                                .append(dateLCSV.getHour())
                                .append(":")
                                .append(dateLCSV.getMinute())
                                .append("\n")
                                .append(evento.getDescription())
                                .append("\n");

                        EventInfo.setText(sb.toString());
                        EventName.setText("More events");
                        ret = true;
                    }
                }
            }
        }
        return ret;
    }
}
