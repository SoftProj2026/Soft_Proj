package presentation;

import javax.swing.*;
import java.awt.*;

public class RoundedPanel extends JPanel {
    private final int radius;
    private final Color fill;

    public RoundedPanel(int radius, Color fill) {
        this.radius = radius;
        this.fill = fill;
        setOpaque(false);  
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(fill);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

        g2.setColor(new Color(255, 255, 255, 80));
        g2.setStroke(new BasicStroke(2f));
        g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, radius, radius);

        g2.dispose();
        super.paintComponent(g);
    }
}