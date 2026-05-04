package Test;

import domain.*;
import persistence.DataRepository;
import presentation.UnifiedBookingFrame;
import service.AuthService;
import service.BookingService;
import presentation.UITheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UnifiedBookingFrameTest {

    private static void runOnEdt(Runnable r) throws Exception {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
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
    }

    private <T extends Component> List<T> findAll(Container root, Class<T> cls) {
        List<T> out = new ArrayList<>();
        for (Component c : root.getComponents()) {
            if (cls.isInstance(c)) out.add(cls.cast(c));
            if (c instanceof Container) out.addAll(findAll((Container) c, cls));
        }
        return out;
    }

    private JLabel findLabelContains(Container root, String substr) {
        for (JLabel lbl : findAll(root, JLabel.class)) {
            String txt = lbl.getText();
            if (txt != null && txt.contains(substr)) return lbl;
        }
        return null;
    }

    private JRadioButton findRadioContains(Container root, String substr) {
        for (JRadioButton rb : findAll(root, JRadioButton.class)) {
            String txt = rb.getText();
            if (txt != null && txt.contains(substr)) return rb;
        }
        for (AbstractButton b : findAll(root, AbstractButton.class)) {
            String txt = b.getText();
            if (txt != null && txt.contains(substr) && b instanceof JRadioButton) {
                return (JRadioButton) b;
            }
        }
        return null;
    }

    DataRepository repo;
    AuthService authMock;
    BookingService bookingMock;
    Category category;
    UnifiedBookingFrame frame;

    @BeforeEach
    void setUp() throws Exception {
        runOnEdt(() -> UITheme.apply());
        repo = new DataRepository();
        authMock = mock(AuthService.class);
        bookingMock = mock(BookingService.class);
        category = new Category("TestCat");
        repo.addCategory(category);
        runOnEdt(() -> frame = new UnifiedBookingFrame(authMock, bookingMock, repo, category));
    }

    @Test
    void day_bar_contains_seven_buttons() throws Exception {
        runOnEdt(() -> {
            int count = 0;
            for (JButton b : findAll(frame, JButton.class)) {
                String t = b.getText();
                if (t != null && t.contains("/")) count++;
            }
            assertTrue(count >= 7, "Expect at least 7 day-like buttons (found " + count + ")");
        });
    }

    @Test
    void company_shows_available_slot_when_slot_exists_and_user_logged_in() throws Exception {
        LocalDate today = LocalDate.now();
        LocalDateTime start = LocalDateTime.of(today, LocalTime.of(9, 0));
        TimeSlot slot = new TimeSlot(start, 60, category);
        repo.addSlot(slot);
        User u = new User("F", "L", "u1", "pw", LocalDate.of(1990,1,1), "a@b.com");
        repo.addUser(u);
        when(authMock.isLoggedIn()).thenReturn(true);
        when(authMock.getCurrentUser()).thenReturn(u);
        runOnEdt(() -> {
            frame.dispose();
            frame = new UnifiedBookingFrame(authMock, bookingMock, repo, category);
        });
        runOnEdt(() -> {
            JLabel availableLabel = findLabelContains(frame, "09:00  (Available)");
            assertNotNull(availableLabel, "Expected company available label for 09:00");
        });
    }

    @Test
    void mutual_shows_send_request_radio_when_slot_is_mutually_ok_and_user_logged_in() throws Exception {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime start = LocalDateTime.of(date, LocalTime.of(10, 0));
        TimeSlot slot = new TimeSlot(start, 60, category);
        repo.addSlot(slot);
        User u = new User("F", "L", "u2", "pw", LocalDate.of(1990,1,1), "a@b.com");
        repo.addUser(u);
        when(authMock.isLoggedIn()).thenReturn(true);
        when(authMock.getCurrentUser()).thenReturn(u);

        runOnEdt(() -> {
            frame.dispose();
            frame = new UnifiedBookingFrame(authMock, bookingMock, repo, category);
        });

        // Set selectedMutualDate via reflection and call reloadMutual() to force reload for our date
        runOnEdt(() -> {
            try {
                Field sel = UnifiedBookingFrame.class.getDeclaredField("selectedMutualDate");
                sel.setAccessible(true);
                sel.set(frame, date);

                Method reload = UnifiedBookingFrame.class.getDeclaredMethod("reloadMutual");
                reload.setAccessible(true);
                reload.invoke(frame);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        runOnEdt(() -> {
            JRadioButton radio = findRadioContains(frame, "(Send Request)");
            assertNotNull(radio, "Expected a radio button for Send Request");
            String txt = radio.getText();
            assertNotNull(txt);
            assertTrue(txt.contains("10:00") || txt.contains("(Send Request)"));
        });
    }

    @Test
    void mutual_shows_login_required_when_not_logged_in() throws Exception {
        when(authMock.isLoggedIn()).thenReturn(false);
        when(authMock.getCurrentUser()).thenReturn(null);
        runOnEdt(() -> {
            frame.dispose();
            frame = new UnifiedBookingFrame(authMock, bookingMock, repo, category);
        });
        runOnEdt(() -> {
            JLabel needLabel = findLabelContains(frame, "Login required");
            assertNotNull(needLabel, "When not logged in mutual panel should show 'Login required'");
        });
    }

    @Test
    void mutual_shows_past_rows_for_past_slots() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDateTime start = LocalDateTime.of(startDate, LocalTime.of(11, 0));
        TimeSlot past = new TimeSlot(start, 60, category);
        repo.addSlot(past);
        User u = new User("F", "L", "u3", "pw", LocalDate.of(1990,1,1), "a@b.com");
        repo.addUser(u);
        when(authMock.isLoggedIn()).thenReturn(true);
        when(authMock.getCurrentUser()).thenReturn(u);

        runOnEdt(() -> {
            frame.dispose();
            frame = new UnifiedBookingFrame(authMock, bookingMock, repo, category);
        });

        runOnEdt(() -> {
            try {
                Field sel = UnifiedBookingFrame.class.getDeclaredField("selectedMutualDate");
                sel.setAccessible(true);
                sel.set(frame, startDate);

                Method reload = UnifiedBookingFrame.class.getDeclaredMethod("reloadMutual");
                reload.setAccessible(true);
                reload.invoke(frame);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        });

        runOnEdt(() -> {
            JLabel pastLabel = findLabelContains(frame, "Past - Not bookable");
            assertNotNull(pastLabel, "Expected presence of 'Past - Not bookable' row for past slot");
        });
    }
}