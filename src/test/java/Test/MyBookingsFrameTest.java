package Test;

import domain.*;
import org.junit.jupiter.api.*;
import persistence.DataRepository;
import presentation.MyBookingsFrame;
import service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * Stable Swing tests for MyBookingsFrame:
 * - Skips automatically if running in headless environment (prevents HeadlessException).
 */
class MyBookingsFrameTest {

    private DataRepository repo;
    private AuthService auth;

    private User u1;
    private User other;
    private Category catA;

    private MyBookingsFrame frame;

    @BeforeEach
    void setUp() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(),
                "Skipping Swing UI tests in headless environment.");

        repo = new DataRepository();

        catA = new Category("CatA");
        repo.addCategory(catA);

        u1 = new User("F", "L", "u1", "pw", LocalDate.of(2000, 1, 1), "mail@test.com");
        other = new User("F", "L", "u2", "pw", LocalDate.of(2000, 1, 1), "other@test.com");

        repo.addUser(u1);
        repo.addUser(other);

        auth = new AuthService(repo);
        assertTrue(auth.login("u1", "pw"));

        frame = createFrameOnEdt();
    }

    @AfterEach
    void tearDown() throws Exception {
        if (frame != null) {
            SwingUtilities.invokeAndWait(frame::dispose);
        }
    }

    private MyBookingsFrame createFrameOnEdt() throws Exception {
        AtomicReference<MyBookingsFrame> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> ref.set(new MyBookingsFrame(auth, repo)));
        return ref.get();
    }

    private static JTable findTable(Container root) {
        if (root == null) return null;
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane sp) {
                Component view = sp.getViewport().getView();
                if (view instanceof JTable t) return t;
            }
            if (c instanceof Container cc) {
                JTable t = findTable(cc);
                if (t != null) return t;
            }
        }
        return null;
    }

    private JTable table() {
        JTable t = findTable(frame.getContentPane());
        assertNotNull(t);
        return t;
    }

    private DefaultTableModel model() {
        return (DefaultTableModel) table().getModel();
    }

    private JButton findButton(String text) {
        return findButton(frame.getContentPane(), text);
    }

    private static JButton findButton(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof JButton b && text.equals(b.getText())) return b;
            if (c instanceof Container cc) {
                JButton b = findButton(cc, text);
                if (b != null) return b;
            }
        }
        return null;
    }

    @Test
    void loadMyBookings_showsOnlyCurrentUsersAppointments() throws Exception {
        TimeSlot u1Slot = new TimeSlot(LocalDateTime.now().plusDays(3).withHour(10).withMinute(0), 30, catA);
        u1Slot.setAvailable(true);
        repo.addSlot(u1Slot);

        Appointment u1Ap = new Appointment(u1, u1Slot, 30, 1);
        u1Ap.confirm();
        repo.addAppointment(u1Ap);

        TimeSlot otherSlot = new TimeSlot(LocalDateTime.now().plusDays(3).withHour(11).withMinute(0), 30, catA);
        otherSlot.setAvailable(true);
        repo.addSlot(otherSlot);

        Appointment otherAp = new Appointment(other, otherSlot, 30, 1);
        otherAp.confirm();
        repo.addAppointment(otherAp);

        SwingUtilities.invokeAndWait(() -> {
            JButton refresh = findButton("Refresh");
            assertNotNull(refresh);
            refresh.doClick();

            DefaultTableModel m = model();
            assertEquals(1, m.getRowCount());
            assertEquals("APPOINTMENT", m.getValueAt(0, 0));
            assertEquals("CatA", m.getValueAt(0, 2));
            assertEquals("CONFIRMED", m.getValueAt(0, 6));
        });
    }

    @Test
    void loadMyBookings_marksPastConfirmedAppointmentsCompleted() throws Exception {
        LocalDateTime pastStart = LocalDateTime.now().minusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);

        TimeSlot pastSlot = new TimeSlot(pastStart, 30, catA);
        pastSlot.setAvailable(true);
        repo.addSlot(pastSlot);

        Appointment past = new Appointment(u1, pastSlot, 30, 1);
        past.confirm();
        repo.addAppointment(past);

        SwingUtilities.invokeAndWait(() -> {
            JButton refresh = findButton("Refresh");
            assertNotNull(refresh);
            refresh.doClick();

            assertEquals(AppointmentStatus.COMPLETED, past.getStatus());

            DefaultTableModel m = model();
            assertEquals(1, m.getRowCount());
            assertEquals("COMPLETED", m.getValueAt(0, 6));
        });
    }
}