package presentation;

import Service.AiBookingAssistantService;
import Service.AuthService;
import Service.BookingResult;
import Service.BookingService;
import Service.EmailSender;
import Service.SmtpEmailSender;
import domain.Category;
import domain.TimeSlot;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BookingTypeChoiceDialog extends JDialog {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;
    private final Category category;

    private static final String COMPANY_EMAIL = "remaajomaa842@gmail.com";
    private static final String COMPANY_EMERGENCY_PHONE = "059-507-9549";
    private static final String DEFAULT_USER_EMAIL = "remaajomaa70@gmail.com";

    private static final Color BTN_COLOR = UITheme.PRIMARY_DARK;
    private static final Color BTN_HOVER = new Color(25, 95, 190);
    private static final Color BG = Color.WHITE;

    public BookingTypeChoiceDialog(JFrame parent,
                                   Category category,
                                   DataRepository repo,
                                   AuthService auth,
                                   BookingService booking) {
        super(parent, "Choose Booking Type", true);

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;
        this.category = category;

        setLayout(new BorderLayout(12, 12));
        setSize(680, 560);
        setResizable(false);
        setLocationRelativeTo(parent);

        getContentPane().setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildButtonsPanel(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(18, 22, 8, 22));

        JLabel title = new JLabel("Choose Booking Type", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));

        JLabel sub = new JLabel("Category: " + escapeHtml(category.getName()), SwingConstants.CENTER);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sub.setForeground(UITheme.MUTED);

        header.add(title);
        header.add(Box.createVerticalStrut(6));
        header.add(sub);

        return header;
    }

    private JPanel buildButtonsPanel() {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(BG);
        wrap.setBorder(new EmptyBorder(10, 22, 10, 22));

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setBackground(BG);
        buttonsPanel.setLayout(new GridLayout(0, 1, 12, 12));

        JButton emergencyBtn = bigPrimaryButton("Emergency Booking");
        emergencyBtn.addActionListener(e -> openEmergencyQuickDialog());

        JButton newBookingBtn = bigPrimaryButton("New Booking");
        newBookingBtn.addActionListener(e -> {
            dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setVisible(true);
        });

       /* JButton reviewBtn = bigPrimaryButton("Review Booking");
        reviewBtn.addActionListener(e -> {
            dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setVisible(true);
        });*/

        JButton individualBtn = bigPrimaryButton("Individual Booking");
        individualBtn.addActionListener(e -> {
            dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setForceIndividual(true);
            ub.setForcedParticipantCount(1);
            ub.setVisible(true);
        });

        JButton groupBtn = bigPrimaryButton("Group Booking (1–5 participants)");
        groupBtn.addActionListener(e -> {
            dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setVisible(true);
        });

        JButton aiBtn = bigPrimaryButton("AI Booking (Suggest 5 slots + Send Request)");
        aiBtn.addActionListener(e -> openAiSuggestDialog());

        buttonsPanel.add(emergencyBtn);
        buttonsPanel.add(newBookingBtn);
       // buttonsPanel.add(reviewBtn);
        buttonsPanel.add(individualBtn);
        buttonsPanel.add(groupBtn);
        buttonsPanel.add(aiBtn);

        JScrollPane scroll = new JScrollPane(buttonsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.getViewport().setBackground(BG);

        wrap.add(scroll, BorderLayout.CENTER);
        return wrap;
    }

    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(BG);
        bottom.setBorder(new EmptyBorder(0, 16, 16, 16));

        JButton close = UITheme.secondaryButton("Close");
        close.setFont(new Font("Segoe UI", Font.BOLD, 14));
        close.addActionListener(e -> dispose());

        bottom.add(close);
        return bottom;
    }

    private JButton bigPrimaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(16, 18, 16, 18));
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setUI(new RoundedButtonUI(18, BTN_COLOR, BTN_HOVER));

        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                b.putClientProperty("hover", true);
                b.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                b.putClientProperty("hover", false);
                b.repaint();
            }
        });

        return b;
    }

    private static class RoundedButtonUI extends javax.swing.plaf.basic.BasicButtonUI {

        private final int radius;
        private final Color normal;
        private final Color hover;

        RoundedButtonUI(int radius, Color normal, Color hover) {
            this.radius = radius;
            this.normal = normal;
            this.hover = hover;
        }

        @Override
        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            boolean isHover = Boolean.TRUE.equals(b.getClientProperty("hover"));
            Color bg = isHover ? hover : normal;

            int w = b.getWidth();
            int h = b.getHeight();

            g2.setColor(new Color(0, 0, 0, 25));
            g2.fillRoundRect(2, 3, w - 4, h - 4, radius, radius);

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, w - 1, h - 1, radius, radius);

            g2.setColor(new Color(255, 255, 255, 50));
            g2.drawRoundRect(0, 0, w - 1, h - 1, radius, radius);

            g2.dispose();
            super.paint(g, c);
        }
    }

    private void openAiSuggestDialog() {
        if (auth == null || !auth.isLoggedIn() || auth.getCurrentUser() == null) {
            JOptionPane.showMessageDialog(this, "You must login first.");
            return;
        }

        Integer participants = promptIntInRange(
                "AI Booking - Participants",
                "Enter participants (1 - 5):",
                1, 5,
                1
        );
        if (participants == null) return;

        Integer duration = promptIntInRange(
                "AI Booking - Duration",
                "Enter duration in minutes:",
                1, 60,
                30
        );
        if (duration == null) return;

        AiBookingAssistantService ai = new AiBookingAssistantService(repo);

        List<TimeSlot> suggestions = ai.suggestTopMutualSlots(auth.getCurrentUser(), category, 5);
        if (suggestions.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "AI could not find any suitable mutual slot for this category.",
                    "AI Booking",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String[] options = new String[suggestions.size()];
        for (int i = 0; i < suggestions.size(); i++) {
            TimeSlot s = suggestions.get(i);
            String start = (s.getStartDateTime() != null) ? s.getStartDateTime().format(fmt) : "N/A";
            String end = (s.getEndDateTime() != null) ? s.getEndDateTime().format(fmt) : "N/A";
            options[i] = (i + 1) + ") " + start + " → " + end;
        }

        String choice = (String) JOptionPane.showInputDialog(
                this,
                "AI suggested the following 5 slots. Please pick one:",
                "AI Booking - Choose Slot",
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );

        if (choice == null) return;

        int idx = 0;
        try {
            int paren = choice.indexOf(')');
            if (paren > 0) idx = Integer.parseInt(choice.substring(0, paren).trim()) - 1;
        } catch (Exception ignored) {
        }
        if (idx < 0 || idx >= suggestions.size()) idx = 0;

        TimeSlot selectedSlot = suggestions.get(idx);

        BookingResult res = ai.sendRequestForSlot(
                auth.getCurrentUser(),
                selectedSlot,
                duration,
                participants
        );

        JOptionPane.showMessageDialog(
                this,
                res.getMessage(),
                res.isSuccess() ? "AI Booking" : "AI Booking Failed",
                res.isSuccess() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE
        );

        if (res.isSuccess()) dispose();
    }

    private Integer promptIntInRange(String title, String message, int min, int max, int defaultValue) {
        while (true) {
            String input = JOptionPane.showInputDialog(this, message, String.valueOf(defaultValue));
            if (input == null) return null;

            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Value cannot be empty.");
                continue;
            }

            int val;
            try {
                val = Integer.parseInt(input);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
                continue;
            }

            if (val < min || val > max) {
                JOptionPane.showMessageDialog(this, "Value must be between " + min + " and " + max + ".");
                continue;
            }

            return val;
        }
    }

    private void openEmergencyQuickDialog() {
        JDialog dlg = new JDialog(this, "Emergency - Preferred Time", true);
        dlg.setLayout(new BorderLayout(8, 8));
        dlg.setSize(520, 340);
        dlg.setLocationRelativeTo(this);

        JLabel lbl = new JLabel("<html><b>Select preferred emergency time (optional)</b></html>");
        lbl.setBorder(BorderFactory.createEmptyBorder(10, 12, 0, 12));
        dlg.add(lbl, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(8, 8));
        center.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem("No preferred time");
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        List<TimeSlot> slots = repo.getSlots();
        List<TimeSlot> choices = new ArrayList<>();
        for (TimeSlot s : slots) {
            if (s == null || s.getStartDateTime() == null) continue;
            if (s.getCategory() == null || s.getCategory().getName() == null) continue;
            if (!s.getCategory().getName().equalsIgnoreCase(category.getName())) continue;
            if (!s.isAvailable()) continue;
            if (!s.getStartDateTime().isAfter(java.time.LocalDateTime.now())) continue;
            choices.add(s);
        }
        for (TimeSlot t : choices) cb.addItem(t.getStartDateTime().format(fmt));

        center.add(cb, BorderLayout.NORTH);

        JTextArea note = new JTextArea(5, 30);
        note.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setBorder(BorderFactory.createTitledBorder("Additional note (optional)"));
        center.add(new JScrollPane(note), BorderLayout.CENTER);

        dlg.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton send = UITheme.primaryButton("Send Emergency Email");
        JButton cancel = UITheme.secondaryButton("Cancel");
        actions.add(cancel);
        actions.add(send);
        dlg.add(actions, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());

        send.addActionListener(e -> {
            String userEmail = DEFAULT_USER_EMAIL;
            try {
                if (auth != null && auth.getCurrentUser() != null) {
                    String uEmail = auth.getCurrentUser().getEmail();
                    if (uEmail != null && !uEmail.trim().isEmpty()) userEmail = uEmail;
                }
            } catch (Exception ex) {
            }

            String preferred = (String) cb.getSelectedItem();
            if ("No preferred time".equals(preferred)) preferred = "No preference";

            String reference = "EMG-" + System.currentTimeMillis() % 1000000;

            String cat = category.getName();
            String selected = preferred;

            String body = "Your appointment has been marked as EMERGENCY.\n"
                    + "Reference: #" + reference + "\n"
                    + "Category: " + cat + "\n"
                    + "Preferred emergency time: " + selected + "\n"
                    + "Company contact phone: " + COMPANY_EMERGENCY_PHONE + "\n";

            String subject = "Emergency Notification - " + cat + " - Ref #" + reference;

            EmailSender sender = createEmailSender();

            if (userEmail != null && !userEmail.trim().isEmpty()) {
                try {
                    sender.send(SmtpEmailSender.getCompanyEmail(), userEmail, subject, body);
                } catch (Exception ex) {
                }
            }

            try {
                sender.send(SmtpEmailSender.getCompanyEmail(),
                        COMPANY_EMAIL,
                        subject + " (Company copy)",
                        body + "\nSubmitted by: " + userEmail);
            } catch (Exception ex) {
            }

            JOptionPane.showMessageDialog(dlg,
                    "Emergency notification sent.\nReference: " + reference,
                    "Sent",
                    JOptionPane.INFORMATION_MESSAGE);

            dlg.dispose();
            BookingTypeChoiceDialog.this.dispose();
        });

        dlg.setVisible(true);
    }

    private EmailSender createEmailSender() {
        try {
            return new SmtpEmailSender();
        } catch (Throwable t) {
            return new ConsoleEmailSender();
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static class ConsoleEmailSender implements EmailSender {
        @Override
        public void send(String fromIgnored, String to, String subject, String body) {
            System.out.println("=== ConsoleEmailSender ===");
            System.out.println("From (logical): " + fromIgnored);
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Body:\n" + body);
            System.out.println("==========================");
        }
    }
}