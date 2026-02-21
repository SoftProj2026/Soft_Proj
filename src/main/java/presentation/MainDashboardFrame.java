package presentation;

import domain.Appointment;
import domain.TimeSlot;
import Service.AuthService;
import Service.BookingService;
import Service.BookingResult;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

class MainDashboardFrame extends JFrame {

    private JTextField dateField;
    private JTextField timeField;
    private JTextField durationField;
    private JTextField participantsField;

    public MainDashboardFrame(AuthService auth,
                              BookingService booking) {

        setTitle("Dynamic Booking System");
        setSize(500, 350);
        setLayout(new GridLayout(6, 2));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        add(new JLabel("Date (YYYY-MM-DD):"));
        dateField = new JTextField();
        add(dateField);

        add(new JLabel("Time (HH:MM):"));
        timeField = new JTextField();
        add(timeField);

        add(new JLabel("Duration (minutes):"));
        durationField = new JTextField();
        add(durationField);

        add(new JLabel("Participants:"));
        participantsField = new JTextField();
        add(participantsField);

        JButton bookBtn = new JButton("Book Appointment");
        add(bookBtn);

        JButton logoutBtn = new JButton("Logout");
        add(logoutBtn);

        bookBtn.addActionListener(e -> {

            try {

                LocalDate date = LocalDate.parse(dateField.getText());
                LocalTime time = LocalTime.parse(timeField.getText());
                LocalDateTime start = LocalDateTime.of(date, time);

                int duration = Integer.parseInt(durationField.getText());
                int participants = Integer.parseInt(participantsField.getText());

                TimeSlot slot = new TimeSlot(start, duration);

                Appointment appointment =
                        new Appointment(auth.getCurrentUser(),
                                slot,
                                duration,
                                participants);

                BookingResult result = booking.book(appointment);

                JOptionPane.showMessageDialog(this,
                        result.getMessage());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid input format.");
            }
        });

        logoutBtn.addActionListener(e -> {
            auth.logout();
            this.dispose();
        });
    }
}