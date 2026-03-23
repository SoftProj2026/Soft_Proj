package Test;

import Service.AuthService;
import Service.BookingRequestService;
import Service.BookingService;
import domain.*;
import persistence.DataRepository;
import presentation.LoginFrame;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for presentation.LoginFrame covering QR admin, Category admin and Normal login paths.
 *
 * - Runs Swing interactions on the EDT.
 * - Uses a temporary user.home so RepoStorage.save writes to a temp location.
 */
class LoginFrameTest {

    private static void runOnEdt(RunnableWithException r) throws Exception {
        Throwable[] err = new Throwable[1];
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        SwingUtilities.invokeAndWait(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err[0] = t;
            }
        });
        if (err[0] != null) {
            if (err[0] instanceof RuntimeException) throw (RuntimeException) err[0];
            if (err[0] instanceof Error) throw (Error) err[0];
            throw (Exception) err[0];
        }
    }

    @FunctionalInterface
    private interface RunnableWithException { void run() throws Exception; }

    private JLabel findLabel(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel && text.equals(((JLabel) c).getText())) return (JLabel) c;
            if (c instanceof Container) {
                JLabel r = findLabel((Container) c, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private Component findSiblingField(JLabel label) {
        Container parent = label.getParent();
        if (parent == null) return null;
        Component[] comps = parent.getComponents();
        for (int i = 0; i < comps.length; i++) {
            if (comps[i] == label) {
                for (int j = i+1; j < comps.length; j++) {
                    if (comps[j] instanceof JTextComponent || comps[j] instanceof JComboBox || comps[j] instanceof JPasswordField) {
                        return comps[j];
                    }
                }
                for (int j = i-1; j >= 0; j--) {
                    if (comps[j] instanceof JTextComponent || comps[j] instanceof JComboBox || comps[j] instanceof JPasswordField) {
                        return comps[j];
                    }
                }
            }
        }
        return null;
    }

    private JCheckBox findCheckBox(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JCheckBox && text.equals(((JCheckBox) c).getText())) return (JCheckBox) c;
            if (c instanceof Container) {
                JCheckBox r = findCheckBox((Container) c, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
            if (c instanceof Container) {
                JButton r = findButton((Container) c, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private JComboBox<Category> findCategoryBox(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JComboBox) {
                try {
                    return (JComboBox<Category>) c;
                } catch (ClassCastException ignored) {}
            }
            if (c instanceof Container) {
                JComboBox<Category> r = findCategoryBox((Container) c);
                if (r != null) return r;
            }
        }
        return null;
    }

    private boolean repoFileExists(Path userHome) {
        Path p = userHome.resolve(".Soft_Proj").resolve("data.json");
        return Files.exists(p);
    }

    @Test
    void qr_admin_login_success_and_failure_write_repo_and_call_loginAsAdmin(@TempDir Path tmp) throws Exception {
        System.setProperty("user.home", tmp.toString());

        DataRepository repo = new DataRepository(); 
        AuthService auth = mock(AuthService.class);
        BookingService bookingService = mock(BookingService.class);

        final LoginFrame[] frameRef = new LoginFrame[1];
        runOnEdt(() -> frameRef[0] = new LoginFrame(auth, bookingService, repo));
        LoginFrame frame = frameRef[0];

        JCheckBox qrCheck = findCheckBox(frame.getContentPane(), "QR Admin for Company");
        assertNotNull(qrCheck);

        JLabel adminLabel = findLabel(frame.getContentPane(), "QR Admin Key:");
        assertNotNull(adminLabel);
        Component adminFieldComp = findSiblingField(adminLabel);
        assertNotNull(adminFieldComp);
        assertTrue(adminFieldComp instanceof JPasswordField);
        JPasswordField adminField = (JPasswordField) adminFieldComp;

        JButton loginBtn = findButton(frame.getContentPane(), "Log In");
        assertNotNull(loginBtn);

        runOnEdt(() -> {
            qrCheck.setSelected(true);
            adminField.setText("WRONGKEY");
            loginBtn.doClick();
        });

        verify(auth, never()).loginAsAdmin();

        when(auth.loginAsAdmin()).thenReturn(true);

        runOnEdt(() -> {
            qrCheck.setSelected(true);
            adminField.setText("ADMIN2026");
            loginBtn.doClick();
        });

        verify(auth, atLeastOnce()).loginAsAdmin();

        assertTrue(repoFileExists(tmp), "Repo data.json should be created in temp user.home");
    }

    @Test
    void category_admin_flow_calls_loginAsUser_when_key_matches(@TempDir Path tmp) throws Exception {
        System.setProperty("user.home", tmp.toString());

        DataRepository repo = new DataRepository();
        Category cat = new Category("MyCat");
        repo.addCategory(cat);

        AuthService auth = mock(AuthService.class);
        BookingService bookingService = mock(BookingService.class);

        final LoginFrame[] frameRef = new LoginFrame[1];
        runOnEdt(() -> frameRef[0] = new LoginFrame(auth, bookingService, repo));
        LoginFrame frame = frameRef[0];

        JCheckBox catCheck = findCheckBox(frame.getContentPane(), "Login for Category Admin");
        assertNotNull(catCheck);

        JComboBox<Category> categoryBox = findCategoryBox(frame.getContentPane());
        assertNotNull(categoryBox);

        JLabel categoryKeyLabel = findLabel(frame.getContentPane(), "Category Admin Key:");
        assertNotNull(categoryKeyLabel);
        Component catKeyComp = findSiblingField(categoryKeyLabel);
        assertNotNull(catKeyComp);
        assertTrue(catKeyComp instanceof JPasswordField);
        JPasswordField catKeyField = (JPasswordField) catKeyComp;

        JButton loginBtn = findButton(frame.getContentPane(), "Log In");
        assertNotNull(loginBtn);

        String expectedKey = BookingRequestService.categoryAdminKey(cat);
        String adminUsername = BookingRequestService.categoryAdminUsername(cat);

        when(auth.loginAsUser(adminUsername)).thenReturn(true);

        runOnEdt(() -> {
            catCheck.setSelected(true);
            categoryBox.setSelectedItem(cat);
            catKeyField.setText(expectedKey);
            loginBtn.doClick();
        });

        verify(auth, atLeastOnce()).loginAsUser(eq(adminUsername));

        assertTrue(repoFileExists(tmp), "Repo saved for category-admin path");
    }

    @Test
    void normal_login_as_admin_username_password_path_saves_and_calls_login(@TempDir Path tmp) throws Exception {
        System.setProperty("user.home", tmp.toString());

        DataRepository repo = new DataRepository();
        Administrator admin = new Administrator("admin", "pw");
        repo.addUser(admin);

        AuthService auth = mock(AuthService.class);
        BookingService bookingService = mock(BookingService.class);

        when(auth.login("admin", "pw")).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(admin); 

        final LoginFrame[] frameRef = new LoginFrame[1];
        runOnEdt(() -> frameRef[0] = new LoginFrame(auth, bookingService, repo));
        LoginFrame frame = frameRef[0];

        JLabel userLabel = findLabel(frame.getContentPane(), "Username:");
        assertNotNull(userLabel);
        Component userComp = findSiblingField(userLabel);
        assertNotNull(userComp);
        assertTrue(userComp instanceof JTextField);
        JTextField usernameField = (JTextField) userComp;

        JLabel passLabel = findLabel(frame.getContentPane(), "Password:");
        assertNotNull(passLabel);
        Component passComp = findSiblingField(passLabel);
        assertNotNull(passComp);
        assertTrue(passComp instanceof JPasswordField);
        JPasswordField passwordField = (JPasswordField) passComp;

        JButton loginBtn = findButton(frame.getContentPane(), "Log In");
        assertNotNull(loginBtn);

        runOnEdt(() -> {
            usernameField.setText("admin");
            passwordField.setText("pw");
            loginBtn.doClick();
        });

        verify(auth, atLeastOnce()).login("admin", "pw");

        assertTrue(repoFileExists(tmp));
    }
}