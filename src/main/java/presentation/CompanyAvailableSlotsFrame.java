package presentation;

import Service.BlockedSlotsRule;
import domain.Category;
import domain.TimeSlot;
import persistence.DataRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.format.DateTimeFormatter;

/**
 * View-only window that shows slots available for the company (slot.isAvailable())
 * for a selected category.
 * <p>
 * Break-blocked slots are highlighted using a warning style.
 * </p>
 *
 * <p>
 * Note: No "Close" button (close via window X or by closing the parent window).
 * </p>
 */
public class CompanyAvailableSlotsFrame extends JFrame {

    private static final Color BG = UITheme.BG;

    private static final Color ROW_OK_BG = new Color(220, 252, 231);      // green-100
    private static final Color ROW_OK_FG = new Color(20, 83, 45);         // green-900

    private static final Color ROW_BLOCK_BG = new Color(254, 249, 195);   // yellow-100
    private static final Color ROW_BLOCK_FG = new Color(113, 63, 18);     // amber-900

    private final DataRepository repo;
    private final Category category;

    private final JPanel listPanel = new JPanel();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private final BlockedSlotsRule blockedRule = new BlockedSlotsRule();

    /**
     * Creates the company availability window for a category.
     *
     * @param repo     data repository containing slots
     * @param category selected category
     */
    public CompanyAvailableSlotsFrame(DataRepository repo, Category category) {
        this.repo = repo;
        this.category = category;

        setTitle("Company Available Slots - " + category.getName());
        setSize(520, 600);
        setLocationByPlatform(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(10, 10));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("Available slots for company (View only)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Shows company availability (break-blocked highlighted).");
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
     * Loads and renders the list of available company slots for the category.
     */
    private void load() {
        listPanel.removeAll();

        int count = 0;

        for (TimeSlot slot : repo.getSlots()) {
            if (slot.getCategory() == null) continue;
            if (!slot.getCategory().getName().equalsIgnoreCase(category.getName())) continue;

            if (!slot.isAvailable()) continue;

            String start = slot.getStartDateTime().format(fmt);
            String blockMsg = blockedRule.getBlockMessageIfBlocked(slot);
            boolean blocked = (blockMsg != null);

            JLabel row = new JLabel(start + (blocked ? "  (Break Blocked)" : ""));
            row.setOpaque(true);
            row.setBorder(new EmptyBorder(10, 12, 10, 12));
            row.setFont(new Font("Segoe UI", Font.PLAIN, 13));

            if (blocked) {
                row.setBackground(ROW_BLOCK_BG);
                row.setForeground(ROW_BLOCK_FG);
                row.setToolTipText(blockMsg);
            } else {
                row.setBackground(ROW_OK_BG);
                row.setForeground(ROW_OK_FG);
            }

            listPanel.add(row);
            listPanel.add(Box.createVerticalStrut(8));
            count++;
        }

        if (count == 0) {
            JLabel none = new JLabel("No available company slots for this category.");
            none.setBorder(new EmptyBorder(10, 10, 10, 10));
            listPanel.add(none);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }
}