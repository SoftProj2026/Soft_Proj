package presentation;

import Service.AuthService;
import domain.Appointment;
import domain.AppointmentStatus;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the currently logged-in user's bookings in a table.
 *
 * Note: Desktop / popup reminders have been removed.
 */
public class MyBookingsFrame extends JFrame {

    private final AuthService auth;
    private final DataRepository repo;

    private final DefaultTableModel model;
    private final JTable table;

    private final List<Appointment> visibleAppointments = new ArrayList<>();

    private static final Color CANCELLED_FG = new Color(180, 30, 30);
    private static final Color CONFIRMED_FG = new Color(20, 83, 45);

    /**
     * Creates the "My Bookings" window.
     *
     * @param auth authentication service
     * @param repo data repository
     */
    public MyBookingsFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("My Bookings");
        setSize(980, 440);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Removed "Reminder" column
        String[] cols = {"ID", "Category", "Start", "Duration", "Participants", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        installRowRenderer();

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
     * Installs a custom renderer to color the status text for CONFIRMED and CANCELLED.
     * (No "SOON" highlighting / reminders.)
     */
    private void installRowRenderer() {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table,
                                                           Object value,
                                                           boolean isSelected,
                                                           boolean hasFocus,
                                                           int row,
                                                           int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column
                );

                if (!isSelected) {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }

                // Status column index is now 5 (last column)
                if (!isSelected && column == 5 && row >= 0 && row < visibleAppointments.size()) {
                    Appointment a = visibleAppointments.get(row);
                    if (a.getStatus() == AppointmentStatus.CANCELLED) {
                        c.setForeground(CANCELLED_FG);
                    } else if (a.getStatus() == AppointmentStatus.CONFIRMED) {
                        c.setForeground(CONFIRMED_FG);
                    }
                }

                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
    }

    /**
     * Loads the current user's bookings into the table.
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
            if (a.getUser() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            visibleAppointments.add(a);

            String categoryName = "N/A";
            String startStr = "N/A";
            if (a.getSlot() != null) {
                if (a.getSlot().getCategory() != null) categoryName = a.getSlot().getCategory().getName();
                if (a.getSlot().getStartDateTime() != null) startStr = a.getSlot().getStartDateTime().format(fmt);
            }

            model.addRow(new Object[]{
                    a.getId(),
                    categoryName,
                    startStr,
                    a.getDurationInMinutes(),
                    a.getParticipants(),
                    a.getStatus().name()
            });
        }

        table.repaint();
    }

    /**
     * Cancels the selected booking if it is confirmed.
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