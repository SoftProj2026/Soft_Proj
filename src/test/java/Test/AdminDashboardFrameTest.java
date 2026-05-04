package Test;

import domain.Administrator;
import domain.Provider;
import domain.User;
import persistence.DataRepository;
import persistence.RepoStorage;
import presentation.AdminDashboardFrame;
import service.AuthService;
import service.BookingService;

import org.junit.jupiter.api.*;

import javax.swing.*;
import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.mockito.MockedStatic;

class AdminDashboardFrameTest {

    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            Throwable[] err = new Throwable[1];
            SwingUtilities.invokeAndWait(() -> {
                try { r.run(); } catch (Throwable t) { err[0] = t; }
            });
            if (err[0] != null) throw new RuntimeException(err[0]);
        }
    }

    private JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton && text.equals(((JButton) c).getText())) return (JButton) c;
            if (c instanceof Container) {
                JButton b = findButton((Container) c, text);
                if (b != null) return b;
            }
        }
        return null;
    }

    private JLabel findStatsLabelByPrefix(Container root, String prefix) {
        for (Component c : root.getComponents()) {
            if (c instanceof JLabel) {
                String t = ((JLabel) c).getText();
                if (t != null && t.startsWith(prefix)) return (JLabel) c;
            }
            if (c instanceof Container) {
                JLabel l = findStatsLabelByPrefix((Container) c, prefix);
                if (l != null) return l;
            }
        }
        return null;
    }

    private static Window findWindowByTitle(String title) {
        for (Window w : Window.getWindows()) {
            if (!w.isDisplayable()) continue;
            if (w instanceof Frame) {
                if (title.equals(((Frame) w).getTitle())) return w;
            } else if (w instanceof Dialog) {
                if (title.equals(((Dialog) w).getTitle())) return w;
            }
        }
        return null;
    }

    AuthService auth;
    BookingService bookingSvc;
    DataRepository repo;

    @BeforeEach
    void setup() {
        auth = mock(AuthService.class);
        bookingSvc = mock(BookingService.class);
        repo = new DataRepository();

        repo.addUser(new User("F", "L", "user1", "pw", java.time.LocalDate.of(1995, 1, 1), "a@b.com"));

        repo.addProvider(new Provider(
                "provider1", "pw", "Provider One",
                "0799123456", "prov1@b.com", "Amman"
        ));

        var slot = new domain.TimeSlot(java.time.LocalDateTime.now(), 60, new domain.Category("cat1"));
        repo.addSlot(slot);
        repo.addAppointment(new domain.Appointment(
                repo.getUsers().get(0),
                repo.getSlots().get(0),
                60, 1
        ));
    }

    @AfterEach
    void tearDown() throws Exception {
        runOnEdt(() -> {
            for (Window w : Window.getWindows()) {
                if (w != null && w.isDisplayable()) w.dispose();
            }
        });
    }

    @Test
    void refreshCounts_labels_show_correct_counts() throws Exception {
        final AdminDashboardFrame[] ref = new AdminDashboardFrame[1];
        runOnEdt(() -> ref[0] = new AdminDashboardFrame(auth, bookingSvc, repo));
        AdminDashboardFrame frame = ref[0];

        runOnEdt(() -> {
            JLabel users = findStatsLabelByPrefix(frame.getContentPane(), "Users count:");
            assertNotNull(users);
            assertTrue(users.getText().endsWith("2"));

            JLabel providers = findStatsLabelByPrefix(frame.getContentPane(), "Providers count:");
            assertNotNull(providers);
            assertTrue(providers.getText().endsWith("1"));

            JLabel slots = findStatsLabelByPrefix(frame.getContentPane(), "Slots count:");
            assertNotNull(slots);
            assertTrue(slots.getText().endsWith("1"));

            JLabel appts = findStatsLabelByPrefix(frame.getContentPane(), "Appointments count:");
            assertNotNull(appts);
            assertTrue(appts.getText().endsWith("1"));
        });

        runOnEdt(frame::dispose);
    }

    @Test
    void requests_button_opens_requests_for_admin_only() throws Exception {
        Administrator adminUser = new Administrator("admin", "pw");
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(adminUser);

        final AdminDashboardFrame[] ref = new AdminDashboardFrame[1];
        runOnEdt(() -> ref[0] = new AdminDashboardFrame(auth, bookingSvc, repo));
        AdminDashboardFrame frame = ref[0];

        JButton requestsBtn = findButton(frame.getContentPane(), "Requests");
        assertNotNull(requestsBtn);

        runOnEdt(requestsBtn::doClick);

        runOnEdt(() -> {
            Window w = findWindowByTitle("Approval Requests");
            assertNotNull(w, "Expected Approval Requests window to open");
        });

        runOnEdt(frame::dispose);
    }

    @Test
    void logout_calls_logout_and_does_not_crash() throws Exception {
        Administrator adminUser = new Administrator("admin", "pw");
        when(auth.isLoggedIn()).thenReturn(true);
        when(auth.getCurrentUser()).thenReturn(adminUser);

        try (MockedStatic<RepoStorage> repoStorage = mockStatic(RepoStorage.class)) {
            repoStorage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            final AdminDashboardFrame[] ref = new AdminDashboardFrame[1];
            runOnEdt(() -> ref[0] = new AdminDashboardFrame(auth, bookingSvc, repo));
            AdminDashboardFrame frame = ref[0];

            JButton logoutBtn = findButton(frame.getContentPane(), "Logout");
            assertNotNull(logoutBtn);

            runOnEdt(logoutBtn::doClick);

            verify(auth, atLeastOnce()).logout();

            runOnEdt(() -> assertFalse(frame.isDisplayable(), "Dashboard frame should be disposed after logout"));
        }
    }
}