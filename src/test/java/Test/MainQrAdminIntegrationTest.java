package Test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
public class MainQrAdminIntegrationTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    @TempDir
    Path tmpHome;

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        final Throwable[] err = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try { r.run(); } catch (Throwable t) { err[0] = t; }
        });
        if (err[0] != null) {
            if (err[0] instanceof RuntimeException) throw (RuntimeException) err[0];
            throw new RuntimeException(err[0]);
        }
    }

    private static <T extends Component> T findFirstComponent(Container root, Class<T> type, java.util.function.Predicate<T> pred) {
        if (root == null) return null;
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                T t = type.cast(c);
                if (pred == null || pred.test(t)) return t;
            }
            if (c instanceof Container) {
                T nested = findFirstComponent((Container) c, type, pred);
                if (nested != null) return nested;
            }
        }
        return null;
    }

    private static JCheckBox findCheckBoxByText(Container root, String text) {
        return findFirstComponent(root, JCheckBox.class, cb -> text.equals(cb.getText()));
    }

    private static JButton findButtonByText(Container root, String text) {
        return findFirstComponent(root, JButton.class, b -> text.equals(b.getText()));
    }

    private static JLabel findLabelByText(Container root, String text) {
        return findFirstComponent(root, JLabel.class, l -> text.equals(l.getText()));
    }

    private static JPasswordField findPasswordFieldNearLabel(Container root, String labelText) {
        JLabel label = findLabelByText(root, labelText);
        if (label == null) return null;

        Container parent = label.getParent();
        if (parent == null) return null;

        Component[] comps = parent.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == label) {
                for (int j = i + 1; j < comps.length; j++) {
                    if (comps[j] instanceof JPasswordField) return (JPasswordField) comps[j];
                }
                for (int j = i - 1; j >= 0; j--) {
                    if (comps[j] instanceof JPasswordField) return (JPasswordField) comps[j];
                }
            }
        }
        return null;
    }

    private static Window waitForWindowByTitle(String title, long timeoutMs) throws Exception {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            final Window[] found = new Window[1];
            runOnEdtAndWait(() -> {
                for (Window w : Window.getWindows()) {
                    if (w == null || !w.isDisplayable()) continue;
                    if (w instanceof Frame f) {
                        if (title.equals(f.getTitle())) { found[0] = w; return; }
                    } else if (w instanceof Dialog d) {
                        if (title.equals(d.getTitle())) { found[0] = w; return; }
                    }
                }
            });
            if (found[0] != null) return found[0];
            TimeUnit.MILLISECONDS.sleep(50);
        }
        return null;
    }

    @Test
    void main_qrAdminLogin_opensAdminDashboard() throws Exception {
        System.setProperty("user.home", tmpHome.toString());

        runOnEdtAndWait(() -> mainapp.Main.main(new String[0]));

        Window loginWin = waitForWindowByTitle("Login", 4000);
        assertNotNull(loginWin, "Login window should appear after Main.main()");

        assertTrue(loginWin instanceof JFrame);
        JFrame loginFrame = (JFrame) loginWin;
        JCheckBox qrAdmin = findCheckBoxByText(loginFrame.getContentPane(), "QR Admin for Company");
        assertNotNull(qrAdmin, "QR Admin checkbox should exist");

        JButton loginBtn = findButtonByText(loginFrame.getContentPane(), "Log In");
        assertNotNull(loginBtn, "Log In button should exist");

        runOnEdtAndWait(() -> qrAdmin.setSelected(true));

        JPasswordField adminKeyField = findPasswordFieldNearLabel(loginFrame.getContentPane(), "QR Admin Key:");
        assertNotNull(adminKeyField, "QR Admin Key field should exist when QR Admin mode is enabled");

        runOnEdtAndWait(() -> {
            adminKeyField.setText("ADMIN2026");
            loginBtn.doClick();
        });

        Window dash = waitForWindowByTitle("Admin Dashboard", 5000);
        assertNotNull(dash, "Admin Dashboard should open after QR admin login");

        runOnEdtAndWait(() -> {
            for (Window w : Window.getWindows()) {
                if (w != null && w.isDisplayable()) w.dispose();
            }
        });
    }
}