package presentation;

import Service.AuthService;
import Service.BookingResult;
import Service.BookingService;
import Service.BlockedSlotsRule;
import domain.Appointment;
import domain.Category;
import domain.TimeSlot;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Main booking dashboard UI.
 * <p>
 * Displays categories and available time slots, and allows the user to book.
 * Also provides access to "My Bookings" and Logout.
 * </p>
 */
public class MainDashboardFrame extends JFrame {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;

    private JPanel slotPanel;
    private ButtonGroup slotGroup = new ButtonGroup();
    private TimeSlot selectedSlot;
    private Category selectedCategory;

    private final BlockedSlotsRule blockedRule = new BlockedSlotsRule();

    private static final Color BG = new Color(245, 248, 255);
    private static final Color BLUE = new Color(33, 120, 255);
    private static final Color BLUE_DARK = new Color(18, 78, 180);

    private final DateTimeFormatter slotFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates the main dashboard frame.
     *
     * @param auth  authentication service
     * @param booking booking service
     * @param repo  data repository
     */
    public MainDashboardFrame(AuthService auth,
                              BookingService booking,
                              DataRepository repo) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;

        setTitle("Booking Dashboard");
        setSize(1100, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(12, 12));

        getContentPane().setBackground(BG);

        JPanel categoryPanel = new JPanel(new GridLayout(0, 2, 16, 16));
        categoryPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        categoryPanel.setBackground(BG);

        List<Category> cats = repo.getCategories();
        for (Category c : cats) {
            JButton btn = createCategoryButton(c.getName());
            btn.addActionListener(e -> {
                selectedCategory = c;
                loadSlots(c);
            });
            categoryPanel.add(btn);
        }

        JScrollPane catScroll = new JScrollPane(categoryPanel);
        catScroll.setBorder(null);
        catScroll.getViewport().setBackground(BG);
        add(catScroll, BorderLayout.CENTER);

        slotPanel = new JPanel();
        slotPanel.setLayout(new BoxLayout(slotPanel, BoxLayout.Y_AXIS));
        slotPanel.setBackground(Color.WHITE);

        JScrollPane slotScroll = new JScrollPane(slotPanel);
        slotScroll.setPreferredSize(new Dimension(350, 0));
        slotScroll.setBorder(BorderFactory.createTitledBorder("Available Slots"));
        add(slotScroll, BorderLayout.EAST);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(BG);

        JButton myBookingsBtn = new JButton("My Bookings");
        myBookingsBtn.addActionListener(e -> new MyBookingsFrame(auth, repo).setVisible(true));

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.addActionListener(e -> {
            auth.logout();
            new LoginFrame(auth, booking, repo).setVisible(true);
            dispose();
        });

        JButton bookBtn = new JButton("Book Selected Slot");
        bookBtn.setBackground(BLUE_DARK);
        bookBtn.setForeground(Color.WHITE);
        bookBtn.setFocusPainted(false);
        bookBtn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        bookBtn.addActionListener(e -> bookSelectedSlot());

        bottom.add(myBookingsBtn);
        bottom.add(logoutBtn);
        bottom.add(bookBtn);

        add(bottom, BorderLayout.SOUTH);

        if (!cats.isEmpty()) {
            selectedCategory = cats.get(0);
            loadSlots(selectedCategory);
        }
    }

    /**
     * Creates a styled button for a category item.
     *
     * @param text category name
     * @return styled JButton
     */
    private JButton createCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(BLUE);
        btn.setForeground(Color.WHITE);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(18, 16, 18, 16));
        return btn;
    }

    /**
     * Loads available (unbooked) slots for the given category into the right panel.
     * <p>
     * Blocked slots (break time) are displayed but disabled, and clicking shows a warning.
     * </p>
     *
     * @param category selected category
     */
    private void loadSlots(Category category) {
        slotPanel.removeAll();
        slotGroup = new ButtonGroup();
        selectedSlot = null;

        for (TimeSlot slot : repo.getSlots()) {
            if (slot.getCategory() == null) continue;

            if (slot.getCategory().getName().equalsIgnoreCase(category.getName())
                    && slot.isAvailable()) {

                String baseLabel = slot.getStartDateTime().format(slotFmt);

                String blockMsg = blockedRule.getBlockMessageIfBlocked(slot);
                boolean blocked = (blockMsg != null);

                String label = blocked
                        ? baseLabel + "  (Blocked - Break)"
                        : baseLabel;

                JRadioButton radio = new JRadioButton(label);
                radio.setBackground(Color.WHITE);
                radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                if (blocked) {
                    radio.setEnabled(false);

                    radio.addMouseListener(new java.awt.event.MouseAdapter() {
                        @Override
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            DialogUtil.show(
                                    MainDashboardFrame.this,
                                    "Blocked Slot",
                                    blockMsg,
                                    DialogUtil.Type.WARNING
                            );
                            slotGroup.clearSelection();
                            selectedSlot = null;
                        }
                    });
                } else {
                    radio.addActionListener(e -> selectedSlot = slot);
                }

                slotGroup.add(radio);
                slotPanel.add(radio);
            }
        }

        if (slotPanel.getComponentCount() == 0) {
            JLabel none = new JLabel("No available slots for this category.");
            none.setBorder(new EmptyBorder(10, 10, 10, 10));
            slotPanel.add(none);
        }

        slotPanel.revalidate();
        slotPanel.repaint();
    }

    /**
     * Prompts user for an integer input within a specified range.
     *
     * @param title   dialog title
     * @param message prompt message
     * @param min     minimum allowed value
     * @param max     maximum allowed value
     * @return the integer value, or null if the user cancels
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
     * Books the currently selected slot after collecting participants and duration,
     * then displays a result dialog.
     */
    private void bookSelectedSlot() {
        if (!auth.isLoggedIn()) {
            DialogUtil.show(this, "Not Logged In", "You must login first!", DialogUtil.Type.ERROR);
            return;
        }

        if (selectedSlot == null) {
            DialogUtil.show(this, "No Slot Selected", "Please select a slot first!", DialogUtil.Type.WARNING);
            return;
        }

        Integer participants = promptIntInRange(
                "Participants",
                "Enter participants (1 - 5):",
                1, 5
        );
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
            if (selectedCategory != null) {
                loadSlots(selectedCategory);
            }
        }
    }
}