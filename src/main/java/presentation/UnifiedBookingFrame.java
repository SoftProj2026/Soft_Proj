package presentation;

import Service.AuthService;
import Service.BookingRequestService;
import Service.BookingResult;
import Service.BookingService;
import Service.BlockedSlotsRule;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Unified booking window that displays three side-by-side views for a selected {@link Category}:
 * <ol>
 *   <li><b>Company Available</b>: company-available hours for the selected day</li>
 *   <li><b>My Free Slots</b>: working hours showing whether the current user is free or busy</li>
 *   <li><b>Mutual Slots (Send Request)</b>: bookable hours where company is available and user is free</li>
 * </ol>
 *
 * <p>
 * Booking behavior:
 * </p>
 * <ul>
 *   <li>The user does <b>not</b> confirm an appointment directly.</li>
 *   <li>Clicking "Book Selected Slot" submits a booking request via {@link BookingRequestService}.</li>
 *   <li>The selected slot is held while the request is pending approvals.</li>
 * </ul>
 *
 * <p>
 * Working hours are 09:00 to 16:00 inclusive, with a break at 12:00 that is not bookable.
 * </p>
 */
public class UnifiedBookingFrame extends JFrame {

    /** Base background color used for the frame. */
    private static final Color BG = UITheme.BG;

    /** Day selector button background color. */
    private static final Color DAY_BTN_BG = Color.WHITE;

    /** Day selector button foreground color. */
    private static final Color DAY_BTN_FG = new Color(25, 35, 45);

    /** Selected day button background color. */
    private static final Color DAY_BTN_SELECTED_BG = UITheme.PRIMARY_DARK;

    /** Selected day button foreground color. */
    private static final Color DAY_BTN_SELECTED_FG = Color.WHITE;

    /** Row background color for available/OK rows. */
    private static final Color ROW_OK_BG = new Color(220, 252, 231);

    /** Row foreground color for available/OK rows. */
    private static final Color ROW_OK_FG = new Color(20, 83, 45);

    /** Row background color for free-time rows. */
    private static final Color ROW_FREE_BG = new Color(219, 234, 254);

    /** Row foreground color for free-time rows. */
    private static final Color ROW_FREE_FG = new Color(30, 64, 175);

    /** Row background color for break rows. */
    private static final Color ROW_BREAK_BG = new Color(254, 249, 195);

    /** Row foreground color for break rows. */
    private static final Color ROW_BREAK_FG = new Color(113, 63, 18);

    /** Row background color for busy/unavailable rows. */
    private static final Color ROW_BAD_BG = new Color(254, 242, 242);

    /** Row foreground color for busy/unavailable rows. */
    private static final Color ROW_BAD_FG = new Color(180, 30, 30);

    /** Working day start hour. */
    private static final LocalTime START_HOUR = LocalTime.of(9, 0);

    /** Working day end hour (inclusive). */
    private static final LocalTime END_HOUR = LocalTime.of(16, 0);

    /** Break start hour (12:00 to 13:00 is break). */
    private static final LocalTime BREAK_HOUR = LocalTime.of(12, 0);

    private final AuthService auth;

    private final BookingService booking;

    private final DataRepository repo;
    private final Category category;

    private final BookingRequestService requestService;

    private final BlockedSlotsRule blockedRule = new BlockedSlotsRule();

    private final List<LocalDate> weekDates = new ArrayList<>();

    private LocalDate selectedCompanyDate;
    private LocalDate selectedMyDate;
    private LocalDate selectedMutualDate;

    private JPanel companyDayBarWrap;
    private JPanel myDayBarWrap;
    private JPanel mutualDayBarWrap;

    private final JPanel companyListPanel = new JPanel();
    private final JPanel myListPanel = new JPanel();
    private final JPanel mutualListPanel = new JPanel();

    private final ButtonGroup mutualGroup = new ButtonGroup();
    private TimeSlot selectedMutualSlot;

