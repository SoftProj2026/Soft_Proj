package presentation;

import Service.AuthService;
import Service.BookingService;
import Service.EmailSender;
import Service.SmtpEmailSender;
import domain.AppointmentType;
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
 *
 * @author remaa
 * @version 1.0
 */
public class BookingTypeChoiceDialog extends JDialog {

    /**
     * Authentication service used to obtain current user info.
     */
    private final AuthService auth;

    /**
     * Booking service used for booking related operations.
     */
    private final BookingService booking;

    /**
     * Repository providing application data (slots, categories, appointments).
     */
    private final DataRepository repo;

    /**
     * Category for which booking types are selected.
     */
    private final Category category;

    /**
     * Company email address used as destination for emergency notifications.
     */
    private static final String COMPANY_EMAIL = "remaajomaa842@gmail.com";

    /**
     * Company emergency phone number printed in the emergency email body.
     */
    private static final String COMPANY_EMERGENCY_PHONE = "059-507-9549";

    /**
     * Fallback user email used when authenticated user email is not available.
     */
    private static final String DEFAULT_USER_EMAIL = "remaajomaa70@gmail.com";

    /**
     * Create the booking type choice dialog.
     *
     * @param parent   parent frame for modality and positioning
     * @param category category to create bookings for
     * @param repo     repository instance providing data
     * @param auth     authentication service instance
     * @param booking  booking service instance
     */
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
        setSize(460, 360);
        setResizable(false);
        setLocationRelativeTo(parent);

        JLabel header = new JLabel("<html><div style='text-align:center'>Select Booking Type for:<br><b>" + escapeHtml(category.getName()) + "</b></div></html>", SwingConstants.CENTER);
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

        buttonsPanel.add(emergencyBtn);
        buttonsPanel.add(newBookingBtn);
        buttonsPanel.add(reviewBtn);
        buttonsPanel.add(individualBtn);
        buttonsPanel.add(groupBtn);

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
     * Recursively search the container for participant selectors (JSpinner or JComboBox),
     * set their value to {@code participantCount} and disable them.
     *
     * @param c                root container to search
     * @param participantCount value to set (1 for individual)
     * @param min              minimum allowed value (1)
     * @param max              maximum allowed value (5)
     */
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

    /**
     * Open a small modal dialog to select a preferred emergency time (optional) and send an emergency email.
     *
     * <p>No appointment is created by this flow; it only sends notifications to the user and company.</p>
     */
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

    /**
     * Create an EmailSender. Attempts to construct {@link SmtpEmailSender} and falls back to
     * an internal console printing implementation if that fails.
     *
     * @return an {@link EmailSender} instance
     */
    private EmailSender createEmailSender() {
        try {
            return new SmtpEmailSender();
        } catch (Throwable t) {
            return new ConsoleEmailSender();
        }
    }

    /**
     * Create a styled JButton used in this dialog.
     *
     * @param text button label text
     * @param bg   background color for the button
     * @return constructed JButton
     */
    private JButton styledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        return b;
    }

    /**
     * Escape HTML sensitive characters to avoid injection in the header label.
     *
     * @param s input string
     * @return escaped string suitable for use inside HTML label
     */
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /**
     * Fallback email sender that prints email fields to the console.
     *
     * @author remaa
     * @version 1.0
     */
    private static class ConsoleEmailSender implements EmailSender {
        /**
         * Print email details to System.out for testing purposes.
         *
         * @param fromIgnored logical sender address (ignored for printing)
         * @param to          recipient email address
         * @param subject     email subject line
         * @param body        email body text
         */
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