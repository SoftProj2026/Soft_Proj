package Test;

import Service.AuthService;
import Service.BookingService;
import domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.MainDashboardFrame;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Swing tests for {@link MainDashboardFrame}.
 *
 * What we test (stable, no dialogs needed):
 * - Category buttons are created for repo categories
 * - Bottom buttons exist: Contact Companies / My Bookings / Logout
 *
 * Notes:
 * - Requires non-headless environment (normal Eclipse run).
 * - We do NOT click buttons that open dialogs/frames (to avoid additional dependencies and popups).
 */
class MainDashboardFrameTest {

    private DataRepository repo;
    private AuthService auth;
    private BookingService booking;

    @BeforeEach
    void setUp() {
        assertFalse(GraphicsEnvironment.isHeadless(),
                "Swing tests require a non-headless environment. Remove -Djava.awt.headless=true.");

        repo = new DataRepository();

        // Add some categories
        repo.addCategory(new Category("CatA"));
        repo.addCategory(new Category("CatB"));
        repo.addCategory(new Category("CatC"));

        auth = new AuthService(repo);
        booking = new BookingService(repo);
    }

    private MainDashboardFrame createFrameOnEdt() throws Exception {
        AtomicReference<MainDashboardFrame> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new MainDashboardFrame(auth, booking, repo)));
        return ref.get();
    }

    private static List<JButton> findAllButtons(Container root) {
        ArrayList<JButton> out = new ArrayList<>();
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
        return findAllButtons(root).stream().anyMatch(b -> text.equals(b.getText()));
    }

    private static long countButtonsWithTextIn(Container root, List<String> texts) {
        return findAllButtons(root).stream().filter(b -> texts.contains(b.getText())).count();
    }

    @Test
    void rendersCategoryButtons_forAllCategories() throws Exception {
        MainDashboardFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            List<String> catNames = List.of("CatA", "CatB", "CatC");

            // Ensure each category name exists as a button
            for (String name : catNames) {
                assertTrue(hasButton(frame.getContentPane(), name),
                        "Expected category button for: " + name);
            }

            // And total category buttons count == categories count
            long count = countButtonsWithTextIn(frame.getContentPane(), catNames);
            assertEquals(catNames.size(), count,
                    "Expected exactly one button per category");

            frame.dispose();
        });
    }

    @Test
    void bottomButtons_exist() throws Exception {
        MainDashboardFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasButton(frame.getContentPane(), "Contact Companies"));
            assertTrue(hasButton(frame.getContentPane(), "My Bookings"));
            assertTrue(hasButton(frame.getContentPane(), "Logout"));
            frame.dispose();
        });
    }
}