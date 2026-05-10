package presentation;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.BookingRequest;
import domain.BookingRequestStatus;
import domain.TimeSlot;
import persistence.DataRepository;
import service.AppointmentTypeService;
import service.AuthService;
import service.SmtpEmailSender;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Displays the currently logged-in user's appointments and booking requests in a single table.
 *
 * <p>
 * UI Update:
 * <ul>
 *   <li>Uses UITheme colors/background (consistent look)</li>
 *   <li>Buttons styled using UITheme helper methods</li>
 *   <li>Adds header (title + subtitle)</li>
 *   <li>Fixes formatting/line breaks (no "one-line" messy code)</li>
 * </ul>
 *
 * @author Qussaialaw &amp; remaa
 * @version 1.0
 */
public class MyBookingsFrame extends JFrame {

    private final AuthService auth;
    private final DataRepository repo;

    private final DefaultTableModel model;
    private final JTable table;

    private final List<RowItem> visibleItems = new ArrayList<>();

    private static final Color CANCELLED_FG = new Color(180, 30, 30);
    private static final Color CONFIRMED_FG = new Color(20, 83, 45);
    private static final Color COMPLETED_FG = new Color(120, 120, 120);

    private static final Color REQUEST_PENDING_FG = new Color(30, 64, 175);
    private static final Color REQUEST_APPROVED_FG = new Color(20, 83, 45);
    private static final Color REQUEST_REJECTED_FG = new Color(180, 30, 30);

