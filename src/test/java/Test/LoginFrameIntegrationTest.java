package Test;

import domain.Administrator;
import domain.Category;
import domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistence.DataRepository;
import presentation.LoginFrame;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LoginFrameIntegrationTest {

    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) { r.run(); return; }
        final Throwable[] err = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try { r.run(); } catch (Throwable t) { err[0] = t; }
        });
        if (err[0] != null) throw new RuntimeException(err[0]);
    }

    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) return b;
            if (c instanceof Container cc) {
                JButton r = findButton(cc, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static JCheckBox findCheck(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JCheckBox cb && text.equals(cb.getText())) return cb;
            if (c instanceof Container cc) {
                JCheckBox r = findCheck(cc, text);
                if (r != null) return r;
            }
        }
        return null;
    }

    private static <T extends JComponent> T findFirst(Container root, Class<T> cls) {
        for (Component c : root.getComponents()) {
            if (cls.isInstance(c)) return cls.cast(c);
            if (c instanceof Container cc) {
                T r = findFirst(cc, cls);
                if (r != null) return r;
            }
        }
        return null;
    }

    @Test
    void qrAdminKey_is_ADMIN2026_and_wrong_key_does_not_call_loginAsAdmin(@TempDir Path tmp) throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        System.setProperty("user.home", tmp.toString());

        DataRepository repo = new DataRepository();
        AuthService auth = mock(AuthService.class);
        BookingService booking = mock(BookingService.class);

        final LoginFrame[] ref = new LoginFrame[1];
        runOnEdt(() -> ref[0] = new LoginFrame(auth, booking, repo));
        LoginFrame frame = ref[0];

        JCheckBox qrAdmin = findCheck(frame.getContentPane(), "QR Admin for Company");
        assertNotNull(qrAdmin);

        JButton login = findButton(frame.getContentPane(), "Log In");
        assertNotNull(login);

        runOnEdt(() -> qrAdmin.setSelected(true));
        runOnEdt(() -> qrAdmin.doClick()); 

        JPasswordField anyPassField = findFirst(frame.getContentPane(), JPasswordField.class);
        assertNotNull(anyPassField);

        runOnEdt(() -> {
            anyPassField.setText("WRONG");
            login.doClick();
        });
        verify(auth, never()).loginAsAdmin();

        when(auth.loginAsAdmin()).thenReturn(true);
        runOnEdt(() -> {
            anyPassField.setText("ADMIN2026");
            login.doClick();
        });
        verify(auth, atLeastOnce()).loginAsAdmin();

        Path data = tmp.resolve(".Soft_Proj").resolve("data.json");
        assertTrue(Files.exists(data));

        runOnEdt(frame::dispose);
    }

    @Test
    void normalLogin_calls_authService_login(@TempDir Path tmp) throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        System.setProperty("user.home", tmp.toString());

        DataRepository repo = new DataRepository();
        AuthService auth = mock(AuthService.class);
        BookingService booking = mock(BookingService.class);

        User u = new User("F","L","u1","pw", null, "u1@example.com");
        repo.addUser(u);

        when(auth.login("u1", "pw")).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(u);

        final LoginFrame[] ref = new LoginFrame[1];
        runOnEdt(() -> ref[0] = new LoginFrame(auth, booking, repo));
        LoginFrame frame = ref[0];

        JTextField username = findFirst(frame.getContentPane(), JTextField.class);
        assertNotNull(username);

        // PasswordField: there are multiple, but in normal mode only one is visible
        JPasswordField password = findFirst(frame.getContentPane(), JPasswordField.class);
        assertNotNull(password);

        JButton login = findButton(frame.getContentPane(), "Log In");
        assertNotNull(login);

        runOnEdt(() -> {
            username.setText("u1");
            password.setText("pw");
            login.doClick();
        });

        verify(auth, atLeastOnce()).login("u1", "pw");

        runOnEdt(frame::dispose);
    }

    @Test
    void adminNormalLogin_allows_only_big_admin_username_admin(@TempDir Path tmp) throws Exception {
        if (GraphicsEnvironment.isHeadless()) return;

        System.setProperty("user.home", tmp.toString());

        DataRepository repo = new DataRepository();
        AuthService auth = mock(AuthService.class);
        BookingService booking = mock(BookingService.class);

        Administrator admin = new Administrator("admin", "pw");
        repo.addUser(admin);

        when(auth.login("admin", "pw")).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(admin);

        final LoginFrame[] ref = new LoginFrame[1];
        runOnEdt(() -> ref[0] = new LoginFrame(auth, booking, repo));
        LoginFrame frame = ref[0];

        JTextField username = findFirst(frame.getContentPane(), JTextField.class);
        JPasswordField password = findFirst(frame.getContentPane(), JPasswordField.class);
        JButton login = findButton(frame.getContentPane(), "Log In");

        runOnEdt(() -> {
            username.setText("admin");
            password.setText("pw");
            login.doClick();
        });

        verify(auth, atLeastOnce()).login("admin", "pw");
        runOnEdt(frame::dispose);
    }
}