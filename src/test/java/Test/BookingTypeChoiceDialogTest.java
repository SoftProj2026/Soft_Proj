package Test;

import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import presentation.BookingTypeChoiceDialog;
import presentation.DialogUtil;
import presentation.UITheme;
import service.AuthService;
import service.BookingService;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
public class BookingTypeChoiceDialogTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private DataRepository repo;
    private AuthService auth;
    private BookingService booking;
    private Category cat;
    private User user;

    @BeforeEach
    void setUp() {
        UITheme.apply();

        repo = mock(DataRepository.class);
        auth = mock(AuthService.class);
        booking = mock(BookingService.class);

        user = new User("T", "U", "tester", "pw", java.time.LocalDate.of(1990,1,1), "t@example.com");
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(user);

        cat = new Category("Health");
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) w.dispose();
        }
    }

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        final Throwable[] ex = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                ex[0] = t;
            }
        });
        if (ex[0] != null) throw new RuntimeException(ex[0]);
    }

    private JDialog findDialogByTitle(String title) {
        for (Window w : Window.getWindows()) {
            if (w instanceof JDialog && title.equals(((JDialog) w).getTitle())) {
                return (JDialog) w;
            }
        }
        return null;
    }

    private JComponent findComponent(Container root, Class<?> cls, java.util.function.Predicate<Component> pred) {
        for (Component c : root.getComponents()) {
            if (cls.isInstance(c) && (pred == null || pred.test(c))) return (JComponent) c;
            if (c instanceof Container) {
                JComponent r = findComponent((Container) c, cls, pred);
                if (r != null) return r;
            }
        }
        return null;
    }
    @Test
    void constructor_sets_basic_properties_and_buttons_present() throws Exception {
        when(repo.getSlots()).thenReturn(List.of());
        final BookingTypeChoiceDialog[] ref = new BookingTypeChoiceDialog[1];

        runOnEdtAndWait(() -> ref[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
        BookingTypeChoiceDialog dlg = ref[0];

        assertNotNull(dlg);
        assertEquals("Choose Booking Type", dlg.getTitle());
        assertTrue(dlg.isModal());
        assertFalse(dlg.isResizable());
        assertEquals(Color.WHITE, dlg.getContentPane().getBackground());

        boolean foundEmergency = findComponent(dlg.getContentPane(), JButton.class, c -> "Emergency Booking".equals(((JButton) c).getText())) != null;
        boolean foundNew = findComponent(dlg.getContentPane(), JButton.class, c -> "New Booking".equals(((JButton) c).getText())) != null;
        boolean foundReview = findComponent(dlg.getContentPane(), JButton.class, c -> "Review Booking".equals(((JButton) c).getText())) != null;
        boolean foundIndividual = findComponent(dlg.getContentPane(), JButton.class, c -> "Individual Booking".equals(((JButton) c).getText())) != null;
        boolean foundGroup = findComponent(dlg.getContentPane(), JButton.class, c -> ((JButton) c).getText() != null && ((JButton) c).getText().startsWith("Group")) != null;

        assertTrue(foundEmergency, "Emergency button present");
        assertTrue(foundNew, "New Booking button present");
        assertTrue(foundReview, "Review Booking present");
        assertTrue(foundIndividual, "Individual Booking present");
        assertTrue(foundGroup, "Group Booking present");

        runOnEdtAndWait(dlg::dispose);
    }

    /*@Test
    void emergencyQuickDialog_builds_components_and_cancel_closes() throws Exception {
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 30, cat);
        s.setAvailable(true);
        when(repo.getSlots()).thenReturn(List.of(s));

        final BookingTypeChoiceDialog[] ref = new BookingTypeChoiceDialog[1];

        try (MockedStatic<DialogUtil> dmock = mockStatic(DialogUtil.class);
             MockedStatic<JOptionPane> jmock = mockStatic(JOptionPane.class)) {

            dmock.when(() -> DialogUtil.show(any(), anyString(), anyString(), any())).then(inv -> null);
            jmock.when(() -> JOptionPane.showMessageDialog(any(), anyString(), anyString(), anyInt())).then(inv -> null);

            runOnEdtAndWait(() -> ref[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
            BookingTypeChoiceDialog dlg = ref[0];

            JButton emergency = (JButton) findComponent(dlg.getContentPane(), JButton.class, c -> "Emergency Booking".equals(((JButton) c).getText()));
            assertNotNull(emergency, "Emergency button must exist");

            SwingUtilities.invokeLater(() -> emergency.doClick());

            JDialog emerg = null;
            long start = System.currentTimeMillis();
            while (System.currentTimeMillis() - start < 4000) {
                emerg = findDialogByTitle("Emergency - Preferred Time");
                if (emerg != null && emerg.isShowing()) break;
                Thread.sleep(50);
            }
            assertNotNull(emerg, "Emergency dialog should be shown");
            assertTrue(emerg.isShowing());

            JComboBox<?> combo = (JComboBox<?>) findComponent(emerg.getContentPane(), JComboBox.class, null);
            assertNotNull(combo, "combo in emergency dialog exists");
            assertTrue(combo.getItemCount() >= 1, "combo should contain at least the default item");

            JTextArea notes = (JTextArea) findComponent(emerg.getContentPane(), JTextArea.class, null);
            assertNotNull(notes, "notes text area exists");

            JButton cancel = (JButton) findComponent(emerg.getContentPane(), JButton.class, c -> "Cancel".equals(((JButton) c).getText()));
            assertNotNull(cancel, "Cancel button exists");

            runOnEdtAndWait(() -> cancel.doClick());

            assertFalse(emerg.isShowing(), "Emergency dialog should be closed after Cancel");

            runOnEdtAndWait(dlg::dispose);
        }
    }

    @Test
    void disableParticipantSelectorsInContainer_disables_and_sets_values() throws Exception {
        final BookingTypeChoiceDialog[] ref = new BookingTypeChoiceDialog[1];
        when(repo.getSlots()).thenReturn(List.of());
        runOnEdtAndWait(() -> ref[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
        BookingTypeChoiceDialog dlg = ref[0];

        JPanel panel = new JPanel();
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        panel.add(spinner);
        JComboBox<String> combo = new JComboBox<>(new String[] {"1", "2", "3", "4", "5"});
        combo.setSelectedIndex(4);
        panel.add(combo);

        runOnEdtAndWait(() -> {
            try {
                Method m = BookingTypeChoiceDialog.class.getDeclaredMethod("disableParticipantSelectorsInContainer", Container.class, int.class, int.class, int.class);
                m.setAccessible(true);
                m.invoke(dlg, panel, 1, 1, 5);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        });

        runOnEdtAndWait(() -> {
            assertFalse(spinner.isEnabled());
            assertEquals(1, ((Number) spinner.getValue()).intValue());
            assertFalse(combo.isEnabled());
            Object sel = combo.getSelectedItem();
            assertTrue("1".equals(sel) || (sel != null && "1".equals(sel.toString())));
            dlg.dispose();
        });
    }*/
}