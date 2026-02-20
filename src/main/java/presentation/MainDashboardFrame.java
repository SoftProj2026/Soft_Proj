package presentation;

import domain.Appointment;
import domain.TimeSlot;
import Service.AuthService;
import Service.ScheduleService;
import Service.BookingService;
import Service.BookingResult;

import javax.swing.*;
import java.awt.*;
import java.util.List;

class MainDashboardFrame extends JFrame {

    private JList<TimeSlot> slotList;
    private JTextField durationField;
    private JTextField participantsField;

    public MainDashboardFrame(AuthService auth,
                              ScheduleService schedule,
                              BookingService booking) {

        setTitle("Dashboard - Book Appointment");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultListModel<TimeSlot> model = new DefaultListModel<>();
        List<TimeSlot> availableSlots = schedule.getAvailableSlots();

        for (TimeSlot s : availableSlots) {
            model.addElement(s);
        }

        slotList = new JList<>(model);
        add(new JScrollPane(slotList), BorderLayout.CENTER);

        JPanel bookingPanel = new JPanel(new GridLayout(3, 2));

        bookingPanel.add(new JLabel("Duration (minutes):"));
        durationField = new JTextField();
        bookingPanel.add(durationField);

        bookingPanel.add(new JLabel("Participants:"));
        participantsField = new JTextField();
        bookingPanel.add(participantsField);

        JButton bookBtn = new JButton("Book Appointment");
        bookingPanel.add(bookBtn);

        JButton logoutBtn = new JButton("Logout");
        bookingPanel.add(logoutBtn);

        add(bookingPanel, BorderLayout.SOUTH);

        bookBtn.addActionListener(e -> {

            TimeSlot selectedSlot = slotList.getSelectedValue();

            if (selectedSlot == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a time slot.");
                return;
            }

            try {
                int duration = Integer.parseInt(durationField.getText());
                int participants = Integer.parseInt(participantsField.getText());

                Appointment appointment = new Appointment(
                        auth.getCurrentUser(),
                        selectedSlot,
                        duration,
                        participants
                );

                BookingResult result = booking.book(appointment);

                JOptionPane.showMessageDialog(this,
                        result.getMessage());

                if (result.isSuccess()) {
                    model.removeElement(selectedSlot);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter valid numbers.");
            }
        });

        logoutBtn.addActionListener(e -> {
            auth.logout();
            new LoginFrame(auth, schedule, booking).setVisible(true);
            this.dispose();
        });
    }
}
