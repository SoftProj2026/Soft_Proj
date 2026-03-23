package presentation;

import Service.AuthService;
import domain.Administrator;
import domain.Appointment;
import domain.AppointmentStatus;
import domain.TimeSlot;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Administrator screen for managing user reservations (appointments).
 *
 * <p>This frame allows administrators to:</p>
 * <ul>
 *   <li>View all appointments in the repository.</li>
 *   <li>Cancel future confirmed appointments.</li>
 *   <li>Modify future confirmed appointments by moving them to a different available slot
 *       (and updating duration/participants).</li>
 * </ul>
 *
 * <p>Only {@link Administrator} accounts should be able to open this screen.</p>
 * @author remaa
 * @version 1.0
 */
public class AdminManageReservationsFrame extends JFrame {

    private final DataRepository repo;
    private final AuthService auth;

    private final DefaultTableModel model;
    private final JTable table;

    private final List<Appointment> visible = new ArrayList<>();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates the reservations management screen.
     *
     * @param auth authentication service
     * @param repo data repository
     */
    public AdminManageReservationsFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("Admin - Manage Reservations");
        setSize(1250, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UITheme.BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Manage Reservations (Appointments)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Admin can modify or cancel future confirmed appointments.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        String[] cols = {
                "Appointment ID",
                "User",
                "Category",
                "Start",
                "End",
                "Duration",
                "Participants",
                "Status"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> load());

        JButton modify = UITheme.primaryButton("Modify");
        modify.addActionListener(e -> modifySelected());

        JButton cancel = UITheme.secondaryButton("Cancel");
        cancel.addActionListener(e -> cancelSelected());

        JButton close = UITheme.secondaryButton("Close");
        close.addActionListener(e -> dispose());

        actions.add(refresh);
        actions.add(cancel);
        actions.add(modify);
        actions.add(close);

        add(actions, BorderLayout.SOUTH);

        load();
    }

    /**
     * Loads all appointments into the table.
     */
    private void load() {
        model.setRowCount(0);
        visible.clear();

        for (Appointment a : repo.getAppointments()) {
            if (a == null) continue;

            String user = (a.getUser() != null && a.getUser().getUsername() != null) ? a.getUser().getUsername() : "N/A";
            String category = (a.getSlot() != null && a.getSlot().getCategory() != null && a.getSlot().getCategory().getName() != null)
                    ? a.getSlot().getCategory().getName()
                    : "N/A";

            String start = (a.getSlot() != null && a.getSlot().getStartDateTime() != null) ? a.getSlot().getStartDateTime().format(fmt) : "N/A";
            String end = (a.getSlot() != null && a.getSlot().getEndDateTime() != null) ? a.getSlot().getEndDateTime().format(fmt) : "N/A";

            visible.add(a);

            model.addRow(new Object[]{
                    a.getId(),
                    user,
                    category,
                    start,
                    end,
                    a.getDurationInMinutes(),
                    a.getParticipants(),
                    a.getStatus().name()
            });
        }
    }

