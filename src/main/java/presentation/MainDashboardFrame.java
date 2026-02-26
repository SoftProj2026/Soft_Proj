package presentation;

import Service.AuthService;
import Service.BookingService;
import domain.Category;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Main booking dashboard UI.
 * <p>
 * Displays categories. When a category is clicked, it opens 3 separate windows:
 * <ol>
 *   <li>Company available slots (view only)</li>
 *   <li>My free slots (view only)</li>
 *   <li>Mutual slots (booking)</li>
 * </ol>
 * </p>
 * Also provides access to "My Bookings" and Logout.
 */
public class MainDashboardFrame extends JFrame {

    private final AuthService auth;
    private final BookingService booking;
    private final DataRepository repo;

    private static final Color BG = new Color(245, 248, 255);
    private static final Color BLUE = new Color(33, 120, 255);

    /**
     * Creates the main dashboard frame.
     *
     * @param auth    authentication service
     * @param booking booking service
     * @param repo    data repository
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
                CompanyAvailableSlotsFrame f1 = new CompanyAvailableSlotsFrame(repo, c);
                MyFreeSlotsFrame f2 = new MyFreeSlotsFrame(auth, repo, c);
                MutualBookingFrame f3 = new MutualBookingFrame(auth, booking, repo, c, f1, f2);

                int w = 520;   
                int h = 640;   
                int gap = 20;  

                Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
                int totalW = (w * 3) + (gap * 2);

                int startX = Math.max(10, (screen.width - totalW) / 2);
                int y = Math.max(20, (screen.height - h) / 2);

                f1.setSize(w, h);
                f2.setSize(w, h);
                f3.setSize(w, h);

                f1.setLocation(startX, y);
                f2.setLocation(startX + w + gap, y);
                f3.setLocation(startX + (w + gap) * 2, y);

                f1.setVisible(true);
                f2.setVisible(true);
                f3.setVisible(true);
            });
            categoryPanel.add(btn);
        }

        JScrollPane catScroll = new JScrollPane(categoryPanel);
        catScroll.setBorder(null);
        catScroll.getViewport().setBackground(BG);
        add(catScroll, BorderLayout.CENTER);

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

        bottom.add(myBookingsBtn);
        bottom.add(logoutBtn);

        add(bottom, BorderLayout.SOUTH);
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
}