    public MyBookingsFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("My Bookings");
        setSize(1300, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        getContentPane().setBackground(UITheme.BG);

        add(buildHeader(), BorderLayout.NORTH);

        String[] cols = {
                "Type",
                "ID",
                "Category",
                "Start",
                "Duration",
                "Participants",
                "Status",
                "Appointment Type",
                "Group Size",
                "Rejected By",
                "Reject Reason"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);

        installRowRenderer();

        installOpenOptionsOnDoubleClick();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
        scroll.getViewport().setBackground(UITheme.BG);
        add(scroll, BorderLayout.CENTER);

        add(buildActions(), BorderLayout.SOUTH);

        loadMyBookings();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("My Bookings");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("View your appointments and requests. Double-click a CONFIRMED appointment to set options.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    private JPanel buildActions() {
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton refreshBtn = UITheme.secondaryButton("Refresh");
        JButton modifyBtn = UITheme.primaryButton("Modify Booking");
        JButton cancelBtn = UITheme.secondaryButton("Cancel Booking");
        JButton closeBtn = UITheme.secondaryButton("Close");

        refreshBtn.addActionListener(e -> loadMyBookings());
        modifyBtn.addActionListener(e -> modifySelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        closeBtn.addActionListener(e -> dispose());

        actions.add(refreshBtn);
        actions.add(modifyBtn);
        actions.add(cancelBtn);
        actions.add(closeBtn);

        return actions;
    }

    private static class RowItem {

        enum Kind {
            APPOINTMENT,
            REQUEST
        }

        private final Kind kind;
        private final Appointment appointment;
        private final BookingRequest request;

        RowItem(Appointment a) {
            this.kind = Kind.APPOINTMENT;
            this.appointment = a;
            this.request = null;
        }

        RowItem(BookingRequest r) {
            this.kind = Kind.REQUEST;
            this.request = r;
            this.appointment = null;
        }

        Kind getKind() {
            return kind;
        }

        Appointment getAppointment() {
            return appointment;
        }

        BookingRequest getRequest() {
            return request;
        }
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

                if (!isSelected && column == 6 && row >= 0 && row < visibleItems.size()) {
                    RowItem item = visibleItems.get(row);

                    if (item.getKind() == RowItem.Kind.APPOINTMENT) {
                        Appointment a = item.getAppointment();
                        if (a != null) {
                            if (a.getStatus() == AppointmentStatus.CANCELLED) {
                                c.setForeground(CANCELLED_FG);
                            } else if (a.getStatus() == AppointmentStatus.CONFIRMED) {
                                c.setForeground(CONFIRMED_FG);
                            } else if (a.getStatus() == AppointmentStatus.COMPLETED) {
                                c.setForeground(COMPLETED_FG);
                            }
                        }
                    } else {
                        BookingRequest r = item.getRequest();
                        if (r != null) {
                            BookingRequestStatus s = r.getStatus();
                            if (s == BookingRequestStatus.PENDING_CATEGORY_ADMIN || s == BookingRequestStatus.PENDING_BIG_ADMIN) {
                                c.setForeground(REQUEST_PENDING_FG);
                            } else if (s == BookingRequestStatus.APPROVED_AND_CONFIRMED) {
                                c.setForeground(REQUEST_APPROVED_FG);
                            } else if (s == BookingRequestStatus.REJECTED_CATEGORY_ADMIN || s == BookingRequestStatus.REJECTED_BIG_ADMIN) {
                                c.setForeground(REQUEST_REJECTED_FG);
                            }
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

     void installOpenOptionsOnDoubleClick() {
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2 || !SwingUtilities.isLeftMouseButton(e)) return;

                int row = table.rowAtPoint(e.getPoint());
                if (row < 0 || row >= visibleItems.size()) return;

                table.setRowSelectionInterval(row, row);

                RowItem item = visibleItems.get(row);
                if (item.getKind() != RowItem.Kind.APPOINTMENT || item.getAppointment() == null) {
                    JOptionPane.showMessageDialog(MyBookingsFrame.this,
                            "This row is not an appointment.\nYou can only set options for CONFIRMED appointments.");
                    return;
                }

                Appointment selected = item.getAppointment();

                if (selected.getStatus() != AppointmentStatus.CONFIRMED) {
                    JOptionPane.showMessageDialog(MyBookingsFrame.this,
                            "This booking is not CONFIRMED.\nOnly CONFIRMED bookings have options.");
                    return;
                }

                Object[] options = {"Cancel Booking", "Modify Booking", "More Options", "Close"};
                int choice = JOptionPane.showOptionDialog(
                        MyBookingsFrame.this,
                        "Choose an action for the selected CONFIRMED booking:",
                        "Booking Options",
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]
                );

                if (choice == 0) {
                    cancelSelected();
                } else if (choice == 1) {
                    modifySelected();
                } else if (choice == 2) {
                    AppointmentTypeService typeService = new AppointmentTypeService(repo, new SmtpEmailSender());
                    AppointmentOptionsFrame f = new AppointmentOptionsFrame(MyBookingsFrame.this, repo, selected, typeService);

                    f.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            loadMyBookings();
                        }
                    });

                    f.setVisible(true);
                }
            }
        });
    }

    private void markPastAppointmentsCompleted(String username) {
        if (username == null || username.trim().isEmpty()) return;

        LocalDateTime now = LocalDateTime.now();

        for (Appointment a : repo.getAppointments()) {
            if (a == null) continue;
            if (a.getStatus() != AppointmentStatus.CONFIRMED) continue;
            if (a.getUser() == null || a.getUser().getUsername() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            if (a.getSlot() == null || a.getSlot().getEndDateTime() == null) continue;

            if (a.getSlot().getEndDateTime().isBefore(now)) {
                a.complete();
            }
        }
    }

    private void loadMyBookings() {
        model.setRowCount(0);
        visibleItems.clear();

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "You must login first.");
            dispose();
            return;
        }

        String username = auth.getCurrentUser().getUsername();

        markPastAppointmentsCompleted(username);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (BookingRequest r : repo.getBookingRequests()) {
            if (r == null || r.getRequester() == null || r.getRequester().getUsername() == null) continue;
            if (!r.getRequester().getUsername().equalsIgnoreCase(username)) continue;

            String categoryName = "N/A";
            String startStr = "N/A";

            if (r.getSlot() != null) {
                if (r.getSlot().getCategory() != null && r.getSlot().getCategory().getName() != null) {
                    categoryName = r.getSlot().getCategory().getName();
                }
                if (r.getSlot().getStartDateTime() != null) {
                    startStr = r.getSlot().getStartDateTime().format(fmt);
                }
            }

            String rejectedBy = "";
            if (r.getStatus() == BookingRequestStatus.REJECTED_CATEGORY_ADMIN) {
                rejectedBy = safe(r.getCategoryAdminActor());
            } else if (r.getStatus() == BookingRequestStatus.REJECTED_BIG_ADMIN) {
                rejectedBy = safe(r.getBigAdminActor());
            }

            String rejectReason = safe(r.getRejectReason());

            visibleItems.add(new RowItem(r));
            model.addRow(new Object[]{
                    "REQUEST",
                    r.getId(),
                    categoryName,
                    startStr,
                    r.getDurationInMinutes(),
                    r.getParticipants(),
                    r.getStatus().name(),
                    "",
                    "",
                    rejectedBy,
                    rejectReason
            });
        }

        for (Appointment a : repo.getAppointments()) {
            if (a == null || a.getUser() == null || a.getUser().getUsername() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            String categoryName = "N/A";
            String startStr = "N/A";

            if (a.getSlot() != null) {
                if (a.getSlot().getCategory() != null && a.getSlot().getCategory().getName() != null) {
                    categoryName = a.getSlot().getCategory().getName();
                }
                if (a.getSlot().getStartDateTime() != null) {
                    startStr = a.getSlot().getStartDateTime().format(fmt);
                }
            }

            String type = (a.getAppointmentType() != null) ? a.getAppointmentType().name() : "";
            String groupSize = (a.getGroupSize() != null) ? String.valueOf(a.getGroupSize()) : "";

            visibleItems.add(new RowItem(a));
            model.addRow(new Object[]{
                    "APPOINTMENT",
                    a.getId(),
                    categoryName,
                    startStr,
                    a.getDurationInMinutes(),
                    a.getParticipants(),
                    a.getStatus().name(),
                    type,
                    groupSize,
                    "",
                    ""
            });
        }

        table.repaint();
    }

    private void cancelSelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.");
            return;
        }

        RowItem item = visibleItems.get(row);

        if (item.getKind() != RowItem.Kind.APPOINTMENT || item.getAppointment() == null) {
            JOptionPane.showMessageDialog(this, "You can only cancel CONFIRMED appointments (not booking requests).");
            return;
        }

        Appointment selected = item.getAppointment();

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

    private void modifySelected() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visibleItems.size()) {
            JOptionPane.showMessageDialog(this, "Please select a booking first.");
            return;
        }

        RowItem item = visibleItems.get(row);

        if (item.getKind() != RowItem.Kind.APPOINTMENT || item.getAppointment() == null) {
            JOptionPane.showMessageDialog(this, "You can only modify CONFIRMED appointments (not booking requests).");
            return;
        }

        Appointment selected = item.getAppointment();

        if (selected.getStatus() != AppointmentStatus.CONFIRMED) {
            JOptionPane.showMessageDialog(this, "This booking is not CONFIRMED.");
            return;
        }

        if (selected.getSlot() == null || selected.getSlot().getStartDateTime() == null) {
            JOptionPane.showMessageDialog(this, "This booking has no slot time.");
            return;
        }

        if (!selected.getSlot().getStartDateTime().isAfter(LocalDateTime.now())) {
            JOptionPane.showMessageDialog(this, "Only FUTURE bookings can be modified.");
            return;
        }

        TimeSlot newSlot = promptNewSlot(selected);
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
                "Confirm modifying this booking to the new slot?",
                "Confirm Modification",
                JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) return;

        String actor = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUsername() : "";
        String msg = repo.modifyAppointment(selected, newSlot, duration, participants, actor);
        JOptionPane.showMessageDialog(this, msg);

        loadMyBookings();
    }

    private TimeSlot promptNewSlot(Appointment selected) {
        if (selected == null || selected.getSlot() == null || selected.getSlot().getCategory() == null) {
            JOptionPane.showMessageDialog(this, "Cannot modify (missing category).");
            return null;
        }

        List<TimeSlot> options = new ArrayList<>();
        for (TimeSlot slot : repo.getSlots()) {
            if (slot == null || slot.getStartDateTime() == null) continue;
            if (slot.getCategory() == null || slot.getCategory().getName() == null) continue;

            if (!slot.getCategory().getName().equalsIgnoreCase(selected.getSlot().getCategory().getName())) continue;
            if (!slot.isAvailable()) continue;
            if (!slot.getStartDateTime().isAfter(LocalDateTime.now())) continue;

            options.add(slot);
        }

        if (options.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No available future slots found for this category.");
            return null;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String[] labels = new String[options.size()];
        for (int i = 0; i < options.size(); i++) {
            labels[i] = options.get(i).getStartDateTime().format(fmt);
        }

        String selectedLabel = (String) JOptionPane.showInputDialog(
                this,
                "Select a new time slot:",
                "Modify Booking",
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

    private Integer promptIntInRange(String title, String message, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
            if (input == null) return null;

            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Value cannot be empty.");
                continue;
            }

            int value;
            try {
                value = Integer.parseInt(input);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                continue;
            }

            if (value < min || value > max) {
                JOptionPane.showMessageDialog(this, "Value must be between " + min + " and " + max + ".");
                continue;
            }

            return value;
        }
    }

    private int minutesBetween(TimeSlot slot) {
        if (slot == null || slot.getStartDateTime() == null || slot.getEndDateTime() == null) return 60;
        long mins = java.time.Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes();
        return (int) Math.max(1, mins);
    }

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}