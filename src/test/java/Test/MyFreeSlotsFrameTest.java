package Test;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.MyFreeSlotsFrame;
import service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;

public class MyFreeSlotsFrameTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private DataRepository repo;
    private AuthService auth;
    private User user;
    private Category cat;

    private static void runOnEdtAndWait(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        final Throwable[] err = new Throwable[1];
        SwingUtilities.invokeAndWait(() -> {
            try {
                r.run();
            } catch (Throwable t) {
                err[0] = t;
            }
        });
        if (err[0] != null) throw new RuntimeException(err[0]);
    }

    @BeforeEach
    void setUp() {
        repo = mock(DataRepository.class);
        auth = mock(AuthService.class);

        user = new User("First", "Last", "john", "pw", java.time.LocalDate.of(1990,1,1), "john@ex.com");
        cat = new Category("Consultation");

        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(user);
    }

    @AfterEach
    void tearDown() {
        for (Window w : Window.getWindows()) {
            if (w != null) w.dispose();
        }
    }

    private JPanel findListPanel(MyFreeSlotsFrame frame) {
        for (Component c : frame.getContentPane().getComponents()) {
            if (c instanceof JScrollPane) {
                JViewport vp = ((JScrollPane) c).getViewport();
                Component view = vp.getView();
                if (view instanceof JPanel) return (JPanel) view;
            }
            if (c instanceof Container) {
                Container cont = (Container) c;
                for (Component cc : cont.getComponents()) {
                    if (cc instanceof JScrollPane) {
                        JViewport vp = ((JScrollPane) cc).getViewport();
                        Component view = vp.getView();
                        if (view instanceof JPanel) return (JPanel) view;
                    }
                }
            }
        }
        return null;
    }

    @Test
    void shows_login_message_when_not_logged_in() throws Exception {
        when(auth.isLoggedIn()).thenReturn(false);
        when(repo.getSlots()).thenReturn(List.of());

        final MyFreeSlotsFrame[] ref = new MyFreeSlotsFrame[1];
        runOnEdtAndWait(() -> ref[0] = new MyFreeSlotsFrame(auth, repo, cat));
        MyFreeSlotsFrame frame = ref[0];

        JPanel listPanel = findListPanel(frame);
        assertNotNull(listPanel, "listPanel must exist");

        boolean foundMsg = false;
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JLabel) {
                String txt = ((JLabel) c).getText();
                if (txt != null && txt.toLowerCase().contains("you must login first")) {
                    foundMsg = true;
                    break;
                }
            }
        }
        assertTrue(foundMsg, "Login message should be shown when not logged in");

        runOnEdtAndWait(frame::dispose);
    }

    @Test
    void displays_only_free_slots_excluding_confirmed_appointments() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        TimeSlot freeSlot = new TimeSlot(now.plusDays(1), 60, cat);
        TimeSlot busySlot = new TimeSlot(now.plusDays(2), 60, cat);

        when(repo.getSlots()).thenReturn(List.of(freeSlot, busySlot));

        Appointment ap = new Appointment(user, busySlot, 60, 1);
        ap.confirm();
        when(repo.getAppointments()).thenReturn(List.of(ap));

        final MyFreeSlotsFrame[] ref = new MyFreeSlotsFrame[1];
        runOnEdtAndWait(() -> ref[0] = new MyFreeSlotsFrame(auth, repo, cat));
        MyFreeSlotsFrame frame = ref[0];

        JPanel listPanel = findListPanel(frame);
        assertNotNull(listPanel);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String freeText = freeSlot.getStartDateTime().format(fmt);
        String busyText = busySlot.getStartDateTime().format(fmt);

        boolean foundFree = false;
        boolean foundBusy = false;
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JLabel) {
                String txt = ((JLabel) c).getText();
                if (txt != null) {
                    if (txt.equals(freeText)) foundFree = true;
                    if (txt.equals(busyText)) foundBusy = true;
                }
            }
        }

        assertTrue(foundFree, "Free slot should be displayed");
        assertFalse(foundBusy, "Busy slot should NOT be displayed");

        runOnEdtAndWait(frame::dispose);
    }

    @Test
    void shows_no_free_slots_message_when_all_busy() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        TimeSlot s1 = new TimeSlot(now.plusDays(1), 60, cat);
        TimeSlot s2 = new TimeSlot(now.plusDays(2), 60, cat);
        when(repo.getSlots()).thenReturn(List.of(s1, s2));

        Appointment a1 = new Appointment(user, s1, 60, 1);
        a1.confirm();
        Appointment a2 = new Appointment(user, s2, 60, 1);
        a2.confirm();
        when(repo.getAppointments()).thenReturn(List.of(a1, a2));

        final MyFreeSlotsFrame[] ref = new MyFreeSlotsFrame[1];
        runOnEdtAndWait(() -> ref[0] = new MyFreeSlotsFrame(auth, repo, cat));
        MyFreeSlotsFrame frame = ref[0];

        JPanel listPanel = findListPanel(frame);
        assertNotNull(listPanel);

        boolean foundNone = false;
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JLabel) {
                String txt = ((JLabel) c).getText();
                if (txt != null && txt.toLowerCase().contains("no free slots")) {
                    foundNone = true;
                    break;
                }
            }
        }
        assertTrue(foundNone, "Should show 'No free slots' message when none available");

        runOnEdtAndWait(frame::dispose);
    }

    @Test
    void category_matching_is_case_insensitive() throws Exception {
        Category mixedCase = new Category("CONSULTATION");
        LocalDateTime now = LocalDateTime.now();
        TimeSlot sUpper = new TimeSlot(now.plusDays(1), 60, mixedCase);

        when(repo.getSlots()).thenReturn(List.of(sUpper));
        when(repo.getAppointments()).thenReturn(List.of());

        final MyFreeSlotsFrame[] ref = new MyFreeSlotsFrame[1];
        runOnEdtAndWait(() -> ref[0] = new MyFreeSlotsFrame(auth, repo, new Category("consultation")));
        MyFreeSlotsFrame frame = ref[0];

        JPanel listPanel = findListPanel(frame);
        assertNotNull(listPanel);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String expected = sUpper.getStartDateTime().format(fmt);

        boolean found = false;
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JLabel) {
                String txt = ((JLabel) c).getText();
                if (expected.equals(txt)) {
                    found = true;
                    break;
                }
            }
        }
        assertTrue(found, "Slot from category with different case should be displayed");

        runOnEdtAndWait(frame::dispose);
    }

    @Test
    void ignores_non_confirmed_appointments_when_checking_busy() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        TimeSlot s = new TimeSlot(now.plusDays(1), 60, cat);
        when(repo.getSlots()).thenReturn(List.of(s));

        Appointment ap = new Appointment(user, s, 60, 1);
        when(repo.getAppointments()).thenReturn(List.of(ap));

        final MyFreeSlotsFrame[] ref = new MyFreeSlotsFrame[1];
        runOnEdtAndWait(() -> ref[0] = new MyFreeSlotsFrame(auth, repo, cat));
        MyFreeSlotsFrame frame = ref[0];

        JPanel listPanel = findListPanel(frame);
        assertNotNull(listPanel);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String expected = s.getStartDateTime().format(fmt);

        boolean found = false;
        for (Component c : listPanel.getComponents()) {
            if (c instanceof JLabel && expected.equals(((JLabel) c).getText())) { found = true; break; }
        }
        assertTrue(found, "Slot should be shown because appointment is not CONFIRMED");

        runOnEdtAndWait(frame::dispose);
    }
}