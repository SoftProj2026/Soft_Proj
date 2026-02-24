package presentation;

import domain.*;
import Service.*;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MainDashboardFrame extends JFrame {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;

    private JPanel slotPanel;
    private ButtonGroup slotGroup = new ButtonGroup();
    private TimeSlot selectedSlot;

    private static final Color BG = new Color(245, 248, 255);
    private static final Color BLUE = new Color(33, 120, 255);
    private static final Color BLUE_DARK = new Color(18, 78, 180);

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

        for (Category c : repo.getCategories()) {
            JButton btn = createCategoryButton(c.getName());
            btn.addActionListener(e -> loadSlots(c));
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
    }

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

    private void loadSlots(Category category) {
        slotPanel.removeAll();
        slotGroup = new ButtonGroup();
        selectedSlot = null;

        List<TimeSlot> slots = repo.getSlots();

        for (TimeSlot slot : slots) {
            if (slot.getCategory().equals(category) && slot.isAvailable()) {

                JRadioButton radio = new JRadioButton(slot.getStartDateTime().toString());
                radio.setBackground(Color.WHITE);
                radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));

                radio.addActionListener(e -> selectedSlot = slot);

                slotGroup.add(radio);
                slotPanel.add(radio);
            }
        }

        slotPanel.revalidate();
        slotPanel.repaint();
    }

    private Integer promptIntInRange(String title, String message, int min, int max) {
        while (true) {
            String input = JOptionPane.showInputDialog(
                    this,
                    message,
                    title,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (input == null) {
                return null; 
            }

            input = input.trim();
            if (input.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Value cannot be empty. Please try again.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            int value;
            try {
                value = Integer.parseInt(input);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a valid number.",
                        "Invalid Input",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            if (value < min || value > max) {
                JOptionPane.showMessageDialog(this,
                        "Value must be between " + min + " and " + max + ". Please try again.",
                        "Out of Range",
                        JOptionPane.ERROR_MESSAGE);
                continue;
            }

            return value;
        }
    }

    private void bookSelectedSlot() {

        if (selectedSlot == null) {
            JOptionPane.showMessageDialog(this, "Please select a slot first!");
            return;
        }

        if (!auth.isLoggedIn()) {
            JOptionPane.showMessageDialog(this, "You must login first!");
            return;
        }

        Integer participants = promptIntInRange(
                "Participants",
                "Enter participants (1 - 3):",
                1, 3
        );
        if (participants == null) return;

        Integer duration = promptIntInRange(
                "Duration",
                "Enter duration in minutes (1 - 30):",
                1, 30
        );
        if (duration == null) return;

        if (duration > selectedSlot.getDuration()) {
            JOptionPane.showMessageDialog(this,
                    "Duration cannot exceed the slot duration (" + selectedSlot.getDuration() + " minutes).",
                    "Invalid Duration",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment appointment = new Appointment(
                auth.getCurrentUser(),
                selectedSlot,
                duration,
                participants
        );

        BookingResult result = booking.book(appointment);
        JOptionPane.showMessageDialog(this, result.getMessage());

        if (result.isSuccess()) {
            slotPanel.removeAll();
            slotPanel.revalidate();
            slotPanel.repaint();
        }
    }
}