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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Unified booking window displaying company availability, user availability, and mutual bookable slots for a given category.
 * Handles booking request submission and ensures only one active (pending + confirmed) item per (user, category).
 *
 * @author remaa
 * @version 1.0
 */
public class UnifiedBookingFrame extends JFrame {

    /**
     * The background color for the window.
     */
    private static final Color BG = UITheme.BG;

    /**
     * Day button default background color.
     */
    private static final Color DAY_BTN_BG = Color.WHITE;

    /**
     * Day button default foreground color.
     */
    private static final Color DAY_BTN_FG = new Color(25, 35, 45);

    /**
     * Day button background color when selected.
     */
    private static final Color DAY_BTN_SELECTED_BG = UITheme.PRIMARY_DARK;

    /**
     * Day button foreground color when selected.
     */
    private static final Color DAY_BTN_SELECTED_FG = Color.WHITE;

    /**
     * Background color for available slots.
     */
    private static final Color ROW_OK_BG = new Color(220, 252, 231);

    /**
     * Foreground color for available slots.
     */
    private static final Color ROW_OK_FG = new Color(20, 83, 45);

    /**
     * Background color for user free slots.
     */
    private static final Color ROW_FREE_BG = new Color(219, 234, 254);

    /**
     * Foreground color for user free slots.
     */
    private static final Color ROW_FREE_FG = new Color(30, 64, 175);

    /**
     * Background color for break rows.
     */
    private static final Color ROW_BREAK_BG = new Color(254, 249, 195);

    /**
     * Foreground color for break rows.
     */
    private static final Color ROW_BREAK_FG = new Color(113, 63, 18);

    /**
     * Background color for unavailable/busy slots.
     */
    private static final Color ROW_BAD_BG = new Color(254, 242, 242);

    /**
     * Foreground color for unavailable/busy slots.
     */
    private static final Color ROW_BAD_FG = new Color(180, 30, 30);

    /**
     * Background color for past slots.
     */
    private static final Color ROW_PAST_BG = new Color(240, 240, 240);

    /**
     * Foreground color for past slots.
     */
    private static final Color ROW_PAST_FG = new Color(120, 120, 120);

    /**
     * The starting hour for slot times (inclusive).
     */
    private static final LocalTime START_HOUR = LocalTime.of(9, 0);

    /**
     * The ending hour for slot times (inclusive).
     */
    private static final LocalTime END_HOUR = LocalTime.of(16, 0);

    /**
     * The break hour (excluded from booking).
     */
    private static final LocalTime BREAK_HOUR = LocalTime.of(12, 0);

    /**
     * Current authentication service (for user info).
     */
    private final AuthService auth;

    /**
     * Booking service for availability/business logic.
     */
    private final BookingService booking;

    /**
     * The data repository instance.
     */
    private final DataRepository repo;

    /**
     * The active booking category.
     */
    private final Category category;

    /**
     * Booking request service for this frame.
     */
    private final BookingRequestService requestService;

    /**
     * Rule to determine blocked (unbookable) slots.
     */
    private final BlockedSlotsRule blockedRule = new BlockedSlotsRule();

    /**
     * Dates shown in the week view (next 7 days).
     */
    private final List<LocalDate> weekDates = new ArrayList<>();

    /**
     * Selected date for company column.
     */
    private LocalDate selectedCompanyDate;

    /**
     * Selected date for my free slots column.
     */
    private LocalDate selectedMyDate;

    /**
     * Selected date for mutual slots column.
     */
    private LocalDate selectedMutualDate;

    /**
     * Panel containing company's day bar buttons.
     */
    private JPanel companyDayBarWrap;

    /**
     * Panel containing user's day bar buttons.
     */
    private JPanel myDayBarWrap;

    /**
     * Panel containing mutual's day bar buttons.
     */
    private JPanel mutualDayBarWrap;

    /**
     * Panel listing company's available slots.
     */
    private final JPanel companyListPanel = new JPanel();

    /**
     * Panel listing user's free/busy slots.
     */
    private final JPanel myListPanel = new JPanel();

    /**
     * Panel listing mutual slots.
     */
    private final JPanel mutualListPanel = new JPanel();

    /**
     * Button group to ensure only one mutual slot is selected.
     */
    private final ButtonGroup mutualGroup = new ButtonGroup();

    /**
     * The selected slot in the mutual column.
     */
    private TimeSlot selectedMutualSlot;

