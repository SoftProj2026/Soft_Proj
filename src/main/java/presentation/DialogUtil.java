package presentation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Utility helper to display consistent styled dialogs (info/success/warning/error).
 * @author remaa
 * @version 1.0
 */
public final class DialogUtil {

    private DialogUtil() {}

    /**
     * Dialog types supported by this utility.
     */
    public enum Type {
        /** Informational message. */
        INFO,
        /** Success message. */
        SUCCESS,
        /** Warning message. */
        WARNING,
        /** Error message. */
        ERROR
    }

    /**
     * Shows a styled dialog message.
     *
     * @param parent  parent component (can be null)
     * @param title   dialog title
     * @param message dialog message
     * @param type    visual style type (INFO/SUCCESS/WARNING/ERROR)
     */
    public static void show(Component parent, String title, String message, Type type) {
        Color bg;
        Color border;
        Icon icon;

        switch (type) {
            case SUCCESS:
                bg = new Color(236, 253, 245);
                border = new Color(16, 185, 129);
                icon = UIManager.getIcon("OptionPane.informationIcon");
                break;
            case WARNING:
                bg = new Color(255, 251, 235);
                border = new Color(245, 158, 11);
                icon = UIManager.getIcon("OptionPane.warningIcon");
                break;
            case ERROR:
                bg = new Color(254, 242, 242);
                border = new Color(239, 68, 68);
                icon = UIManager.getIcon("OptionPane.errorIcon");
                break;
            case INFO:
            default:
                bg = new Color(239, 246, 255);
                border = new Color(59, 130, 246);
                icon = UIManager.getIcon("OptionPane.informationIcon");
                break;
        }

        JPanel card = new JPanel(new BorderLayout(12, 12));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, 2, true),
                new EmptyBorder(14, 14, 14, 14)
        ));
        card.setBackground(bg);

        JLabel iconLabel = new JLabel(icon);
        card.add(iconLabel, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLabel.setForeground(new Color(25, 35, 45));

        JLabel msgLabel = new JLabel("<html>" + escapeHtml(message).replace("\n", "<br/>") + "</html>");
        msgLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        msgLabel.setForeground(new Color(25, 35, 45));

        text.add(titleLabel);
        text.add(Box.createVerticalStrut(6));
        text.add(msgLabel);

        card.add(text, BorderLayout.CENTER);

        JOptionPane.showMessageDialog(
                parent,
                card,
                title,
                JOptionPane.PLAIN_MESSAGE
        );
    }

    /**
     * Escapes minimal HTML characters for safe display in Swing HTML labels.
     *
     * @param s input string
     * @return escaped string (never null)
     */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}