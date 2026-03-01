package presentation;

import Service.AuthService;
import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * View-only window that shows slots where the current user is free
 * (no overlap with {@link AppointmentStatus#CONFIRMED} appointments),
 * regardless of company availability.
 *
 * <p>
 * Note: No "Close" button (close via window X or by closing the parent window).
 * </p>
 */
public class MyFreeSlotsFrame extends JFrame {

    private static final Color BG = UITheme.BG;

    private static final Color ROW_FREE_BG = new Color(219, 234, 254);
    private static final Color ROW_FREE_FG = new Color(30, 64, 175);

    private final AuthService auth;
    private final DataRepository repo;
    private final Category category;

    private final JPanel listPanel = new JPanel();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /**
     * Creates the "My Free Slots" window.
     *
     * @param auth     authentication service (used to determine current user)
     * @param repo     data repository containing slots/appointments
     * @param category selected category
     */
    public MyFreeSlotsFrame(AuthService auth, DataRepository repo, Category category) {
        this.auth = auth;
        this.repo = repo;
        this.category = category;

        setTitle("My Free Slots - " + category.getName());
        setSize(520, 600);
        setLocationByPlatform(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Slots where I'm free (View only)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("These slots do not overlap with your confirmed bookings.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(90, 100, 115));
        header.add(subtitle, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(listPanel);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 14));
        scroll.getViewport().setBackground(Color.WHITE);
        add(scroll, BorderLayout.CENTER);

        load();
    }

    /**
     * Checks whether the current user has a confirmed appointment that overlaps this slot.
     *
     * @param slot slot to check
     * @return true if the user is busy during this slot; false otherwise
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
     * Loads and renders the list of free slots for the current user in the selected category.
     */
    private void load() {
        listPanel.removeAll();

        if (!auth.isLoggedIn()) {
            JLabel msg = new JLabel("You must login first to view 'My Free Slots'.");
            msg.setBorder(new EmptyBorder(10, 10, 10, 10));
            listPanel.add(msg);
            listPanel.revalidate();
            listPanel.repaint();
            return;
        }

        int count = 0;

        for (TimeSlot slot : repo.getSlots()) {
            if (slot.getCategory() == null) continue;
            if (!slot.getCategory().getName().equalsIgnoreCase(category.getName())) continue;

            if (isUserBusy(slot)) continue;

            JLabel row = new JLabel(slot.getStartDateTime().format(fmt));
            row.setOpaque(true);
            row.setBorder(new EmptyBorder(10, 12, 10, 12));
            row.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            row.setBackground(ROW_FREE_BG);
            row.setForeground(ROW_FREE_FG);

            listPanel.add(row);
            listPanel.add(Box.createVerticalStrut(8));
            count++;
        }

        if (count == 0) {
            JLabel none = new JLabel("No free slots for you in this category.");
            none.setBorder(new EmptyBorder(10, 10, 10, 10));
            listPanel.add(none);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}