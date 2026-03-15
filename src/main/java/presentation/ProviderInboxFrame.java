package presentation;

import Service.AuthService;
import domain.ContactRequest;
import domain.Provider;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Provider inbox window (Swing UI).
 * <p>
 * This screen is shown after a successful login when the current user is a
 * {@link Provider}. It displays incoming {@link ContactRequest} messages sent by
 * customers to that provider account.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Shows a table of all messages addressed to the logged-in provider.</li>
 *   <li>Allows the provider to refresh the inbox.</li>
 *   <li>Allows marking a selected message as "Read".</li>
 * </ul>
 *
 * <h2>Data Source</h2>
 * <ul>
 *   <li>Messages are fetched from {@link DataRepository#getRequestsForProvider(String)}.</li>
 *   <li>Read status changes are stored via {@link DataRepository#markRequestRead(int)}.</li>
 * </ul>
 *
 * <p>
 * If opened without a valid logged-in provider, the frame shows a warning and closes itself.
 * </p>
 * @author remaa
 * @version 1.0
 */
public class ProviderInboxFrame extends JFrame {

    /** Authentication service used to check login status and obtain current user. */
    private final AuthService auth;

    /** Repository used to fetch and update messages. */
    private final DataRepository repo;

    /** Table model used to render messages in the JTable. */
    private final DefaultTableModel model;

    /** Messages table UI component. */
    private final JTable table;

    /**
     * The list currently displayed in the table.
     * <p>
     * Used to map selected table rows back to the underlying {@link ContactRequest}.
     * </p>
     */
    private List<ContactRequest> visible = java.util.Collections.emptyList();

    /** Formatter for message timestamps in the table. */
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates the provider inbox window and immediately loads the inbox.
     *
     * @param auth authentication service
     * @param repo data repository containing contact requests/messages
     */
    public ProviderInboxFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("Provider Inbox");
        setSize(980, 460);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Inbox");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Messages sent to your provider account.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        String[] cols = {"ID", "From", "Date", "Read", "Message"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton refresh = UITheme.secondaryButton("Refresh");
        refresh.addActionListener(e -> loadInbox());

        JButton markRead = UITheme.secondaryButton("Mark as Read");
        markRead.addActionListener(e -> markSelectedRead());

        JButton close = UITheme.secondaryButton("Close");
        close.addActionListener(e -> dispose());

        actions.add(refresh);
        actions.add(markRead);
        actions.add(close);

        add(actions, BorderLayout.SOUTH);

        loadInbox();
    }

    /**
     * Returns the currently logged-in provider, or {@code null} if:
     * <ul>
     *   <li>No user is logged in</li>
     *   <li>The logged-in user is not a {@link Provider}</li>
     * </ul>
     *
     * @return provider user if logged in as provider; otherwise null
     */
    private Provider currentProviderOrNull() {
        if (auth == null || !auth.isLoggedIn() || auth.getCurrentUser() == null) return null;
        if (!(auth.getCurrentUser() instanceof Provider)) return null;
        return (Provider) auth.getCurrentUser();
    }

    /**
     * Loads the inbox messages for the current provider into the table.
     * <p>
     * If the user is not a provider (or not logged in), a warning is shown and
     * the frame closes itself.
     * </p>
     */
    private void loadInbox() {
        model.setRowCount(0);

        Provider p = currentProviderOrNull();
        if (p == null) {
            DialogUtil.show(
                    this,
                    "Not a Provider",
                    "You must login as a provider to view inbox.",
                    DialogUtil.Type.WARNING
            );
            dispose();
            return;
        }

        visible = repo.getRequestsForProvider(p.getUsername());

        for (ContactRequest r : visible) {
            model.addRow(new Object[] {
                    r.getId(),
                    r.getFromUsername(),
                    r.getCreatedAt().format(fmt),
                    r.isRead() ? "YES" : "NO",
                    r.getMessage()
            });
        }
    }

    /**
     * Marks the selected message as read.
     * <p>
     * This updates the repository by calling {@link DataRepository#markRequestRead(int)}
     * and then reloads the inbox table.
     * </p>
     */
    private void markSelectedRead() {
        int row = table.getSelectedRow();
        if (row < 0 || row >= visible.size()) {
            DialogUtil.show(this, "No Selection", "Please select a message first.", DialogUtil.Type.WARNING);
            return;
        }

        ContactRequest r = visible.get(row);
        repo.markRequestRead(r.getId());

        loadInbox();
    }
}