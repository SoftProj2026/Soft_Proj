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
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog shown after the user clicks a category on the main dashboard.
 *
 * <p>The dialog allows choosing a booking type (Emergency, New, Review, Individual, Group).
 * Emergency opens a small quick dialog that sends an immediate notification email
 * (no appointment is created). Other types open {@code UnifiedBookingFrame}.</p>
 */
public class BookingTypeChoiceDialog extends JDialog {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;
    private final Category category;

    private static final String COMPANY_EMAIL = "remaajomaa842@gmail.com";
    private static final String COMPANY_EMERGENCY_PHONE = "059-507-9549";
    private static final String DEFAULT_USER_EMAIL = "remaajomaa70@gmail.com";

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

        setLayout(new BorderLayout(10, 10));
        setSize(520, 440); 
        setResizable(false);
        setLocationRelativeTo(parent);

        JLabel header = new JLabel(
                "<html><div style='text-align:center'>Select Booking Type for:<br><b>"
                        + escapeHtml(category.getName())
                        + "</b></div></html>",
                SwingConstants.CENTER
        );
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setBorder(BorderFactory.createEmptyBorder(14, 8, 8, 8));
        add(header, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(0, 1, 10, 10));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        buttonsPanel.setBackground(Color.WHITE);

        JButton emergencyBtn = styledButton("Emergency Booking", new Color(220, 48, 56));
        emergencyBtn.addActionListener(e -> openEmergencyQuickDialog());

        JButton newBookingBtn = styledButton("New Booking", new Color(32, 136, 203));
        newBookingBtn.addActionListener(e -> {
            this.dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setVisible(true);
        });

        JButton reviewBtn = styledButton("Review Booking", new Color(90, 184, 23));
        reviewBtn.addActionListener(e -> {
            this.dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setVisible(true);
        });

        JButton individualBtn = styledButton("Individual Booking", new Color(110, 118, 237));
        individualBtn.addActionListener(e -> {
            this.dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);

            ub.setForceIndividual(true);
            ub.setForcedParticipantCount(1);

            ub.setVisible(true);
        });

        JButton groupBtn = styledButton("Group Booking (1–5 participants)", new Color(244, 166, 39));
        groupBtn.addActionListener(e -> {
            this.dispose();
            UnifiedBookingFrame ub = new UnifiedBookingFrame(auth, booking, repo, category);
            ub.setVisible(true);
        });

        JButton aiBtn = styledButton("AI Booking (Suggest 5 slots + Send Request)", new Color(88, 28, 135));
        aiBtn.addActionListener(e -> openAiSuggestDialog());

        buttonsPanel.add(emergencyBtn);
        buttonsPanel.add(newBookingBtn);
        buttonsPanel.add(reviewBtn);
        buttonsPanel.add(individualBtn);
        buttonsPanel.add(groupBtn);
        buttonsPanel.add(aiBtn);

        add(buttonsPanel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(Color.WHITE);
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());
        bottom.add(close);
        add(bottom, BorderLayout.SOUTH);

        getContentPane().setBackground(Color.WHITE);
    }

    /**
     * NEW: AI dialog flow:
     * 1) Ensure logged in
     * 2) Ask participants + duration
     * 3) Ask AI for top 5 mutual slots
     * 4) Let user pick one
     * 5) Submit booking request for approval (Category Admin -> Big Admin)
     */
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
            if (paren > 0) {
                idx = Integer.parseInt(choice.substring(0, paren).trim()) - 1;
            }
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

        if (res.isSuccess()) {
            dispose();
        }
    }

    /**
     * Prompt integer input in range.
     */
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


    private void disableParticipantSelectorsInContainer(Container c, int participantCount, int min, int max) {
        if (c == null) return;
        for (Component comp : c.getComponents()) {
            if (comp instanceof JSpinner) {
                try {
                    JSpinner spinner = (JSpinner) comp;
                    spinner.setModel(new SpinnerNumberModel(participantCount, min, max, 1));
                    spinner.setValue(participantCount);
                    spinner.setEnabled(false);
                    spinner.setToolTipText("Individual booking — participants fixed to 1");
                } catch (Exception ignored) {
                }
            } else if (comp instanceof JComboBox) {
                try {
                    @SuppressWarnings("rawtypes")
                    JComboBox combo = (JComboBox) comp;
                    boolean integerLike = false;
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        Object item = combo.getItemAt(i);
                        if (item instanceof Integer) { integerLike = true; break; }
                        if (item instanceof String) {
                            String s = ((String) item).trim();
                            if (s.matches("\\d+")) { integerLike = true; break; }
                        }
                    }
                    if (integerLike) {
                        boolean setDone = false;
                        for (int i = 0; i < combo.getItemCount(); i++) {
                            Object item = combo.getItemAt(i);
                            if ((item instanceof Integer && ((Integer) item) == participantCount)
                                    || (item instanceof String && ((String) item).trim().equals(String.valueOf(participantCount)))) {
                                combo.setSelectedIndex(i);
                                setDone = true;
                                break;
                            }
                        }
                        if (!setDone && combo.getItemCount() > 0) {
                            combo.setSelectedIndex(0);
                        }
                        combo.setEnabled(false);
                        combo.setToolTipText("Individual booking — participants fixed to 1");
                    }
                } catch (Exception ignored) {
                }
            }
            if (comp instanceof Container) {
                disableParticipantSelectorsInContainer((Container) comp, participantCount, min, max);
            }
        }
    }

    private void openEmergencyQuickDialog() {
        JDialog dlg = new JDialog(this, "Emergency - Preferred Time", true);
        dlg.setLayout(new BorderLayout(8, 8));
        dlg.setSize(420, 300);
        dlg.setLocationRelativeTo(this);

        JLabel lbl = new JLabel("<html><b>Select preferred emergency time (optional)</b></html>");
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 0, 8));
        dlg.add(lbl, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JComboBox<String> cb = new JComboBox<>();
        cb.addItem("No preferred time");
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
        for (TimeSlot t : choices) {
            cb.addItem(t.getStartDateTime().format(fmt));
        }

        center.add(cb, BorderLayout.NORTH);

        JTextArea note = new JTextArea(5, 30);
        note.setLineWrap(true);
        note.setWrapStyleWord(true);
        note.setBorder(BorderFactory.createTitledBorder("Additional note (optional)"));
        center.add(new JScrollPane(note), BorderLayout.CENTER);

        dlg.add(center, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton send = new JButton("Send Emergency Email");
        JButton cancel = new JButton("Cancel");
        actions.add(cancel);
        actions.add(send);
        dlg.add(actions, BorderLayout.SOUTH);

        cancel.addActionListener(e -> dlg.dispose());

        send.addActionListener(e -> {
            String userEmail = DEFAULT_USER_EMAIL;
            try {
                if (auth != null && auth.getCurrentUser() != null) {
                    String uEmail = auth.getCurrentUser().getEmail();
                    if (uEmail != null && !uEmail.trim().isEmpty()) {
                        userEmail = uEmail;
                    }
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

    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return b;
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