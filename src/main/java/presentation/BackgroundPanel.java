package presentation;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel that paints a scaled background image.
 *
 * Used as a root container to display a full-window background image
 * with components centered using GridBagLayout.
 */
public class BackgroundPanel extends JPanel {

    private final Image background;

    public BackgroundPanel(Image background) {
        this.background = background;
        setLayout(new GridBagLayout());

        setOpaque(false);
        setFocusable(false);
        setEnabled(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth();
        int h = getHeight();

        g2.drawImage(background, 0, 0, w, h, this);
        g2.dispose();
    }
}