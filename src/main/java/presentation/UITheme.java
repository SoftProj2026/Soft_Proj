package presentation;

import javax.swing.*;
import java.awt.*;

public final class UITheme {

    private UITheme() {}

    public static final Color BG = new Color(245, 248, 255);
    public static final Color CARD = Color.WHITE;
    public static final Color PRIMARY = new Color(33, 120, 255);
    public static final Color PRIMARY_DARK = new Color(18, 78, 180);
    public static final Color TEXT = new Color(25, 35, 45);
    public static final Color MUTED = new Color(110, 120, 135);

    public static void apply() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {}

        Font base = new Font("Segoe UI", Font.PLAIN, 14);
        UIManager.put("defaultFont", base);

        UIManager.put("Panel.background", BG);
        UIManager.put("Viewport.background", BG);

        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 13));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 13));
    }

    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(PRIMARY_DARK);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton secondaryButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(Color.WHITE);
        b.setForeground(PRIMARY_DARK);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 215, 235), 2, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}