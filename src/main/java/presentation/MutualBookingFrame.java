package presentation;

import Service.AuthService;
import Service.BookingResult;
import Service.BookingService;
import Service.BlockedSlotsRule;
import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Booking window that displays only mutual slots for a selected {@link Category}.
 *
 * <p>A mutual slot is considered bookable when:</p>
 * <ul>
 *   <li>The slot belongs to the selected category</li>
 *   <li>The company slot is available</li>
 *   <li>The current user is not busy (no overlap with confirmed appointments)</li>
 *   <li>The slot is not blocked by {@link BlockedSlotsRule}</li>
 *   <li>The slot start time is not in the past</li>
 * </ul>
 *
 * <p>This screen enforces the MAIN + EMERGENCY workflow:</p>
 * <ul>
 *   <li>First confirmed booking in a category is treated as MAIN.</li>
 *   <li>Second confirmed booking in the same category must be EMERGENCY.</li>
 *   <li>If a user has exactly one confirmed booking, closing the window is blocked until EMERGENCY is booked.</li>
 * </ul>
 */
public class MutualBookingFrame extends JFrame {

    private static final Color BG = UITheme.BG;

    private static final Color ROW_OK_BG = new Color(220, 252, 231);
    private static final Color ROW_OK_FG = new Color(20, 83, 45);

    private static final Color ROW_PAST_BG = new Color(240, 240, 240);
    private static final Color ROW_PAST_FG = new Color(120, 120, 120);

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;
    private final Category category;

    private final JFrame companyFrame;
    private final JFrame myFreeFrame;

    private final JPanel listPanel = new JPanel();
    private final ButtonGroup group = new ButtonGroup();
    private TimeSlot selectedSlot;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final BlockedSlotsRule blockedRule = new BlockedSlotsRule();

    /**
     * Creates a mutual booking window.
     *
     * @param auth         authentication service
     * @param booking      booking service
     * @param repo         repository
     * @param category     selected category
     * @param companyFrame company window (if any) to close together
     * @param myFreeFrame  free-slots window (if any) to close together
     */
    public MutualBookingFrame(AuthService auth,
                              BookingService booking,
                              DataRepository repo,
                              Category category,
                              JFrame companyFrame,
                              JFrame myFreeFrame) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;
        this.category = category;
        this.companyFrame = companyFrame;
        this.myFreeFrame = myFreeFrame;

