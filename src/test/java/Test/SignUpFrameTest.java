package Test;

import presentation.SignUpFrame;
import service.AuthService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for presentation.SignUpFrame
 *
 * Tests access private UI fields/methods via reflection and runs interactions on the EDT.
 */
class SignUpFrameTest {

    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }

    private static interface RunnableWithException {
        void run() throws Exception;
    }

    private AuthService authMock;
    private SignUpFrame frame;

    private static void runOnEDTUnchecked(RunnableWithException r) throws Exception {
        Throwable[] err = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err[0] = t;
            }
        });
        if (err[0] != null) {
            if (err[0] instanceof Exception) throw (Exception) err[0];
            else throw new RuntimeException(err[0]);
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        authMock = mock(AuthService.class);
        runOnEDTUnchecked(() -> frame = new SignUpFrame(authMock));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frame != null) {
            runOnEDTUnchecked(() -> frame.dispose());
        }
    }

    private <T> T getField(String name, Class<T> type) throws Exception {
        Field f = SignUpFrame.class.getDeclaredField(name);
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        T v = (T) f.get(frame);
        return v;
    }

    private Object invokePrivate(String name, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method m = SignUpFrame.class.getDeclaredMethod(name, paramTypes);
        m.setAccessible(true);
        return m.invoke(frame, args);
    }

    @Test
    void placeholders_are_installed_on_construction() throws Exception {
        JTextField first = getField("firstNameF", JTextField.class);
        JTextField last = getField("lastNameF", JTextField.class);
        JTextField user = getField("userF", JTextField.class);
        JTextField email = getField("emailF", JTextField.class);
        JPasswordField pass = getField("passF", JPasswordField.class);

        assertEquals("First Name", first.getText());
        assertEquals("Last Name", last.getText());
        assertEquals("Username", user.getText());
        assertEquals("Email", email.getText());
        assertEquals("Password", new String(pass.getPassword()));
    }

    @Test
    void isStrongPassword_checks_rules_correctly() throws Exception {
        assertTrue((Boolean) invokePrivate("isStrongPassword", new Class[]{String.class}, new Object[]{"Abcde1!2"}));
        assertFalse((Boolean) invokePrivate("isStrongPassword", new Class[]{String.class}, new Object[]{"weak"}));
        assertFalse((Boolean) invokePrivate("isStrongPassword", new Class[]{String.class}, new Object[]{null}));
        assertFalse((Boolean) invokePrivate("isStrongPassword", new Class[]{String.class}, new Object[]{"NoDigits!"}));
    }

    @Test
    void isValidEmail_accepts_and_rejects_correctly() throws Exception {
        assertTrue((Boolean) invokePrivate("isValidEmail", new Class[]{String.class}, new Object[]{"a.b@example.com"}));
        assertFalse((Boolean) invokePrivate("isValidEmail", new Class[]{String.class}, new Object[]{"notanemail"}));
        assertFalse((Boolean) invokePrivate("isValidEmail", new Class[]{String.class}, new Object[]{""}));
        assertFalse((Boolean) invokePrivate("isValidEmail", new Class[]{String.class}, new Object[]{null}));
    }

    @Test
    void readDobOrNull_returns_null_when_incomplete_and_valid_date_when_complete() throws Exception {
        @SuppressWarnings("unchecked")
        JComboBox<Integer> dayBox = getField("dayBox", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> monthBox = getField("monthBox", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> yearBox = getField("yearBox", JComboBox.class);

        Object nullDob = invokePrivate("readDobOrNull", new Class[]{}, new Object[]{});
        assertNull(nullDob);

        runOnEDTUnchecked(() -> {
            dayBox.setSelectedItem(2);
            monthBox.setSelectedItem(3);
            yearBox.setSelectedItem(LocalDate.now().getYear() - 25);
        });

        Object dob = invokePrivate("readDobOrNull", new Class[]{}, new Object[]{});
        assertNotNull(dob);
        assertTrue(dob instanceof LocalDate);
        LocalDate ld = (LocalDate) dob;
        assertEquals(LocalDate.of(LocalDate.now().getYear() - 25, 3, 2), ld);
    }

    @Test
    void onSignUp_calls_register_on_valid_input_and_skips_on_invalid_input() throws Exception {
        JTextField first = getField("firstNameF", JTextField.class);
        JTextField last = getField("lastNameF", JTextField.class);
        JTextField user = getField("userF", JTextField.class);
        JTextField email = getField("emailF", JTextField.class);
        JPasswordField pass = getField("passF", JPasswordField.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> dayBox = getField("dayBox", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> monthBox = getField("monthBox", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> yearBox = getField("yearBox", JComboBox.class);

        when(authMock.register(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), anyString()))
                .thenReturn(AuthService.RegisterResult.SUCCESS);

        runOnEDTUnchecked(() -> {
            first.setText("John");
            last.setText("Doe");
            user.setText("jdoe");
            email.setText("jdoe@example.com");
            pass.setText("Abcde1!2");
            dayBox.setSelectedItem(5);
            monthBox.setSelectedItem(6);
            yearBox.setSelectedItem(LocalDate.now().getYear() - 30);
        });

        try {
            runOnEDTUnchecked(() -> {
                try {
                    Method m = SignUpFrame.class.getDeclaredMethod("onSignUp", AuthService.class);
                    m.setAccessible(true);
                    m.invoke(frame, authMock);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    throw new RuntimeException(cause);
                }
            });
        } catch (RuntimeException re) {
            if (!(re.getCause() instanceof HeadlessException) && !(re.getCause() instanceof RuntimeException && re.getCause().getCause() instanceof HeadlessException)) {
                throw re;
            }
        }

        ArgumentCaptor<String> firstCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> lastCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> userCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> passCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<LocalDate> dobCap = ArgumentCaptor.forClass(LocalDate.class);
        ArgumentCaptor<String> emailCap = ArgumentCaptor.forClass(String.class);

        verify(authMock, atLeastOnce()).register(firstCap.capture(), lastCap.capture(),
                userCap.capture(), passCap.capture(), dobCap.capture(), emailCap.capture());

        assertEquals("John", firstCap.getValue());
        assertEquals("Doe", lastCap.getValue());
        assertEquals("jdoe", userCap.getValue());
        assertEquals("jdoe@example.com", emailCap.getValue());
        assertEquals("Abcde1!2", passCap.getValue());
        assertNotNull(dobCap.getValue());

        reset(authMock);
        when(authMock.register(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), anyString()))
                .thenReturn(AuthService.RegisterResult.SUCCESS);

        runOnEDTUnchecked(() -> {
            first.setText("");
            last.setText("X");
            user.setText("ux");
            email.setText("a@b.com");
            pass.setText("Abcde1!2");
            dayBox.setSelectedItem(1);
            monthBox.setSelectedItem(1);
            yearBox.setSelectedItem(LocalDate.now().getYear() - 20);
        });

        try {
            runOnEDTUnchecked(() -> {
                try {
                    Method m = SignUpFrame.class.getDeclaredMethod("onSignUp", AuthService.class);
                    m.setAccessible(true);
                    m.invoke(frame, authMock);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    throw new RuntimeException(cause);
                }
            });
        } catch (RuntimeException re) {
        }

        verify(authMock, never()).register(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), anyString());
    }

    @Test
    void weak_password_prevents_register() throws Exception {
        JTextField first = getField("firstNameF", JTextField.class);
        JTextField last = getField("lastNameF", JTextField.class);
        JTextField user = getField("userF", JTextField.class);
        JTextField email = getField("emailF", JTextField.class);
        JPasswordField pass = getField("passF", JPasswordField.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> dayBox = getField("dayBox", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> monthBox = getField("monthBox", JComboBox.class);
        @SuppressWarnings("unchecked")
        JComboBox<Integer> yearBox = getField("yearBox", JComboBox.class);

        runOnEDTUnchecked(() -> {
            first.setText("A");
            last.setText("B");
            user.setText("ux");
            email.setText("a@b.com");
            pass.setText("weak"); 
            dayBox.setSelectedItem(2);
            monthBox.setSelectedItem(2);
            yearBox.setSelectedItem(LocalDate.now().getYear() - 30);
        });

        try {
            runOnEDTUnchecked(() -> {
                try {
                    Method m = SignUpFrame.class.getDeclaredMethod("onSignUp", AuthService.class);
                    m.setAccessible(true);
                    m.invoke(frame, authMock);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    throw new RuntimeException(cause);
                }
            });
        } catch (RuntimeException re) {
        }

        verify(authMock, never()).register(anyString(), anyString(), anyString(), anyString(), any(LocalDate.class), anyString());
    }
}