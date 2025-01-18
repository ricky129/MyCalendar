package javaapplication1;

import java.util.Calendar;
import static java.util.Calendar.DAY_OF_WEEK;
import java.util.Date;
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

    int dayFill = 1, currentYear = 2025;

    int dayFillPlus() {
        return dayFill++;
    }

    public void initializeCalendar(JTable JTB1) {
        for (int row = 0; row < JTB1.getRowCount(); row++) {
            for (int col = 0; col < JTB1.getColumnCount() && dayFill <= 31; col++) {
                ((DefaultTableModel) JTB1.getModel()).setValueAt(dayFillPlus(), row, col);
            }
        }
    }

    public int getNumberOfDays(String month) {
        switch (month) {
            case "January", "March", "May", "July", "August", "October", "December" -> {
                return 31;
            }
            case "April", "June", "September", "November" -> {
                return 30;
            }
            case "February" -> {
                /*
            // Handle leap years
            int year = Calendar.getInstance().get(Calendar.YEAR);
            return (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) ? 29 : 28;
                 */
                return 28;
            }
            default ->
                throw new IllegalArgumentException("Invalid month: " + month);
        }
    }

    // Helper method to get month index (0-based)
    private int getMonthIndex(String month) {
        switch (month) {
            case "January": return 0;
            case "February": return 1;
            case "March": return 2;
            case "April": return 3;
            case "May": return 4;
            case "June": return 5;
            case "July": return 6;
            case "August": return 7;
            case "September": return 8;
            case "October": return 9;
            case "November": return 10;
            case "December": return 11;
            default: throw new IllegalArgumentException("Invalid month");
        }
    }
    
    // Helper method to get the starting day of the week (Monday = 0)
    private int getStartingDayOfWeek(String month, int year) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(year, getMonthIndex(month), 1); // Set to the first day of the month
    return calendar.get(DAY_OF_WEEK) - 2; // Adjust to 0-based index (Monday = 0)
    }

    // Helper method to get the first day of the month as a Date object
    private Date getFirstDayOfMonth(String month, int year) {
    return new Date(year - 1900, getMonthIndex(month), 1); 
    }

    public void updateCalendar(JTable JTB1, JComboBox JCB1) {
    dayFill = 1;

    // Clear the table
    for (int row = 0; row < JTB1.getRowCount(); row++)
        for (int col = 0; col < JTB1.getColumnCount(); col++)
            ((DefaultTableModel) JTB1.getModel()).setValueAt(null, row, col);

    String selectedMonth = JCB1.getSelectedItem().toString();
    int daysInMonth = getNumberOfDays(selectedMonth);

    // Calculate starting day of the week (assuming Monday is the first day)
    int startingDayOfWeek = getStartingDayOfWeek(selectedMonth, currentYear); 

    for (int row = 0; row < JTB1.getRowCount(); row++) {
        for (int col = 0; col < JTB1.getColumnCount(); col++) {
            if (col >= startingDayOfWeek && dayFill <= daysInMonth) {
                // Calculate milliseconds since the Epoch for the current day
                long timeInMillis = getFirstDayOfMonth(selectedMonth, currentYear).getTime() 
                        + (dayFill - 1) * 24 * 60 * 60 * 1000L; 
                ((DefaultTableModel) JTB1.getModel()).setValueAt(new Date(timeInMillis), row, col);
                dayFill++;
            }
        }
    }
}

    public void updateEventBox(JTextField JTF1, JTextArea JTA1) {

    }
}
