package presentation;

import Service.AuthService;
import Service.ReminderService;
import domain.Appointment;
import domain.AppointmentStatus;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

/**
 * Displays the logged-in user's bookings in a table.
 * <p>
 * Allows refreshing and cancelling confirmed bookings.
 * Also highlights appointments that are within the reminder window (e.g., 60 minutes).
 * </p>
 */
public class MyBookingsFrame extends JFrame {

    private final AuthService auth;
    private final DataRepository repo;

    // Sprint 3: used for "soon" snapshot (optional) + consistency with popup reminders
    private final ReminderService reminder;

    private final DefaultTableModel model;
    private final JTable table;

    private List<Appointment> visibleAppointments = new ArrayList<>();

    // computed at load time: minutes until start per appointment id
    private final Map<Integer, Long> minutesUntilById = new HashMap<>();

    // UI colors
    private static final Color SOON_BG = new Color(254, 249, 195); // light yellow
    private static final Color CANCELLED_FG = new Color(180, 30, 30);
    private static final Color CONFIRMED_FG = new Color(20, 83, 45);

    /**
     * Creates the MyBookings window.
     *
     * @param auth authentication service
     * @param repo data repository
     * @param reminder reminder service (can be null)
     */
    public MyBookingsFrame(AuthService auth, DataRepository repo, ReminderService reminder) {
        this.auth = auth;
        this.repo = repo;
        this.reminder = reminder;

        setTitle("My Bookings");
        setSize(980, 440);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        String[] cols = {"ID", "Category", "Start", "Duration", "Participants", "Status", "Reminder"};
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
     * Backward-compatible constructor if still used anywhere.
     */
    public MyBookingsFrame(AuthService auth, DataRepository repo) {
        this(auth, repo, null);
    }

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

                Appointment a = null;
                if (row >= 0 && row < visibleAppointments.size()) {
                    a = visibleAppointments.get(row);
                }

                if (a != null) {
                    Long mins = minutesUntilById.get(a.getId());
                    boolean isSoon = mins != null
                            && mins >= 0
                            && mins <= 60
                            && a.getStatus() == AppointmentStatus.CONFIRMED;

                    if (!isSelected && isSoon) {
                        c.setBackground(SOON_BG);
                    }

                    // Color Status text
                    if (!isSelected && column == 5) { // Status column index
                        if (a.getStatus() == AppointmentStatus.CANCELLED) {
                            c.setForeground(CANCELLED_FG);
                        } else if (a.getStatus() == AppointmentStatus.CONFIRMED) {
                            c.setForeground(CONFIRMED_FG);
                        }
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
     * Loads appointments belonging to the current logged-in user into the table.
     */
    private void loadMyBookings() {
        model.setRowCount(0);
        visibleAppointments.clear();
        minutesUntilById.clear();

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "You must login first.");
            dispose();
            return;
        }

        String username = auth.getCurrentUser().getUsername();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();

        // Optional: soon ids from reminder service snapshot
        Set<Integer> soonFromService = new HashSet<>();
        if (reminder != null) {
            for (Appointment a : reminder.getSoonAppointmentsSnapshot()) {
                soonFromService.add(a.getId());
            }
        }

        for (Appointment a : repo.getAppointments()) {
            if (a.getUser() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            visibleAppointments.add(a);

            long minsUntil = Long.MIN_VALUE;
            if (a.getSlot() != null && a.getSlot().getStartDateTime() != null) {
                minsUntil = Duration.between(now, a.getSlot().getStartDateTime()).toMinutes();
            }
            minutesUntilById.put(a.getId(), minsUntil);

            String categoryName = "N/A";
            String startStr = "N/A";
            if (a.getSlot() != null) {
                if (a.getSlot().getCategory() != null) categoryName = a.getSlot().getCategory().getName();
                if (a.getSlot().getStartDateTime() != null) startStr = a.getSlot().getStartDateTime().format(fmt);
            }

            boolean isSoonComputed =
                    a.getStatus() == AppointmentStatus.CONFIRMED
                            && minsUntil >= 0
                            && minsUntil <= 60;

            boolean isSoon = isSoonComputed || soonFromService.contains(a.getId());

            String reminderText = "";
            if (isSoon) {
                if (minsUntil <= 0) reminderText = "SOON (now)";
                else reminderText = "SOON (in " + minsUntil + " min)";
            }

            model.addRow(new Object[]{
                    a.getId(),
                    categoryName,
                    startStr,
                    a.getDurationInMinutes(),
                    a.getParticipants(),
                    a.getStatus().name(),
                    reminderText
            });
        }

        table.repaint();
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