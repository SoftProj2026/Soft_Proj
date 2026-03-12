package presentation;

import domain.Appointment;
import domain.AuditEvent;
import domain.ContactRequest;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * Admin UI window that displays system-wide user activity.
 *
 * <p>This frame shows three tabs:</p>
 * <ol>
 *   <li><b>Messages</b>: all {@link ContactRequest} messages sent by customers</li>
 *   <li><b>Appointments</b>: all appointments and their timestamps/status</li>
 *   <li><b>Audit Log</b>: summarized events written into {@link AuditEvent}</li>
 * </ol>
 *
 * <p>This makes it possible for the admin to monitor:</p>
 * <ul>
 *   <li>Who sent which messages and when</li>
 *   <li>Who booked which category and when booking was confirmed</li>
 *   <li>Who cancelled and in which category</li>
 * </ul>
 */
public class AdminActivityFrame extends JFrame {

    private final DataRepository repo;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates the admin activity window.
     *
     * @param repo data repository containing messages, appointments, and audit events
     */
    public AdminActivityFrame(DataRepository repo) {
        this.repo = repo;

        setTitle("Admin - User Activity");
        setSize(1150, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("User Activity (Messages / Bookings / Cancellations)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Admin view: see messages, all bookings, and audit log events.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Messages", buildMessagesPanel());
        tabs.addTab("Appointments", buildAppointmentsPanel());
        tabs.addTab("Audit Log", buildAuditPanel());
        add(tabs, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> {
            dispose();
            new AdminActivityFrame(repo).setVisible(true);
        });

        JButton close = UITheme.secondaryButton("Close");
        close.addActionListener(e -> dispose());

        actions.add(refresh);
        actions.add(close);

        add(actions, BorderLayout.SOUTH);
    }

    /**
     * Builds Messages tab showing all contact requests.
     *
     * @return panel containing the messages table
     */
    private JPanel buildMessagesPanel() {
        String[] cols = {"ID", "From", "To Provider", "Date", "Message"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);

        for (ContactRequest r : repo.getContactRequests()) {
            m.addRow(new Object[]{
                    r.getId(),
                    r.getFromUsername(),
                    r.getToProviderUsername(),
                    r.getCreatedAt().format(fmt),
                    r.getMessage()
            });
        }

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds Appointments tab showing all appointments with status and timestamps.
     *
     * @return panel containing the appointments table
     */
    private JPanel buildAppointmentsPanel() {
        String[] cols = {"ID", "User", "Category", "Slot Start", "Created At", "Confirmed At", "Cancelled At", "Status"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);

        for (Appointment a : repo.getAppointments()) {
            String user = (a.getUser() != null) ? a.getUser().getUsername() : "N/A";
            String category = (a.getSlot() != null && a.getSlot().getCategory() != null)
                    ? a.getSlot().getCategory().getName()
                    : "N/A";
            String slotStart = (a.getSlot() != null && a.getSlot().getStartDateTime() != null)
                    ? a.getSlot().getStartDateTime().format(fmt)
                    : "N/A";

            String createdAt = (a.getCreatedAt() != null) ? a.getCreatedAt().format(fmt) : "";
            String confirmedAt = (a.getConfirmedAt() != null) ? a.getConfirmedAt().format(fmt) : "";
            String cancelledAt = (a.getCancelledAt() != null) ? a.getCancelledAt().format(fmt) : "";

            m.addRow(new Object[]{
                    a.getId(),
                    user,
                    category,
                    slotStart,
                    createdAt,
                    confirmedAt,
                    cancelledAt,
                    a.getStatus().name()
            });
        }

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    /**
     * Builds Audit Log tab showing summarized events.
     *
     * @return panel containing the audit log table
     */
    private JPanel buildAuditPanel() {
        String[] cols = {"ID", "Type", "Actor", "Target", "Date", "Details"};
        DefaultTableModel m = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable t = new JTable(m);

        for (AuditEvent e : repo.getAuditEvents()) {
            m.addRow(new Object[]{
                    e.getId(),
                    e.getType().name(),
                    e.getActorUsername(),
                    e.getTarget(),
                    e.getAt().format(fmt),
                    e.getDetails()
            });
        }

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }
}