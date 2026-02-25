package presentation;

import javax.swing.*;
import java.awt.*;

public class BackgroundPanel extends JPanel {

    private final Image background;

    public BackgroundPanel(Image background) {
        this.background = background;
        setLayout(new GridBagLayout()); // عشان نحط الفورم بالنص
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        // نغطي كل الشاشة (stretch)
        g2.drawImage(background, 0, 0, w, h, this);
        g2.dispose();
    }
}