        setTitle("Mutual Booking - " + category.getName());
        setSize(520, 640);
        setLocationByPlatform(true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptClose();
            }
        });

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Choose a mutual slot to book");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("First: MAIN booking. Second: EMERGENCY booking (only if you booked MAIN).");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        JButton bookBtn = UITheme.primaryButton("Book Selected Slot");
        bookBtn.addActionListener(e -> bookSelected());

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> attemptClose());

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        south.setBackground(BG);
        south.add(closeBtn);
        south.add(bookBtn);
        add(south, BorderLayout.SOUTH);

        load();
    }

    /**
     * Attempts to close the window, blocking the close action if the user must still complete EMERGENCY booking.
     */
    private void attemptClose() {
        long confirmed = countConfirmedForThisCategory();

        if (confirmed == 1) {
            DialogUtil.show(
                    this,
                    "Emergency Required",
                    "You already booked the MAIN appointment for \"" + category.getName() + "\".\n" +
                            "Now you must book an EMERGENCY appointment before closing.",
                    DialogUtil.Type.WARNING
            );
            return;
        }

        closeAll();
    }

    /**
     * Disposes this window and any related windows passed to the constructor.
     */
    private void closeAll() {
        if (companyFrame != null && companyFrame.isDisplayable()) companyFrame.dispose();
        if (myFreeFrame != null && myFreeFrame.isDisplayable()) myFreeFrame.dispose();
        if (this.isDisplayable()) this.dispose();
    }

    /**
     * Counts confirmed appointments for the current user within the selected category.
     *
     * @return count of confirmed appointments
     */
    private long countConfirmedForThisCategory() {
        if (!auth.isLoggedIn()) return 0;
        String user = auth.getCurrentUser().getUsername();

        return repo.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .filter(a -> a.getUser().getUsername().equalsIgnoreCase(user))
                .filter(a -> a.getSlot() != null && a.getSlot().getCategory() != null)
                .filter(a -> a.getSlot().getCategory().getName().equalsIgnoreCase(category.getName()))
                .count();
    }

    /**
     * Checks whether the current user is busy during the given slot based on overlap with confirmed appointments.
     *
     * @param slot target slot
     * @return {@code true} if the user is busy during this slot
     */
    private boolean isUserBusy(TimeSlot slot) {
        if (slot == null) return false;
        if (!auth.isLoggedIn()) return false;

        String username = auth.getCurrentUser().getUsername();

        for (Appointment a : repo.getAppointments()) {
            if (a.getStatus() != AppointmentStatus.CONFIRMED) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            TimeSlot existing = a.getSlot();

            boolean overlap =
                    slot.getStartDateTime().isBefore(existing.getEndDateTime()) &&
                            slot.getEndDateTime().isAfter(existing.getStartDateTime());

            if (overlap) return true;
        }
        return false;
    }

    /**
     * Loads and renders the list of mutual slots.
     *
     * <p>Past slots are displayed but not selectable.</p>
     */
    private void load() {
        listPanel.removeAll();
        selectedSlot = null;
        group.clearSelection();

        if (!auth.isLoggedIn()) {
            JLabel msg = new JLabel("You must login first to book.");
            msg.setBorder(new EmptyBorder(10, 10, 10, 10));
            listPanel.add(msg);
            listPanel.revalidate();
            listPanel.repaint();
            return;
        }

        long confirmed = countConfirmedForThisCategory();

        JLabel hint;
        if (confirmed == 0) {
            hint = new JLabel("Next booking will be your MAIN appointment for this category.");
        } else if (confirmed == 1) {
            hint = new JLabel("Next booking MUST be your EMERGENCY appointment (required).");
        } else {
            hint = new JLabel("MAIN + EMERGENCY completed. You can close now.");
        }
        hint.setBorder(new EmptyBorder(10, 10, 10, 10));
        hint.setForeground(new Color(60, 70, 85));
        listPanel.add(hint);
        listPanel.add(Box.createVerticalStrut(8));

        if (confirmed >= 2) {
            JLabel done = new JLabel("No more bookings allowed for this category.");
            done.setBorder(new EmptyBorder(10, 10, 10, 10));
            listPanel.add(done);
            listPanel.revalidate();
            listPanel.repaint();
            return;
        }

        int count = 0;
        LocalDateTime now = LocalDateTime.now();

        for (TimeSlot slot : repo.getSlots()) {
            if (slot.getCategory() == null) continue;
            if (!slot.getCategory().getName().equalsIgnoreCase(category.getName())) continue;

            if (!slot.isAvailable()) continue;
            if (isUserBusy(slot)) continue;
            if (blockedRule.getBlockMessageIfBlocked(slot) != null) continue;

            if (slot.getStartDateTime() != null && slot.getStartDateTime().isBefore(now)) {
                JLabel past = new JLabel(slot.getStartDateTime().format(fmt) + "  (Past - Not bookable)");
                past.setOpaque(true);
                past.setBackground(ROW_PAST_BG);
                past.setForeground(ROW_PAST_FG);
                past.setBorder(new EmptyBorder(10, 12, 10, 12));
                past.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                listPanel.add(past);
                listPanel.add(Box.createVerticalStrut(8));
                continue;
            }

            String label = slot.getStartDateTime().format(fmt);

            JRadioButton radio = new JRadioButton(label);
            radio.setOpaque(true);
            radio.setBackground(ROW_OK_BG);
            radio.setForeground(ROW_OK_FG);
            radio.setBorder(new EmptyBorder(10, 12, 10, 12));
            radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            radio.addActionListener(e -> selectedSlot = slot);

            group.add(radio);
            listPanel.add(radio);
            listPanel.add(Box.createVerticalStrut(8));
            count++;
        }

        if (count == 0) {
            JLabel none = new JLabel("No mutual slots available to book for this category.");
            none.setBorder(new EmptyBorder(10, 10, 10, 10));
            listPanel.add(none);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    /**
     * Prompts the user for an integer value within a specified range.
     *
     * @param title   dialog title
     * @param message dialog message
     * @param min     minimum accepted value
     * @param max     maximum accepted value
     * @return selected value, or {@code null} if cancelled
     */
    private Integer promptIntInRange(String title, String message, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this, message, title, JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) return null;

            input = input.trim();
            if (input.isEmpty()) {
                DialogUtil.show(this, "Invalid Input", "Value cannot be empty. Please try again.", DialogUtil.Type.ERROR);
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
                DialogUtil.show(this, "Out of Range",
                        "Value must be between " + min + " and " + max + ". Please try again.",
                        DialogUtil.Type.ERROR);
                continue;
            }

            return value;
        }
    }

    /**
     * Creates a booking for the selected mutual slot.
     */
    private void bookSelected() {
        if (selectedSlot == null) {
            DialogUtil.show(this, "No Slot Selected", "Please select a slot first!", DialogUtil.Type.WARNING);
            return;
        }

        if (selectedSlot.getStartDateTime() != null && selectedSlot.getStartDateTime().isBefore(LocalDateTime.now())) {
            DialogUtil.show(this, "Not Allowed", "You cannot book a past time slot.", DialogUtil.Type.ERROR);
            selectedSlot = null;
            group.clearSelection();
            load();
            return;
        }

        long confirmed = countConfirmedForThisCategory();
        if (confirmed >= 2) {
            DialogUtil.show(
                    this,
                    "Booking Not Allowed",
                    "You already have MAIN + EMERGENCY for \"" + category.getName() + "\".",
                    DialogUtil.Type.ERROR
            );
            return;
        }

        boolean isEmergency = (confirmed == 1);
        if (isEmergency) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "This booking will be saved as your EMERGENCY appointment for:\n\"" + category.getName() + "\".\n\nContinue?",
                    "Emergency Appointment",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm != JOptionPane.YES_OPTION) return;
        }

        Integer participants = promptIntInRange("Participants", "Enter participants (1 - 5):", 1, 5);
        if (participants == null) return;

        int slotMinutes = (int) Duration.between(
                selectedSlot.getStartDateTime(),
                selectedSlot.getEndDateTime()
        ).toMinutes();

        Integer duration = promptIntInRange(
                "Duration",
                "Enter duration in minutes (1 - " + slotMinutes + "):",
                1, slotMinutes
        );
        if (duration == null) return;

        Appointment appointment = new Appointment(
                auth.getCurrentUser(),
                selectedSlot,
                duration,
                participants
        );

        BookingResult result = booking.book(appointment);

        DialogUtil.show(
                this,
                "Booking Result",
                result.getMessage(),
                result.isSuccess() ? DialogUtil.Type.SUCCESS : DialogUtil.Type.ERROR
        );

        if (result.isSuccess()) {
            if (!isEmergency) {
                DialogUtil.show(
                        this,
                        "Emergency Required",
                        "MAIN appointment booked for \"" + category.getName() + "\".\n" +
                                "Now please book an EMERGENCY appointment.\n" +
                                "You will not be able to close until you do.",
                        DialogUtil.Type.INFO
                );
            }
            load();
        }
    }
}