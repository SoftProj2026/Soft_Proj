package presentation;

import domain.Administrator;
import domain.BookingRequest;
import domain.BookingRequestStatus;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Approval requests window used by administrators.
 * <p>
 * This screen supports a two-step approval workflow:
 * </p>
 * <ol>
 *   <li>Category admin reviews requests assigned to them and approves/rejects.</li>
 *   <li>Big admin reviews category-approved requests and performs final approval/rejection.</li>
 * </ol>
 *
 * <p>
 * Visibility rules:
 * </p>
 * <ul>
 *   <li>Category admins see only {@link BookingRequestStatus#PENDING_CATEGORY_ADMIN} requests assigned to their username.</li>
 *   <li>The big admin (username {@code admin}) sees only {@link BookingRequestStatus#PENDING_BIG_ADMIN} requests.</li>
 * </ul>
 */
public class AdminRequestsFrame extends JFrame {

    private final DataRepository repo;
    private final Administrator adminUser;

    private final DefaultTableModel model;
    private final JTable table;

    private List<BookingRequest> visible = Collections.emptyList();

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates the approval requests window for the given administrator.
     *
     * @param repo      data repository
     * @param adminUser logged-in administrator
     */
    public AdminRequestsFrame(DataRepository repo, Administrator adminUser) {
        this.repo = repo;
        this.adminUser = adminUser;

        setTitle("Approval Requests");
        setSize(1100, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Approval Requests");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel(roleHint());
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        String[] cols = {
                "Request ID",
                "Requester",
                "Category",
                "Slot Start",
                "Duration",
                "Participants",
                "Status",
                "Assigned Category Admin"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
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

        JButton approve = UITheme.primaryButton("Approve");
        approve.addActionListener(e -> approveSelected());

        JButton reject = UITheme.secondaryButton("Reject");
        reject.addActionListener(e -> rejectSelected());

        JButton close = UITheme.secondaryButton("Close");
        close.addActionListener(e -> dispose());

        actions.add(refresh);
        actions.add(reject);
        actions.add(approve);
        actions.add(close);

        add(actions, BorderLayout.SOUTH);

        load();
    }

    /**
     * Indicates whether the current administrator is the big admin.
     *
     * @return true if username is {@code admin}; otherwise false
     */
    private boolean isBigAdmin() {
        return adminUser != null
                && adminUser.getUsername() != null
                && adminUser.getUsername().equalsIgnoreCase("admin");
    }

    /**
     * Returns a role-specific subtitle used in the header.
     *
     * @return role hint text
     */
    private String roleHint() {
        if (isBigAdmin()) {
            return "QR Admin for Company view: final approve/reject of requests approved by category admins.";
        }
        return "Category Admin view: approve/reject requests assigned to you. (Approve -> sent to QR Admin for Company)";
    }

    /**
     * Loads requests visible to the current administrator into the table.
     */
    private void load() {
        model.setRowCount(0);

        if (adminUser == null) {
            visible = Collections.emptyList();
            return;
        }

        if (isBigAdmin()) {
            visible = repo.getRequestsForBigAdmin();
        } else {
            visible = repo.getRequestsForCategoryAdmin(adminUser.getUsername());
        }

        for (BookingRequest r : visible) {
            String requester = (r.getRequester() != null) ? r.getRequester().getUsername() : "N/A";
            String category = (r.getSlot() != null && r.getSlot().getCategory() != null)
                    ? r.getSlot().getCategory().getName()
                    : "N/A";
            String start = (r.getSlot() != null && r.getSlot().getStartDateTime() != null)
                    ? r.getSlot().getStartDateTime().format(fmt)
                    : "N/A";

            model.addRow(new Object[]{
                    r.getId(),
                    requester,
                    category,
                    start,
                    r.getDurationInMinutes(),
                    r.getParticipants(),
                    r.getStatus().name(),
                    r.getCategoryAdminUsername()
            });
        }
    }

    /**
     * Returns the booking request selected in the table.
     *
     * @return selected request or null if none selected
     */
    private BookingRequest selectedOrNull() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visible.size()) return null;
        return visible.get(row);
    }

    /**
     * Approves the selected request as the current admin role.
     */
    private void approveSelected() {
        BookingRequest r = selectedOrNull();
        if (r == null) {
            DialogUtil.show(this, "No Selection", "Please select a request first.", DialogUtil.Type.WARNING);
            return;
        }

        String msg;

        if (isBigAdmin()) {
            if (r.getStatus() != BookingRequestStatus.PENDING_BIG_ADMIN) {
                DialogUtil.show(this, "Not Allowed", "This request is not pending QR Admin for Company.", DialogUtil.Type.WARNING);
                return;
            }
            msg = repo.approveByBigAdmin(r.getId(), adminUser.getUsername());
        } else {
            if (r.getStatus() != BookingRequestStatus.PENDING_CATEGORY_ADMIN) {
                DialogUtil.show(this, "Not Allowed", "This request is not pending category admin.", DialogUtil.Type.WARNING);
                return;
            }
            msg = repo.approveByCategoryAdmin(r.getId(), adminUser.getUsername());
        }

        DialogUtil.show(this, "Result", msg, DialogUtil.Type.INFO);
        load();
    }

    /**
     * Rejects the selected request as the current admin role.
     */
    private void rejectSelected() {
        BookingRequest r = selectedOrNull();
        if (r == null) {
            DialogUtil.show(this, "No Selection", "Please select a request first.", DialogUtil.Type.WARNING);
            return;
        }

        String reason = JOptionPane.showInputDialog(
                this,
                "Enter reject reason (optional):",
                "Reject Request",
                JOptionPane.QUESTION_MESSAGE
        );
        if (reason == null) return;

        String msg;

        if (isBigAdmin()) {
            if (r.getStatus() != BookingRequestStatus.PENDING_BIG_ADMIN) {
                DialogUtil.show(this, "Not Allowed", "This request is not pending QR Admin for Company.", DialogUtil.Type.WARNING);
                return;
            }
            msg = repo.rejectByBigAdmin(r.getId(), adminUser.getUsername(), reason);
        } else {
            if (r.getStatus() != BookingRequestStatus.PENDING_CATEGORY_ADMIN) {
                DialogUtil.show(this, "Not Allowed", "This request is not pending category admin.", DialogUtil.Type.WARNING);
                return;
            }
            msg = repo.rejectByCategoryAdmin(r.getId(), adminUser.getUsername(), reason);
        }

        DialogUtil.show(this, "Result", msg, DialogUtil.Type.INFO);
        load();
    }
}