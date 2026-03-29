package Test;

import org.junit.jupiter.api.Test;
import presentation.RoundedPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class RoundedPanelTest {

    @Test
    void constructor_setsOpaqueFalse() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            RoundedPanel p = new RoundedPanel(12, Color.BLUE);
            assertFalse(p.isOpaque(), "RoundedPanel should be non-opaque by default");
        });
    }

    @Test
    void paintComponent_drawsFillAndBorder() throws Exception {
        final int w = 120, h = 60;
        final Color fill = new Color(123, 45, 67);

        SwingUtilities.invokeAndWait(() -> {
            RoundedPanel p = new RoundedPanel(16, fill);
            p.setSize(w, h);
            p.doLayout();

            BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                p.paint(g2);
            } finally {
                g2.dispose();
            }

            int centerRgb = img.getRGB(w / 2, h / 2);
            int fillRgb = fill.getRGB();

            assertEquals(
                    (fillRgb & 0x00FFFFFF),
                    (centerRgb & 0x00FFFFFF),
                    "Center pixel should match the panel fill color (ignoring alpha)"
            );

            int edgeX = 2, edgeY = 2;
            int edgeRgb = img.getRGB(edgeX, edgeY);
            assertNotEquals((fillRgb & 0x00FFFFFF), (edgeRgb & 0x00FFFFFF),
                    "Edge pixel should not exactly match fill color due to border drawing");
        });
    }

    @Test
    void paintComponent_zeroSize_noException() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            RoundedPanel p = new RoundedPanel(10, Color.GREEN);
            p.setSize(0, 0);

            BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = img.createGraphics();
            try {
                p.paint(g2);
            } finally {
                g2.dispose();
            }
        });
    }
}