    /**
     * Formatter for day names.
     */
    private final DateTimeFormatter dayNameFmt = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);

    /**
     * Formatter for slot time.
     */
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructs the unified booking frame for a user and category.
     *
     * @param auth current authentication service
     * @param booking booking service
     * @param repo data repository
     * @param category selected booking category
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
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(12, 12));

        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            weekDates.add(today.plusDays(i));
        }

        selectedCompanyDate = weekDates.get(0);
        selectedMyDate = weekDates.get(0);
        selectedMutualDate = weekDates.get(0);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildColumns(), BorderLayout.CENTER);
        add(buildBottomBar(), BorderLayout.SOUTH);

        reloadAll();
    }

    /**
     * Builds the header panel for the booking UI.
     *
     * @return the header panel
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
     * Builds the three-column view: company, user, mutual slots.
     *
     * @return the columns panel
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
     * Builds the company column/card view.
     *
     * @return the panel for company available slots
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
     * Builds the user column/card view.
     *
     * @return the panel for user's free slots
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
     * Builds the mutual slots column/card view.
     *
     * @return the panel for mutual slots and booking
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
     * Creates the visual shell/container for a column/card.
     *
     * @return bordered and padded panel
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
     * Builds a titled header for a column/card.
     *
     * @param titleText header text
     * @return panel containing the header
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
     * Wraps a list panel in a scroll pane.
     *
     * @param list the panel to wrap
     * @return scrollable view of the panel
     */
    private JScrollPane wrapScroll(JPanel list) {
        JScrollPane scroll = new JScrollPane(list);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    /**
     * Builds the bottom action bar with booking and close buttons.
     *
     * @return bottom panel
     */
    private JPanel buildBottomBar() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(BG);

        JButton closeBtn = UITheme.secondaryButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JButton bookBtn = UITheme.primaryButton("Book Selected Slot");
        bookBtn.addActionListener(e -> bookSelectedMutual());

        bottom.add(closeBtn);
        bottom.add(bookBtn);
        return bottom;
    }

    /**
     * Formats a LocalDate as a button label.
     *
     * @param d the date
     * @return display string for the button
     */
    private String formatDayButton(LocalDate d) {
        return d.format(dayNameFmt) + " " + d.getDayOfMonth() + "/" + d.getMonthValue();
    }

    /**
     * Renders the day bars for each card.
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
     * Builds a bar of buttons for each date in the week.
     *
     * @param selected currently selected date
     * @param onSelect action to perform when a date is clicked
     * @return the panel containing the bar
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
     * Styles a day selection button.
     *
     * @param b       the button to style
     * @param selected whether this day is currently selected
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
     * Reloads and updates all panels/columns.
     */
    private void reloadAll() {
        renderDayBars();
        reloadCompany();
        reloadMy();
        reloadMutual();
    }

    /**
     * Counts all the user's active (pending/confirmed) items for this category.
     *
     * @return count of active items
     */
    private long countActiveForThisCategory() {
        if (!auth.isLoggedIn() || auth.getCurrentUser() == null) return 0;

        String user = auth.getCurrentUser().getUsername();
        String cat = category.getName();

        return repo.countConfirmedForUserCategory(user, cat)
                + repo.countPendingRequestsForUserCategory(user, cat);
    }

    /**
     * Checks if the current user is busy during the given slot.
     *
     * @param slot the time slot
     * @return true if user is busy (overlaps with another confirmed slot)
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
     * Finds a slot in the repository matching the specified date and hour for this category.
     *
     * @param date  the date to match
     * @param hour  the time/hour to match
     * @return timeslot if found, otherwise null
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
     * Adds a styled label representing a break row to a panel.
     *
     * @param panel parent panel
     * @param label display text
     */
    private void addBreakRow(JPanel panel, String label) {
        JLabel row = styledLabel(label, ROW_BREAK_BG, ROW_BREAK_FG);
        row.setToolTipText("Break time (12:00 - 13:00).");
        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }

    /**
     * Adds a styled label representing a past (unbookable) row to a panel.
     *
     * @param panel parent panel
     * @param label display text
     */
    private void addPastRow(JPanel panel, String label) {
        JLabel row = styledLabel(label, ROW_PAST_BG, ROW_PAST_FG);
        row.setToolTipText("Past time - not bookable.");
        panel.add(row);
        panel.add(Box.createVerticalStrut(8));
    }

    /**
     * Reloads the company availability column for the selected date.
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
     * Reloads the user free/busy column for the selected date.
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
     * Reloads the mutual slots column (times where company and user are available), allows booking.
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

        long active = countActiveForThisCategory();

        JLabel hint;
        if (active == 0) {
            hint = simpleMsg("You can submit ONE request for this category (pending approval).");
        } else {
            hint = simpleMsg("You already have an active request/booking in this category. No more requests allowed.");
        }

        hint.setForeground(new Color(90, 100, 115));
        mutualListPanel.add(hint);
        mutualListPanel.add(Box.createVerticalStrut(10));

        if (active >= 1) {
            refreshPanel(mutualListPanel);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
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

            boolean isPast = slot.getStartDateTime() != null && slot.getStartDateTime().isBefore(now);
            if (isPast) {
                addPastRow(mutualListPanel, t.format(timeFmt) + "  (Past - Not bookable)");
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
                } else if (!meFree) {
                    reason = "You are busy";
                } else {
                    reason = "Break blocked";
                }

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
     * Creates a styled message label.
     *
     * @param msg the message text
     * @return styled JLabel
     */
    private JLabel simpleMsg(String msg) {
        JLabel l = new JLabel(msg);
        l.setBorder(new EmptyBorder(10, 10, 10, 10));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }

    /**
     * Creates a styled label with colors.
     *
     * @param text label text
     * @param bg   background color
     * @param fg   foreground color
     * @return JLabel styled component
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
     * Refreshes a panel after modifications.
     *
     * @param p the panel to update
     */
    private void refreshPanel(JPanel p) {
        p.revalidate();
        p.repaint();
    }

    /**
     * Shows an input dialog and returns a value in the given range (inclusive).
     *
     * @param title   dialog title
     * @param message dialog message
     * @param min     min value (inclusive)
     * @param max     max value (inclusive)
     * @return entered integer or null if cancelled
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
     * Handles booking submission for the selected mutual slot.
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

        if (selectedMutualSlot.getStartDateTime() != null
                && selectedMutualSlot.getStartDateTime().isBefore(LocalDateTime.now())) {
            DialogUtil.show(this, "Not Allowed", "You cannot book a past time slot.", DialogUtil.Type.ERROR);
            selectedMutualSlot = null;
            mutualGroup.clearSelection();
            reloadMutual();
            return;
        }

        long active = countActiveForThisCategory();
        if (active >= 1) {
            DialogUtil.show(
                    this,
                    "Request Not Allowed",
                    "You already have an active booking/request for \"" + category.getName() + "\".",
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