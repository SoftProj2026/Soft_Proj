package presentation;

import Service.AuthService;
import domain.ContactRequest;
import domain.Provider;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Customer UI: list providers and allow sending a contact request/message.
 */
public class CustomerContactProvidersFrame extends JFrame {

    private final AuthService auth;
    private final DataRepository repo;

    private final DefaultListModel<Provider> providersModel = new DefaultListModel<>();
    private final JList<Provider> providersList = new JList<>(providersModel);

    private final JTextArea messageArea = new JTextArea(6, 30);

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

        // Left: providers list
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
                if (p.getPhone() != null && !p.getPhone().isEmpty()) text += " | " + p.getPhone();
                return super.getListCellRendererComponent(list, text, index, isSelected, cellHasFocus);
            }
        });

        left.add(new JScrollPane(providersList), BorderLayout.CENTER);

        // Right: message
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

    private void loadProviders() {
        providersModel.clear();
        for (Provider p : repo.getProviders()) {
            providersModel.addElement(p);
        }
    }

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
        messageArea.setText("");

        DialogUtil.show(
                this,
                "Sent",
                "Your message has been sent to \"" + selected.getDisplayName() + "\".",
                DialogUtil.Type.SUCCESS
        );
    }
}