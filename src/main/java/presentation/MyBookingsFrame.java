package presentation;

import Service.AuthService;
import domain.Appointment;
import domain.AppointmentStatus;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the logged-in user's bookings in a table.
 * <p>
 * Allows refreshing and cancelling confirmed bookings.
 * </p>
 */
public class MyBookingsFrame extends JFrame {

    private final AuthService auth;
    private final DataRepository repo;

    private final DefaultTableModel model;
    private final JTable table;

    private List<Appointment> visibleAppointments = new ArrayList<>();

    /**
     * Creates the MyBookings window.
     *
     * @param auth authentication service
     * @param repo data repository
     */
    public MyBookingsFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("My Bookings");
        setSize(850, 420);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] cols = {"ID", "Category", "Start", "Duration", "Participants", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton refreshBtn = new JButton("Refresh");
        JButton cancelBtn = new JButton("Cancel Booking");
        JButton closeBtn = new JButton("Close");

        actions.add(refreshBtn);
        actions.add(cancelBtn);
        actions.add(closeBtn);

        add(actions, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadMyBookings());
        cancelBtn.addActionListener(e -> cancelSelected());
        closeBtn.addActionListener(e -> dispose());

        loadMyBookings();
    }

    /**
     * Loads appointments belonging to the current logged-in user into the table.
     */
    private void loadMyBookings() {
        model.setRowCount(0);
        visibleAppointments.clear();

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "You must login first.");
            dispose();
            return;
        }

        String username = auth.getCurrentUser().getUsername();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Appointment a : repo.getAppointments()) {
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            visibleAppointments.add(a);

            model.addRow(new Object[]{
                    a.getId(),
                    a.getSlot().getCategory().getName(),
                    a.getSlot().getStartDateTime().format(fmt),
                    a.getDurationInMinutes(),
                    a.getParticipants(),
                    a.getStatus().name()
            });
        }
    }

    /**
     * Cancels the selected appointment if it is confirmed.
     */
    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.");
            return;
        }

        Appointment selected = visibleAppointments.get(row);

        if (selected.getStatus() != AppointmentStatus.CONFIRMED) {
            JOptionPane.showMessageDialog(this, "This booking is not CONFIRMED.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to cancel this booking?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String msg = repo.cancelAppointment(selected);
        JOptionPane.showMessageDialog(this, msg);

        loadMyBookings();
    }
}