    private final DateTimeFormatter dayNameFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Creates the unified booking window for a given category.
     *
     * @param auth     authentication service
     * @param booking  booking service (not used to confirm directly in this window)
     * @param repo     data repository
     * @param category selected category
     */
    public UnifiedBookingFrame(AuthService auth, BookingService booking, DataRepository repo, Category category) {
        this.auth = auth;
        this.booking = booking;
        this.repo = repo;
        this.category = category;
        this.requestService = new BookingRequestService(repo);

        setTitle("Unified Booking - " + (category != null ? category.getName() : ""));
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                attemptClose();
            }
        });

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(12, 12));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) weekDates.add(today.plusDays(i));

        selectedCompanyDate = weekDates.get(0);
        selectedMyDate = weekDates.get(0);
        selectedMutualDate = weekDates.get(0);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildColumns(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        reloadAll();
    }

    /**
     * Builds the top header panel containing the title and instructions.
     *
     * @return header panel
     */
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Booking Overview - " + category.getName());
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Click a day to show hours. Hours: 09:00 - 16:00. Break at 12:00. Booking sends request for approval.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        return header;
    }

    /**
     * Builds the main 3-column layout.
     *
     * @return columns container panel
     */
    private JPanel buildColumns() {
        JPanel cols = new JPanel(new GridLayout(1, 3, 12, 12));
        cols.setBorder(new EmptyBorder(0, 14, 0, 14));
        cols.setBackground(BG);

        cols.add(buildCompanyCard());
        cols.add(buildMyCard());
        cols.add(buildMutualCard());

        return cols;
    }

    /**
     * Builds the "Company Available" card.
     *
     * @return company card panel
     */
    private JPanel buildCompanyCard() {
        JPanel card = createCardShell();

        JPanel header = buildCardHeader("Company Available");
        companyDayBarWrap = new JPanel(new BorderLayout());
        companyDayBarWrap.setOpaque(false);
        header.add(companyDayBarWrap);

        companyListPanel.setLayout(new BoxLayout(companyListPanel, BoxLayout.Y_AXIS));
        companyListPanel.setBackground(Color.WHITE);

        card.add(header, BorderLayout.NORTH);
        card.add(wrapScroll(companyListPanel), BorderLayout.CENTER);
        return card;
    }

    /**
     * Builds the "My Free Slots" card.
     *
     * @return my-free card panel
     */
    private JPanel buildMyCard() {
        JPanel card = createCardShell();

        JPanel header = buildCardHeader("My Free Slots");
        myDayBarWrap = new JPanel(new BorderLayout());
        myDayBarWrap.setOpaque(false);
        header.add(myDayBarWrap);

        myListPanel.setLayout(new BoxLayout(myListPanel, BoxLayout.Y_AXIS));
        myListPanel.setBackground(Color.WHITE);

        card.add(header, BorderLayout.NORTH);
        card.add(wrapScroll(myListPanel), BorderLayout.CENTER);
        return card;
    }

    /**
     * Builds the "Mutual Slots (Send Request)" card.
     *
     * @return mutual card panel
     */
    private JPanel buildMutualCard() {
        JPanel card = createCardShell();

        JPanel header = buildCardHeader("Mutual Slots (Send Request)");
        mutualDayBarWrap = new JPanel(new BorderLayout());
        mutualDayBarWrap.setOpaque(false);
        header.add(mutualDayBarWrap);

        JLabel hint = new JLabel("Pick a time then click Book. This will SEND a request (slot will be held).");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hint.setForeground(new Color(90, 100, 115));
        hint.setBorder(new EmptyBorder(6, 2, 0, 2));
        header.add(hint);

        mutualListPanel.setLayout(new BoxLayout(mutualListPanel, BoxLayout.Y_AXIS));
        mutualListPanel.setBackground(Color.WHITE);

        card.add(header, BorderLayout.NORTH);
        card.add(wrapScroll(mutualListPanel), BorderLayout.CENTER);
        return card;
    }

    /**
     * Creates a card container with border and padding.
     *
     * @return card panel
     */
    private JPanel createCardShell() {
        JPanel outer = new JPanel(new BorderLayout(10, 10));
        outer.setBackground(Color.WHITE);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 245), 2, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return outer;
    }

    /**
     * Builds a card header block.
     *
     * @param titleText title label text
     * @return header panel
     */
    private JPanel buildCardHeader(String titleText) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(titleText);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setBorder(new EmptyBorder(0, 2, 8, 2));
        header.add(t);

        return header;
    }

    /**
     * Wraps a list panel with a white-background scroll pane.
     *
     * @param list list panel
     * @return scroll pane
     */
    private JScrollPane wrapScroll(JPanel list) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /**
     * Builds the bottom action bar.
     *
     * @return bottom panel
     */
    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(BG);

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> attemptClose());

        JButton bookBtn = UITheme.primaryButton("Book Selected Slot");
        bookBtn.addActionListener(e -> bookSelectedMutual());

        bottom.add(closeBtn);
        bottom.add(bookBtn);
        return bottom;
    }

    /**
     * Formats a day selector button label in English.
     *
     * @param d date to format
     * @return formatted label
     */
    private String formatDayButton(LocalDate d) {
        return d.format(dayNameFmt) + " " + d.getDayOfMonth() + "/" + d.getMonthValue();
    }

    /**
     * Renders the day selector bars for all three columns.
     */
    private void renderDayBars() {
        companyDayBarWrap.removeAll();
        myDayBarWrap.removeAll();
        mutualDayBarWrap.removeAll();

        companyDayBarWrap.add(buildDayBarFor(selectedCompanyDate, d -> {
            selectedCompanyDate = d;
            renderDayBars();
            reloadCompany();
        }), BorderLayout.CENTER);

        myDayBarWrap.add(buildDayBarFor(selectedMyDate, d -> {
            selectedMyDate = d;
            renderDayBars();
            reloadMy();
        }), BorderLayout.CENTER);

        mutualDayBarWrap.add(buildDayBarFor(selectedMutualDate, d -> {
            selectedMutualDate = d;
            renderDayBars();
            reloadMutual();
        }), BorderLayout.CENTER);

        companyDayBarWrap.revalidate();
        myDayBarWrap.revalidate();
        mutualDayBarWrap.revalidate();
    }

    /**
     * Builds a day selector bar for a column.
     *
     * @param selected currently selected date
     * @param onSelect selection handler
     * @return day bar panel
     */
    private JPanel buildDayBarFor(LocalDate selected, java.util.function.Consumer<LocalDate> onSelect) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.setOpaque(false);

        for (LocalDate d : weekDates) {
            JButton b = new JButton(formatDayButton(d));
            styleDayButton(b, d.equals(selected));
            b.addActionListener(e -> onSelect.accept(d));
            bar.add(b);
        }
        return bar;
    }

    /**
     * Styles a day button consistently.
     *
     * @param b        day button
     * @param selected selected flag
     */
    private void styleDayButton(JButton b, boolean selected) {
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 235), 1, true),
                BorderFactory.createEmptyBorder(7, 10, 7, 10)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        if (selected) {
            b.setBackground(DAY_BTN_SELECTED_BG);
            b.setForeground(DAY_BTN_SELECTED_FG);
        } else {
            b.setBackground(DAY_BTN_BG);
            b.setForeground(DAY_BTN_FG);
        }
    }

    /**
     * Reloads day bars and all columns.
     */
    private void reloadAll() {
        renderDayBars();
        reloadCompany();
        reloadMy();
        reloadMutual();
    }

    /**
     * Attempts to close the window.
     * <p>
     * If the current user has exactly one confirmed booking in the category,
     * closing is blocked to preserve the MAIN/EMERGENCY business rule.
     * </p>
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
        dispose();
    }

    /**
     * Counts confirmed bookings for the current user in the selected category.
     *
     * @return number of confirmed bookings
     */
    private long countConfirmedForThisCategory() {
        if (!auth.isLoggedIn()) return 0;
        String user = auth.getCurrentUser().getUsername();

        return repo.getAppointments().stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .filter(a -> a.getUser() != null && a.getUser().getUsername().equalsIgnoreCase(user))
                .filter(a -> a.getSlot() != null && a.getSlot().getCategory() != null)
                .filter(a -> a.getSlot().getCategory().getName().equalsIgnoreCase(category.getName()))
                .count();
    }

    /**
     * Checks whether the current user has any confirmed appointment overlapping a slot.
     *
     * @param slot slot to test
     * @return true if the user is busy; false otherwise
     */
    private boolean isUserBusy(TimeSlot slot) {
        if (slot == null) return false;
        if (!auth.isLoggedIn()) return false;

        String username = auth.getCurrentUser().getUsername();

        for (domain.Appointment a : repo.getAppointments()) {
            if (a.getStatus() != AppointmentStatus.CONFIRMED) continue;
            if (a.getUser() == null) continue;
            if (!a.getUser().getUsername().equalsIgnoreCase(username)) continue;

            TimeSlot existing = a.getSlot();
            if (existing == null) continue;

            boolean overlap =
                    slot.getStartDateTime().isBefore(existing.getEndDateTime()) &&
                            slot.getEndDateTime().isAfter(existing.getStartDateTime());

            if (overlap) return true;
        }
        return false;
    }

    /**
     * Finds a repository slot for this category at the given day and hour.
     *
     * @param date target date
     * @param hour target hour
     * @return matching slot or null if not found
     */
    private TimeSlot findRepoSlot(LocalDate date, LocalTime hour) {
        for (TimeSlot slot : repo.getSlots()) {
            if (slot == null || slot.getStartDateTime() == null) continue;
            if (slot.getCategory() == null) continue;
            if (!slot.getCategory().getName().equalsIgnoreCase(category.getName())) continue;

            if (!slot.getStartDateTime().toLocalDate().equals(date)) continue;
            if (!slot.getStartDateTime().toLocalTime().equals(hour)) continue;

            return slot;
        }
        return null;
    }

    /**
     * Adds a styled break row to a list panel.
     *
     * @param panel list panel
     * @param label row label
     */
    private void addBreakRow(JPanel panel, String label) {
        JLabel row = styledLabel(label, ROW_BREAK_BG, ROW_BREAK_FG);
        row.setToolTipText("Break time (12:00 - 13:00).");
        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }

    /**
     * Reloads the "Company Available" column.
     */
    private void reloadCompany() {
        companyListPanel.removeAll();

        if (!auth.isLoggedIn()) {
            companyListPanel.add(simpleMsg("Login required."));
            refreshPanel(companyListPanel);
            return;
        }

        int count = 0;

        LocalTime t = START_HOUR;
        while (!t.isAfter(END_HOUR)) {
            if (t.equals(BREAK_HOUR)) {
                addBreakRow(companyListPanel, "12:00  (Break)");
                t = t.plusHours(1);
                continue;
            }

            TimeSlot slot = findRepoSlot(selectedCompanyDate, t);

            if (slot != null && slot.isAvailable()) {
                JLabel row = styledLabel(t.format(timeFmt) + "  (Available)", ROW_OK_BG, ROW_OK_FG);
                companyListPanel.add(row);
                companyListPanel.add(Box.createVerticalStrut(8));
                count++;
            }

            t = t.plusHours(1);
        }

        if (count == 0) {
            companyListPanel.add(simpleMsg("No available company slots for this day."));
        }

        refreshPanel(companyListPanel);
    }

    /**
     * Reloads the "My Free Slots" column.
     */
    private void reloadMy() {
        myListPanel.removeAll();

        if (!auth.isLoggedIn()) {
            myListPanel.add(simpleMsg("Login required."));
            refreshPanel(myListPanel);
            return;
        }

        LocalTime t = START_HOUR;
        while (!t.isAfter(END_HOUR)) {
            if (t.equals(BREAK_HOUR)) {
                addBreakRow(myListPanel, "12:00  (Break)");
                t = t.plusHours(1);
                continue;
            }

            TimeSlot slot = findRepoSlot(selectedMyDate, t);

            JLabel row;
            if (slot == null) {
                row = styledLabel(t.format(timeFmt) + "  (No slot)", ROW_BAD_BG, ROW_BAD_FG);
            } else {
                boolean busy = isUserBusy(slot);
                row = busy
                        ? styledLabel(t.format(timeFmt) + "  (Busy)", ROW_BAD_BG, ROW_BAD_FG)
                        : styledLabel(t.format(timeFmt) + "  (Free)", ROW_FREE_BG, ROW_FREE_FG);
            }

            myListPanel.add(row);
            myListPanel.add(Box.createVerticalStrut(8));
            t = t.plusHours(1);
        }

        refreshPanel(myListPanel);
    }

    /**
     * Reloads the "Mutual Slots (Send Request)" column.
     */
    private void reloadMutual() {
        mutualListPanel.removeAll();
        selectedMutualSlot = null;
        mutualGroup.clearSelection();

        if (!auth.isLoggedIn()) {
            mutualListPanel.add(simpleMsg("Login required to book."));
            refreshPanel(mutualListPanel);
            return;
        }

        long confirmed = countConfirmedForThisCategory();

        JLabel hint;
        if (confirmed == 0) hint = simpleMsg("Next CONFIRMED booking will be MAIN for this category (after approvals).");
        else if (confirmed == 1) hint = simpleMsg("Next CONFIRMED booking MUST be EMERGENCY (required).");
        else hint = simpleMsg("MAIN + EMERGENCY done. No more confirmed bookings allowed.");

        hint.setForeground(new Color(90, 100, 115));
        mutualListPanel.add(hint);
        mutualListPanel.add(Box.createVerticalStrut(10));

        if (confirmed >= 2) {
            refreshPanel(mutualListPanel);
            return;
        }

        LocalTime t = START_HOUR;
        int countBookable = 0;

        while (!t.isAfter(END_HOUR)) {
            if (t.equals(BREAK_HOUR)) {
                addBreakRow(mutualListPanel, "12:00  (Break - Not bookable)");
                t = t.plusHours(1);
                continue;
            }

            TimeSlot slot = findRepoSlot(selectedMutualDate, t);

            if (slot == null) {
                JLabel row = styledLabel(t.format(timeFmt) + "  (No slot)", ROW_BAD_BG, ROW_BAD_FG);
                mutualListPanel.add(row);
                mutualListPanel.add(Box.createVerticalStrut(8));
                t = t.plusHours(1);
                continue;
            }

            boolean companyAvailable = slot.isAvailable();
            boolean meFree = !isUserBusy(slot);
            boolean blocked = blockedRule.getBlockMessageIfBlocked(slot) != null;

            boolean mutualOk = companyAvailable && meFree && !blocked;

            if (!mutualOk) {
                String reason;
                if (!companyAvailable) {
                    reason = slot.isHeld() ? "Pending approval" : "Booked by company";
                } else if (!meFree) reason = "You are busy";
                else reason = "Break blocked";

                JLabel row = styledLabel(t.format(timeFmt) + "  (" + reason + ")", ROW_BAD_BG, ROW_BAD_FG);
                mutualListPanel.add(row);
                mutualListPanel.add(Box.createVerticalStrut(8));
                t = t.plusHours(1);
                continue;
            }

            JRadioButton radio = new JRadioButton(t.format(timeFmt) + "  (Send Request)");
            radio.setOpaque(true);
            radio.setBackground(ROW_OK_BG);
            radio.setForeground(ROW_OK_FG);
            radio.setBorder(new EmptyBorder(10, 12, 10, 12));
            radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            radio.addActionListener(e -> selectedMutualSlot = slot);

            mutualGroup.add(radio);
            mutualListPanel.add(radio);
            mutualListPanel.add(Box.createVerticalStrut(8));

            countBookable++;
            t = t.plusHours(1);
        }

        if (countBookable == 0) {
            mutualListPanel.add(simpleMsg("No mutual slots available to request for this day."));
        }

        refreshPanel(mutualListPanel);
    }

    /**
     * Creates a simple message label for list panels.
     *
     * @param msg message text
     * @return label component
     */
    private JLabel simpleMsg(String msg) {
        JLabel l = new JLabel(msg);
        l.setBorder(new EmptyBorder(10, 10, 10, 10));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    /**
     * Creates a styled list row label.
     *
     * @param text row text
     * @param bg   background color
     * @param fg   foreground color
     * @return label component
     */
    private JLabel styledLabel(String text, Color bg, Color fg) {
        JLabel l = new JLabel(text);
        l.setOpaque(true);
        l.setBackground(bg);
        l.setForeground(fg);
        l.setBorder(new EmptyBorder(10, 12, 10, 12));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    /**
     * Revalidates and repaints a panel after updates.
     *
     * @param p panel to refresh
     */
    private void refreshPanel(JPanel p) {
        p.revalidate();
        p.repaint();
    }

    /**
     * Prompts the user for an integer value within an inclusive range.
     *
     * @param title   dialog title
     * @param message dialog message
     * @param min     minimum allowed value
     * @param max     maximum allowed value
     * @return value, or null if cancelled
     */
    private Integer promptIntInRange(String title, String message, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(this, message, title, JOptionPane.QUESTION_MESSAGE);
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
     * Sends a booking request for the selected mutual slot.
     * <p>
     * The slot is held while the request is pending approvals.
     * </p>
     */
    private void bookSelectedMutual() {
        if (!auth.isLoggedIn()) {
            DialogUtil.show(this, "Login Required", "You must login first.", DialogUtil.Type.WARNING);
            return;
        }

        if (selectedMutualSlot == null) {
            DialogUtil.show(this, "No Slot Selected", "Please select a mutual slot first!", DialogUtil.Type.WARNING);
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

        Integer participants = promptIntInRange("Participants", "Enter participants (1 - 5):", 1, 5);
        if (participants == null) return;

        int slotMinutes = (int) Duration.between(
                selectedMutualSlot.getStartDateTime(),
                selectedMutualSlot.getEndDateTime()
        ).toMinutes();

        Integer duration = promptIntInRange(
                "Duration",
                "Enter duration in minutes (1 - " + slotMinutes + "):",
                1, slotMinutes
        );
        if (duration == null) return;

        BookingResult result = requestService.submitRequest(
                auth.getCurrentUser(),
                selectedMutualSlot,
                duration,
                participants
        );

        DialogUtil.show(
                this,
                "Request Result",
                result.getMessage(),
                result.isSuccess() ? DialogUtil.Type.SUCCESS : DialogUtil.Type.ERROR
        );

        if (result.isSuccess()) {
            reloadAll();
        }
    }
}