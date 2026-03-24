/*package Test;

import Service.AuthService;
import Service.BookingService;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.BookingTypeChoiceDialog;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


public class BookingTypeChoiceDialogTest {

    private DataRepository repo;
    private AuthService auth;
    private BookingService booking;
    private Category cat;
    private User user;

    @BeforeEach
    void setUp() {
        repo = new DataRepository();
        auth = mock(AuthService.class);
        booking = mock(BookingService.class);

        user = new User("T", "U", "tester", "pw", LocalDate.of(1990, 1, 1), "t@example.com");
        repo.addUser(user);
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(user);

        cat = new Category("Health");
        repo.addCategory(cat);
        repo.addSlot(new TimeSlot(LocalDateTime.now().plusHours(2), 30, cat));
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) {
                w.dispose();
            }
        }
    }

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
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
    }

    @Test
    void constructor_sets_ui_properties() throws Exception {
        final BookingTypeChoiceDialog[] dlgRef = new BookingTypeChoiceDialog[1];

        runOnEdtAndWait(() -> dlgRef[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
        BookingTypeChoiceDialog dlg = dlgRef[0];

        assertNotNull(dlg);
        assertEquals("Choose Booking Type", dlg.getTitle());
        assertTrue(dlg.isModal());
        assertFalse(dlg.isResizable());
        assertEquals(Color.WHITE, dlg.getContentPane().getBackground());
        assertTrue(dlg.getContentPane().getComponentCount() >= 1);

        runOnEdtAndWait(dlg::dispose);
    }

    @Test
    void emergencyDialog_builds_components_and_can_be_cancelled() throws Exception {
        final BookingTypeChoiceDialog[] dlgRef = new BookingTypeChoiceDialog[1];

        runOnEdtAndWait(() -> dlgRef[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
        BookingTypeChoiceDialog dlg = dlgRef[0];

        Thread t = new Thread(() -> {
            try {
                Method m = BookingTypeChoiceDialog.class.getDeclaredMethod("openEmergencyQuickDialog");
                m.setAccessible(true);
                m.invoke(dlg);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }, "emg-thread");
        t.start();

        JDialog emerg = null;
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < 5000) {
            for (Window w : Window.getWindows()) {
                if (w instanceof JDialog && "Emergency - Preferred Time".equals(((JDialog) w).getTitle())) {
                    emerg = (JDialog) w;
                    if (emerg.isShowing()) break;
                }
            }
            if (emerg != null && emerg.isShowing()) break;
            Thread.sleep(50);
        }

        assertNotNull(emerg, "Emergency dialog must appear");
        assertTrue(emerg.isShowing());

        JComboBox<?> combo = null;
        JTextArea textArea = null;
        JButton sendBtn = null;
        JButton cancelBtn = null;

        Container root = (Container) emerg.getContentPane();
        java.util.LinkedList<Component> queue = new java.util.LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            Component c = queue.removeFirst();
            if (c instanceof JComboBox) combo = (JComboBox<?>) c;
            if (c instanceof JScrollPane) {
                Component v = ((JScrollPane) c).getViewport().getView();
                if (v instanceof JTextArea) textArea = (JTextArea) v;
            }
            if (c instanceof JButton) {
                String txt = ((JButton) c).getText();
                if ("Send Emergency Email".equals(txt)) sendBtn = (JButton) c;
                if ("Cancel".equals(txt)) cancelBtn = (JButton) c;
            }
            if (c instanceof Container) {
                for (Component cc : ((Container) c).getComponents()) queue.add(cc);
            }
        }

        assertNotNull(combo, "Combo box must exist");
        assertNotNull(textArea, "Notes text area must exist");
        assertNotNull(sendBtn, "Send button must exist");
        assertNotNull(cancelBtn, "Cancel button must exist");

        assertTrue(combo.getItemCount() >= 1);
        Object first = combo.getItemAt(0);
        assertNotNull(first);
        assertTrue(first.toString().toLowerCase().contains("no preferred") || first.toString().length() > 0);

        final JButton cancelToClick = cancelBtn;
        runOnEdtAndWait(() -> cancelToClick.doClick());

        t.join(1000);
        assertFalse(t.isAlive());
        assertFalse(emerg.isShowing(), "Emergency dialog should be closed after Cancel");

        runOnEdtAndWait(dlg::dispose);
    }

    @Test
    void disableParticipantSelectorsInContainer_works_on_spinner_and_combo() throws Exception {
        final BookingTypeChoiceDialog[] dlgRef = new BookingTypeChoiceDialog[1];
        runOnEdtAndWait(() -> dlgRef[0] = new BookingTypeChoiceDialog(null, cat, repo, auth, booking));
        BookingTypeChoiceDialog dlg = dlgRef[0];

        JPanel panel = new JPanel();
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(5, 1, 5, 1));
        panel.add(spinner);

        JComboBox<String> combo = new JComboBox<>(new String[]{"1","2","3","4","5"});
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
            assertFalse(spinner.isEnabled(), "Spinner should be disabled");
            assertEquals(1, ((Number) spinner.getValue()).intValue(), "Spinner value should be set to 1");

            assertFalse(combo.isEnabled(), "Combo should be disabled");
            Object selected = combo.getSelectedItem();
            assertTrue("1".equals(selected) || (selected != null && "1".equals(selected.toString())), "Combo selection should be 1");
        });

        runOnEdtAndWait(dlg::dispose);
    }
}*/