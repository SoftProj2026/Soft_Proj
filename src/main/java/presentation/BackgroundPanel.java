package presentation;

import javax.swing.*;
import java.awt.*;

/**
 * A JPanel that paints a scaled background image and uses GridBagLayout for child centering.
 *
 * Used as a root container to display a full-window background image
 * with components centered using GridBagLayout.
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class BackgroundPanel extends JPanel {

    /**
     * The background image to paint.
     */
    private final Image background;

    /**
     * Constructs a new BackgroundPanel with the specified image.
     *
     * @param background the Image to use as a scaled background
     */
    public BackgroundPanel(Image background) {
        this.background = background;
        setLayout(new GridBagLayout());
        setOpaque(false);
        setFocusable(false);
        setEnabled(true);
    }

    /**
     * Paints the component, scaling the background image to fill the bounds.
     *
     * @param g the Graphics context to use for painting
     */
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