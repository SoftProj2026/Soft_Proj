package presentation;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.TimeSlot;
import Service.AuthService;
import Service.BookingService;
import Service.BookingResult;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainDashboardFrame extends JFrame {

    private JTextField dateField = new JTextField(10);
    private JTextField timeField = new JTextField(5);
    private JTextField durationField = new JTextField(5);
    private JTextField participantsField = new JTextField(5);

    private DefaultTableModel tableModel;
    private JTable table;

    private AuthService auth;
    private BookingService booking;
    private DataRepository repo;

    // Tracks which Appointment object corresponds to each visible table row
    private List<Appointment> displayedAppointments = new ArrayList<>();

    public MainDashboardFrame(AuthService auth,
                              BookingService booking,
                              DataRepository repo) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;

        setTitle("Booking Dashboard");
        setSize(700, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel form = new JPanel(new GridLayout(6,2));

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(dateField);

        form.add(new JLabel("Time (HH:MM):"));
        form.add(timeField);

        form.add(new JLabel("Duration (minutes):"));
        form.add(durationField);

        form.add(new JLabel("Participants:"));
        form.add(participantsField);

        JButton bookBtn = new JButton("Book");
        JButton saveBtn = new JButton("Export to CSV");
        JButton cancelBtn = new JButton("Cancel Selected");
        JButton logoutBtn = new JButton("Logout");

        form.add(bookBtn);
        form.add(saveBtn);
        form.add(cancelBtn);
        form.add(logoutBtn);

        add(form, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new String[]{"User", "Date", "Time", "Duration", "Participants", "Status"}, 0);

        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refreshTable();

        bookBtn.addActionListener(e -> bookAppointment());
        saveBtn.addActionListener(e -> exportCSV());
        cancelBtn.addActionListener(e -> cancelAppointment());
        logoutBtn.addActionListener(e -> {
            auth.logout();
            this.dispose();
        });
    }

    private void bookAppointment() {

        try {
            LocalDateTime dateTime =
                    LocalDateTime.parse(dateField.getText()
                            + "T"
                            + timeField.getText());

            int duration = Integer.parseInt(durationField.getText());
            int participants = Integer.parseInt(participantsField.getText());

            TimeSlot slot = new TimeSlot(dateTime, duration);

            Appointment appointment =
                    new Appointment(auth.getCurrentUser(),
                            slot,
                            duration,
                            participants);

            BookingResult result = booking.book(appointment);

            JOptionPane.showMessageDialog(this,
                    result.getMessage());

            if (result.isSuccess()) {
                refreshTable();
                clearForm(); //  بس نفرغ الفورم
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Invalid input format!");
        }
    }

    private void cancelAppointment() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= displayedAppointments.size()) {
            JOptionPane.showMessageDialog(this,
                    "Please select an appointment to cancel.");
            return;
        }
        Appointment a = displayedAppointments.get(row);
        if (a.getStatus() != AppointmentStatus.CONFIRMED) {
            JOptionPane.showMessageDialog(this,
                    "Only confirmed appointments can be cancelled.");
            return;
        }
        a.cancel();
        refreshTable();
        JOptionPane.showMessageDialog(this, "Appointment cancelled.");
    }

    private void refreshTable() {

        tableModel.setRowCount(0);
        displayedAppointments.clear();

        String currentUser = auth.getCurrentUser().getUsername();

        for (Appointment a : repo.getAppointments()) {

            if (!a.getUser().getUsername().equals(currentUser)) {
                continue;
            }

            displayedAppointments.add(a);

            tableModel.addRow(new Object[]{
                    a.getUser().getUsername(),
                    a.getSlot().getStartDateTime().toLocalDate(),
                    a.getSlot().getStartDateTime().toLocalTime(),
                    a.getDurationInMinutes(),
                    a.getParticipants(),
                    a.getStatus()
            });
        }
    }

    private void exportCSV() {

        try {
            PrintWriter writer = new PrintWriter(
                    new FileWriter("appointments.csv"));

            writer.println("User,Date,Time,Duration,Participants");

            for (Appointment a : repo.getAppointments()) {

                writer.println(
                        a.getUser().getUsername() + "," +
                        a.getSlot().getStartDateTime().toLocalDate() + "," +
                        a.getSlot().getStartDateTime().toLocalTime() + "," +
                        a.getDurationInMinutes() + "," +
                        a.getParticipants()
                );
            }

            writer.close();

            JOptionPane.showMessageDialog(this,
                    "Bookings exported successfully (CSV file created).");

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error saving file.");
        }
    }

    private void clearForm() {
        dateField.setText("");
        timeField.setText("");
        durationField.setText("");
        participantsField.setText("");
    }
}