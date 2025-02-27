package com.mycompany.mycalendar;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
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
     * It asks for the LoadCSV.java class object as a parameter, as to
     * use the main instance of the program and not its
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

    // Helper method to get month index (0-based)
    private int getMonthIndex(String month) {
        switch (month) {
            case "January" -> {
                return 0;
            }
            case "February" -> {
                return 1;
            }
            case "March" -> {
                return 2;
            }
            case "April" -> {
                return 3;
            }
            case "May" -> {
                return 4;
            }
            case "June" -> {
                return 5;
            }
            case "July" -> {
                return 6;
            }
            case "August" -> {
                return 7;
            }
            case "September" -> {
                return 8;
            }
            case "October" -> {
                return 9;
            }
            case "November" -> {
                return 10;
            }
            case "December" -> {
                return 11;
            }
            default ->
                throw new IllegalArgumentException("Invalid month");
        }
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

    private Event getEvent(LocalDateTime date, JTextField JTF1, JTextArea JTA1) {
        if (LCSV1.getEventi() == null) {
            System.out.println("Event list is null");
            return null;
        }

        for (int i = 0; i < LCSV1.eventi.size(); i++) {
            
            LocalDateTime dateLCSV = LCSV1.eventi.get(i).date;
            
            if(dateLCSV.getYear() == date.getYear() || dateLCSV.getMonth() == date.getMonth() 
                    || dateLCSV.getMinute() == date.getMinute()){
                JTF1.setText("More events");
                JTA1.append(LCSV1.eventi.get(i).date + "\n" + LCSV1.eventi.get(i).name + "\n" + LCSV1.eventi.get(i).description);
            }
                
            //if(LCSV1.eventi.get(i).date.toLocalDate())
        }

        System.out.println("Eventi caricati: " + LCSV1.getEventi().toString());
        System.out.println("Non trovato");
        return null;
    }

    public void updateEventBox(JTextField JTF1, JTextArea JTA1, LocalDateTime date) {
        Event E1 = getEvent(date, JTF1, JTA1);
        
        if(E1 != null){
            JTF1.setText(E1.name);
            JTA1.setText(E1.description);
        }
        else{
            JTF1.setText("");
            JTA1.setText("No event found!");
        }
            
    }

    public int getDayId(int day, String month, int year) {
        try {
            LocalDateTime date = LocalDateTime.of(year, getMonthIndex(month) + 1, day, 0, 0);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
            return Integer.parseInt(date.format(formatter));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
