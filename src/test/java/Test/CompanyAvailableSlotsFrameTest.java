package Test;

import domain.Category;
import domain.TimeSlot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.CompanyAvailableSlotsFrame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
class CompanyAvailableSlotsFrameTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private DataRepository repo;
    private Category catA;
    private Category catB;

    @BeforeEach
    void setUp() {
        assertFalse(GraphicsEnvironment.isHeadless(),
                "Swing tests require a non-headless environment. Remove -Djava.awt.headless=true.");

        repo = new DataRepository();
        catA = new Category("CatA");
        catB = new Category("CatB");
        repo.addCategory(catA);
        repo.addCategory(catB);
    }

    private CompanyAvailableSlotsFrame createFrameOnEdt(Category category) throws Exception {
        AtomicReference<CompanyAvailableSlotsFrame> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new CompanyAvailableSlotsFrame(repo, category)));
        return ref.get();
    }

    private static List<JLabel> findAllLabels(Container root) {
        ArrayList<JLabel> out = new ArrayList<>();
        ArrayDeque<Container> q = new ArrayDeque<>();
        q.add(root);

        while (!q.isEmpty()) {
            Container cur = q.removeFirst();
            for (Component c : cur.getComponents()) {
                if (c instanceof JLabel l) out.add(l);
                if (c instanceof Container cc) q.add(cc);
            }
        }
        return out;
    }

    private static boolean anyLabelContains(Container root, String text) {
        for (JLabel l : findAllLabels(root)) {
            String t = l.getText();
            if (t != null && t.contains(text)) return true;
        }
        return false;
    }

    private static long countRenderedSlotRows(Container root) {
        return findAllLabels(root).stream()
                .map(JLabel::getText)
                .filter(t -> t != null && t.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}.*$"))
                .count();
    }

    @Test
    void whenNoSlotsForCategory_showsNoneMessage() throws Exception {
        TimeSlot b1 = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), 30, catB);
        b1.setAvailable(true);
        repo.addSlot(b1);

        TimeSlot b2 = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(11).withMinute(0), 30, catB);
        b2.setAvailable(true);
        repo.addSlot(b2);

        CompanyAvailableSlotsFrame frame = createFrameOnEdt(catA);

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(anyLabelContains(frame.getContentPane(), "No available company slots for this category."),
                    "Should show 'No available company slots for this category.' when category has no slots");
            assertEquals(0, countRenderedSlotRows(frame.getContentPane()),
                    "Should not render slot rows for CatA");
            frame.dispose();
        });
    }

    @Test
    void showsOnlyAvailableSlots_forSelectedCategory() throws Exception {
        TimeSlot a1 = new TimeSlot(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0), 30, catA);
        a1.setAvailable(true);
        repo.addSlot(a1);

        TimeSlot a2 = new TimeSlot(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0), 30, catA);
        a2.setAvailable(true);
        repo.addSlot(a2);

        TimeSlot b1 = new TimeSlot(LocalDateTime.now().plusDays(3).withHour(12).withMinute(0), 30, catB);
        b1.setAvailable(true);
        repo.addSlot(b1);

        CompanyAvailableSlotsFrame frame = createFrameOnEdt(catA);

        SwingUtilities.invokeAndWait(() -> {
            assertFalse(anyLabelContains(frame.getContentPane(), "No available company slots for this category."),
                    "Should not show empty message when CatA has slots");
            assertEquals(2, countRenderedSlotRows(frame.getContentPane()),
                    "Should render exactly 2 slot rows for CatA");
            frame.dispose();
        });
    }
}