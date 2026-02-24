package presentation;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class BackgroundPanel extends JPanel {
    private final Image backgroundImage;
    private final Color fallback = new Color(245, 248, 255);

    public BackgroundPanel(String resourcePath) {
        setLayout(new GridBagLayout());

        URL url = getClass().getResource(resourcePath);
        if (url == null) {
            backgroundImage = null; 
            System.err.println("Image not found on classpath: " + resourcePath);
            return;
        }
        backgroundImage = new ImageIcon(url).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage == null) {
            g.setColor(fallback);
            g.fillRect(0, 0, getWidth(), getHeight());
            return;
        }

        g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
    }
}