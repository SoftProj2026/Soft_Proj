package Test;

import Service.AuthService;
import domain.User;
import org.junit.jupiter.api.*;
import persistence.DataRepository;
import presentation.UserProfileFrame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

/**
 * Swing tests for {@link UserProfileFrame}.
 *
 * <p>Tests are automatically skipped in headless environments.</p>
 */
class UserProfileFrameTest {

    private DataRepository repo;
    private AuthService auth;
    private User user;
    private UserProfileFrame frame;

    @BeforeEach
    void setUp() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Skipping Swing UI tests in headless environment.");

        repo = new DataRepository();
        user = new User("Alice", "Smith", "asmith", "Pass1@word", LocalDate.of(1995, 6, 15), "alice@example.com");
        repo.addUser(user);

        auth = new AuthService(repo);
        assertTrue(auth.login("asmith", "Pass1@word"));

        frame = createFrameOnEdt();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private UserProfileFrame createFrameOnEdt() throws Exception {
        AtomicReference<UserProfileFrame> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new UserProfileFrame(auth, repo)));
        return ref.get();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static List<JButton> allButtons(Container root) {
        List<JButton> out = new ArrayList<>();
        ArrayDeque<Container> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Container cur = q.removeFirst();
            for (Component c : cur.getComponents()) {
                if (c instanceof JButton b) out.add(b);
                if (c instanceof Container cc) q.add(cc);
            }
        }
        return out;
    }

    private static boolean hasButton(Container root, String text) {
        return allButtons(root).stream().anyMatch(b -> text.equals(b.getText()));
    }

    private static List<JTextField> allTextFields(Container root) {
        List<JTextField> out = new ArrayList<>();
        ArrayDeque<Container> q = new ArrayDeque<>();
        q.add(root);
        while (!q.isEmpty()) {
            Container cur = q.removeFirst();
            for (Component c : cur.getComponents()) {
                if (c instanceof JTextField tf) out.add(tf);
                if (c instanceof Container cc) q.add(cc);
            }
        }
        return out;
    }

    // -----------------------------------------------------------------------
    // Tests
    // -----------------------------------------------------------------------

    @Test
    void frame_hasSaveAndCloseButtons() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasButton(frame.getContentPane(), "Save Changes"), "Expected 'Save Changes' button");
            assertTrue(hasButton(frame.getContentPane(), "Close"), "Expected 'Close' button");
        });
    }

    @Test
    void frame_titleIsMyProfile() throws Exception {
        SwingUtilities.invokeAndWait(() ->
                assertEquals("My Profile", frame.getTitle())
        );
    }

    @Test
    void frame_showsUsernameInReadOnlyField() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<JTextField> fields = allTextFields(frame.getContentPane());
            boolean found = fields.stream()
                    .anyMatch(f -> !f.isEditable() && "asmith".equals(f.getText()));
            assertTrue(found, "Username read-only field not found");
        });
    }

    @Test
    void frame_showsEmailInEditableField() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            List<JTextField> fields = allTextFields(frame.getContentPane());
            boolean found = fields.stream()
                    .anyMatch(f -> f.isEditable() && "alice@example.com".equals(f.getText()));
            assertTrue(found, "Editable email field with correct value not found");
        });
    }

    @Test
    void saveChanges_updatesUserEmail() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            // Find the editable email field and update it
            List<JTextField> fields = allTextFields(frame.getContentPane());
            JTextField emailField = fields.stream()
                    .filter(JTextField::isEditable)
                    .findFirst()
                    .orElse(null);
            assertNotNull(emailField, "Editable email field not found");
            emailField.setText("newemail@example.com");

            // Click Save Changes
            JButton saveBtn = allButtons(frame.getContentPane()).stream()
                    .filter(b -> "Save Changes".equals(b.getText()))
                    .findFirst()
                    .orElse(null);
            assertNotNull(saveBtn, "Save Changes button not found");

            // Suppress the success dialog for this test by checking state after doClick
            // We dismiss dialogs by checking the user email was updated
            // Use the action listener path — wrap in try-catch for headless modal dialogs
            try {
                saveBtn.doClick();
            } catch (Exception ignored) { }
        });

        // Give EDT time to process
        SwingUtilities.invokeAndWait(() -> {
            assertEquals("newemail@example.com", user.getEmail(),
                    "User email should have been updated after saving");
        });
    }
}
