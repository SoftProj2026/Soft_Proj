
package presentation;

import domain.*;
import Service.*;
import persistence.DataRepository;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainDashboardFrame extends JFrame {

    private AuthService auth;
    private BookingService booking;
    private DataRepository repo;

    private JPanel slotPanel;
    private ButtonGroup slotGroup = new ButtonGroup();
    private TimeSlot selectedSlot;

    public MainDashboardFrame(AuthService auth,
                              BookingService booking,
                              DataRepository repo) {

        this.auth = auth;
        this.booking = booking;
        this.repo = repo;

        setTitle("Booking Dashboard");
        setSize(700, 500);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel categoryPanel = new JPanel(new GridLayout(1, 3, 10, 10));

        for (Category c : repo.getCategories()) {
            JButton btn = new JButton(c.getName());
            btn.setFocusPainted(false);
            btn.setBackground(new Color(70,130,180));
            btn.setForeground(Color.WHITE);
            btn.setFont(new Font("Arial", Font.BOLD, 16));

            btn.addActionListener(e -> loadSlots(c));

            categoryPanel.add(btn);
        }

        add(categoryPanel, BorderLayout.NORTH);

        slotPanel = new JPanel();
        slotPanel.setLayout(new BoxLayout(slotPanel, BoxLayout.Y_AXIS));

        add(new JScrollPane(slotPanel), BorderLayout.CENTER);

        JButton bookBtn = new JButton("Book Selected Slot");
        bookBtn.setFont(new Font("Arial", Font.BOLD, 16));

        bookBtn.addActionListener(e -> bookSelectedSlot());

        add(bookBtn, BorderLayout.SOUTH);
    }

    private void loadSlots(Category category) {

        slotPanel.removeAll();
        slotGroup = new ButtonGroup();
        selectedSlot = null;

        List<TimeSlot> slots = repo.getSlots();

        for (TimeSlot slot : slots) {

            if (slot.getCategory().equals(category)
                    && slot.isAvailable()) {

                JRadioButton radio = new JRadioButton(
                        slot.getStartDateTime().toString()
                );

                radio.addActionListener(e -> selectedSlot = slot);

                slotGroup.add(radio);
                slotPanel.add(radio);
            }
        }

        slotPanel.revalidate();
        slotPanel.repaint();
    }

    private void bookSelectedSlot() {

        if (selectedSlot == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a slot first!");
            return;
        }

        Appointment appointment = new Appointment(
                auth.getCurrentUser(),
                selectedSlot,
                selectedSlot.getDuration(),
                1
        );

        BookingResult result = booking.book(appointment);

        JOptionPane.showMessageDialog(this, result.getMessage());

        if (result.isSuccess()) {
            selectedSlot.book();
            slotPanel.removeAll();
            slotPanel.revalidate();
            slotPanel.repaint();
        }
    }
}