    /**
     * Returns the selected appointment in the table.
     *
     * @return selected appointment or null
     */
    private Appointment selectedOrNull() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visible.size()) return null;
        return visible.get(row);
    }

    /**
     * Cancels a future confirmed appointment.
     */
    private void cancelSelected() {
        Appointment a = selectedOrNull();
        if (a == null) {
            DialogUtil.show(this, "No Selection", "Please select an appointment first.", DialogUtil.Type.WARNING);
            return;
        }

        if (a.getStatus() != AppointmentStatus.CONFIRMED) {
            DialogUtil.show(this, "Not Allowed", "Only CONFIRMED appointments can be cancelled.", DialogUtil.Type.WARNING);
            return;
        }

        if (a.getSlot() == null || a.getSlot().getStartDateTime() == null || !a.getSlot().getStartDateTime().isAfter(LocalDateTime.now())) {
            DialogUtil.show(this, "Not Allowed", "Only FUTURE appointments can be cancelled.", DialogUtil.Type.WARNING);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Cancel this appointment?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String adminUser = currentAdminUsername();
        String msg = repo.adminCancelAppointment(a, adminUser);
        DialogUtil.show(this, "Result", msg, DialogUtil.Type.INFO);
        load();
    }

    /**
     * Modifies a future confirmed appointment.
     */
    private void modifySelected() {
        Appointment a = selectedOrNull();
        if (a == null) {
            DialogUtil.show(this, "No Selection", "Please select an appointment first.", DialogUtil.Type.WARNING);
            return;
        }

        if (a.getStatus() != AppointmentStatus.CONFIRMED) {
            DialogUtil.show(this, "Not Allowed", "Only CONFIRMED appointments can be modified.", DialogUtil.Type.WARNING);
            return;
        }

        if (a.getSlot() == null || a.getSlot().getStartDateTime() == null || !a.getSlot().getStartDateTime().isAfter(LocalDateTime.now())) {
            DialogUtil.show(this, "Not Allowed", "Only FUTURE appointments can be modified.", DialogUtil.Type.WARNING);
            return;
        }

        TimeSlot newSlot = promptNewSlot(a);
        if (newSlot == null) return;

        Integer participants = promptIntInRange("Participants", "Enter participants (1 - 5):", 1, 5);
        if (participants == null) return;

        int maxMinutes = minutesBetween(newSlot);
        Integer duration = promptIntInRange(
                "Duration",
                "Enter duration in minutes (1 - " + maxMinutes + "):",
                1,
                maxMinutes
        );
        if (duration == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Confirm modifying this appointment to the new slot?",
                "Confirm Modification",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String msg = repo.modifyAppointment(a, newSlot, duration, participants, currentAdminUsername());
        DialogUtil.show(this, "Result", msg, DialogUtil.Type.INFO);
        load();
    }

    /**
     * Returns the current admin username, or "admin" as a fallback.
     *
     * @return username
     */
    private String currentAdminUsername() {
        if (auth != null && auth.isLoggedIn() && auth.getCurrentUser() != null && auth.getCurrentUser().getUsername() != null) {
            return auth.getCurrentUser().getUsername().trim();
        }
        return "admin";
    }

    /**
     * Prompts the admin to select a new slot for the appointment category that is available and in the future.
     *
     * @param a target appointment
     * @return chosen slot or null if cancelled
     */
    private TimeSlot promptNewSlot(Appointment a) {
        if (a == null || a.getSlot() == null || a.getSlot().getCategory() == null || a.getSlot().getCategory().getName() == null) {
            DialogUtil.show(this, "Invalid", "Cannot modify (missing category).", DialogUtil.Type.ERROR);
            return null;
        }

        String categoryName = a.getSlot().getCategory().getName();

        List<TimeSlot> options = new ArrayList<>();
        for (TimeSlot slot : repo.getSlots()) {
            if (slot == null || slot.getStartDateTime() == null) continue;
            if (slot.getCategory() == null || slot.getCategory().getName() == null) continue;

            if (!slot.getCategory().getName().equalsIgnoreCase(categoryName)) continue;
            if (!slot.isAvailable()) continue;
            if (!slot.getStartDateTime().isAfter(LocalDateTime.now())) continue;

            options.add(slot);
        }

        if (options.isEmpty()) {
            DialogUtil.show(this, "No Slots", "No available future slots found for this category.", DialogUtil.Type.WARNING);
            return null;
        }

        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = options.get(i).getStartDateTime().format(fmt);
        }

        String selectedLabel = (String) JOptionPane.showInputDialog(
                this,
                "Select a new time slot:",
                "Modify Appointment",
                JOptionPane.QUESTION_MESSAGE,
                null,
                labels,
                labels[0]
        );

        if (selectedLabel == null) return null;

        for (int i = 0; i < labels.length; i++) {
            if (labels[i].equals(selectedLabel)) return options.get(i);
        }

        return null;
    }

    /**
     * Prompts for an integer in range.
     *
     * @param title dialog title
     * @param message dialog message
     * @param min minimum
     * @param max maximum
     * @return value or null
     */
    private Integer promptIntInRange(String title, String message, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
            if (input == null) return null;

            input = input.trim();
            if (input.isEmpty()) {
                DialogUtil.show(this, "Invalid Input", "Value cannot be empty.", DialogUtil.Type.ERROR);
                continue;
            }

            int value;
            try {
                value = Integer.parseInt(input);
            } catch (Exception ex) {
                DialogUtil.show(this, "Invalid Input", "Please enter a valid number.", DialogUtil.Type.ERROR);
                continue;
            }

            if (value < min || value > max) {
                DialogUtil.show(this, "Out of Range", "Value must be between " + min + " and " + max + ".", DialogUtil.Type.ERROR);
                continue;
            }

            return value;
        }
    }

    /**
     * Computes slot length in minutes.
     *
     * @param slot slot
     * @return minutes between start and end, or 60 if missing
     */
    private int minutesBetween(TimeSlot slot) {
        if (slot == null || slot.getStartDateTime() == null || slot.getEndDateTime() == null) return 60;
        long mins = java.time.Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes();
        return (int) Math.max(1, mins);
    }
}
//