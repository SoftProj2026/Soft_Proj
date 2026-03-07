package presentation;

import Service.AuthService;
import Service.EmailSender;
import Service.SmtpEmailSender;
import domain.ContactRequest;
import domain.Provider;
import domain.User;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Customer UI window that lists providers and allows sending a message as a {@link ContactRequest}.
 *
 * <p>This screen supports two actions:</p>
 * <ul>
 *   <li>Storing the message internally as a {@link ContactRequest} in {@link DataRepository}.</li>
 *   <li>Sending the message by email to the company inbox address.</li>
 * </ul>
 *
 * <p>If the logged-in user has an email address, a copy of the message is also sent to the user.</p>
 */
public class CustomerContactProvidersFrame extends JFrame {

    private final AuthService auth;
    private final DataRepository repo;

    private final DefaultListModel<Provider> providersModel = new DefaultListModel<>();
    private final JList<Provider> providersList = new JList<>(providersModel);

    private final JTextArea messageArea = new JTextArea(6, 30);

    /**
     * Creates the customer contact window.
     *
     * @param auth authentication service
     * @param repo data repository
     */
    public CustomerContactProvidersFrame(AuthService auth, DataRepository repo) {
        this.auth = auth;
        this.repo = repo;

        setTitle("Contact Companies / Property Owners");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UITheme.BG);
        setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Send a message to a company / property owner");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Choose a provider then write your message and click Send.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setBorder(new EmptyBorder(12, 12, 12, 6));
        left.setBackground(UITheme.BG);

        JLabel providersLabel = new JLabel("Providers");
        providersLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        left.add(providersLabel, BorderLayout.NORTH);

        providersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        providersList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                         Object value,
                                                         int index,
                                                         boolean isSelected,
                                                         boolean cellHasFocus) {

                Provider p = (Provider) value;
                String text = p.getDisplayName() + "  (@" + p.getUsername() + ")";
                if (p.getEmail() != null && !p.getEmail().isEmpty()) {
                    text += " | " + p.getEmail();
                } else if (p.getPhone() != null && !p.getPhone().isEmpty()) {
                    text += " | " + p.getPhone();
                }
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        left.add(new JScrollPane(providersList), BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(8, 8));
        right.setBorder(new EmptyBorder(12, 6, 12, 12));
        right.setBackground(UITheme.BG);

        JLabel msgLabel = new JLabel("Message");
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        right.add(msgLabel, BorderLayout.NORTH);

        messageArea.setLineWrap(true);
        messageArea.setWrapStyleWord(true);
        right.add(new JScrollPane(messageArea), BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(1, 2, 12, 12));
        center.setBackground(UITheme.BG);
        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(UITheme.BG);

        JButton close = UITheme.secondaryButton("Close");
        close.addActionListener(e -> dispose());

        JButton send = UITheme.primaryButton("Send");
        send.addActionListener(e -> sendMessage());

        actions.add(close);
        actions.add(send);
        add(actions, BorderLayout.SOUTH);

        loadProviders();
    }

    /**
     * Loads providers from the repository into the list model.
     */
    private void loadProviders() {
        providersModel.clear();
        for (Provider p : repo.getProviders()) {
            providersModel.addElement(p);
        }
    }

    /**
     * Trims a string safely.
     *
     * @param s string (may be {@code null})
     * @return trimmed string, or empty string if {@code s} is {@code null}
     */
    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    /**
     * Sends the message to the company inbox email and stores it as a {@link ContactRequest}.
     *
     * <p>The message is always stored in the repository. Email sending is best-effort: if email fails,
     * the request is still saved and the user is informed.</p>
     */
    private void sendMessage() {
        if (auth == null || !auth.isLoggedIn() || auth.getCurrentUser() == null) {
            DialogUtil.show(this, "Login Required", "You must login first.", DialogUtil.Type.WARNING);
            return;
        }

        Provider selected = providersList.getSelectedValue();
        if (selected == null) {
            DialogUtil.show(this, "No Provider Selected", "Please select a provider first.", DialogUtil.Type.WARNING);
            return;
        }

        String msg = messageArea.getText() != null ? messageArea.getText().trim() : "";
        if (msg.isEmpty()) {
            DialogUtil.show(this, "Empty Message", "Please write a message before sending.", DialogUtil.Type.WARNING);
            return;
        }

        ContactRequest req = new ContactRequest(
                auth.getCurrentUser().getUsername(),
                selected.getUsername(),
                msg
        );
        repo.addContactRequest(req);

        try {
            EmailSender sender = new SmtpEmailSender();

            String companyTo = "remaajomaa842@gmail.com";

            User current = auth.getCurrentUser();
            String userEmail = safeTrim(current.getEmail());

            String subject = "New message from @" + current.getUsername();
            String body =
                    "From (username): @" + current.getUsername() + "\n" +
                            "From (email): " + (userEmail.isEmpty() ? "N/A" : userEmail) + "\n" +
                            "Provider selected in app: @" + selected.getUsername() + " (" + selected.getDisplayName() + ")\n\n" +
                            "Message:\n" + msg + "\n";

            sender.send(companyTo, companyTo, subject, body);

            if (!userEmail.isEmpty()) {
                sender.send(companyTo, userEmail, "Copy: " + subject, body);
            }

            messageArea.setText("");

            DialogUtil.show(
                    this,
                    "Sent",
                    "Message saved + email sent to company: " + companyTo
                            + (userEmail.isEmpty() ? "" : ("\nCopy sent to you: " + userEmail)),
                    DialogUtil.Type.SUCCESS
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            DialogUtil.show(
                    this,
                    "Email Failed",
                    "Message saved in app, but email sending failed.\n\n" +
                            "Reason:\n" + ex.getMessage() + "\n\n" +
                            "Check Eclipse Console for full stacktrace.",
                    DialogUtil.Type.ERROR
            );
        }
    }
}