package Test;

import Service.AuthService;
import Service.BookingService;
import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import presentation.MutualBookingFrame;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


class MutualBookingFrameTest {

    private DataRepository repo;
    private AuthService auth;
    private BookingService booking;

    private Category catA;
    private User u1;

    @BeforeEach
    void setUp() {
        repo = new DataRepository();

        catA = new Category("CatA");
        repo.addCategory(catA);

        u1 = new User("F", "L", "u1", "pw", LocalDate.of(2000, 1, 1), "mail@test.com");
        repo.addUser(u1);

        auth = new AuthService(repo);

        booking = new BookingService(repo);
    }


    private MutualBookingFrame createFrameOnEdt() throws Exception {
        AtomicReference<MutualBookingFrame> ref = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() ->
                ref.set(new MutualBookingFrame(auth, booking, repo, catA, null, null))
        );
        return ref.get();
    }

    private static List<Component> flatten(Component root) {
        ArrayList<Component> out = new ArrayList<>();
        if (!(root instanceof Container)) return out;

        ArrayDeque<Container> q = new ArrayDeque<>();
        q.add((Container) root);

        while (!q.isEmpty()) {
            Container cur = q.removeFirst();
            for (Component c : cur.getComponents()) {
                out.add(c);
                if (c instanceof Container) q.add((Container) c);
            }
        }
        return out;
    }

    private static int countRadios(Container root) {
        int n = 0;
        for (Component c : flatten(root)) {
            if (c instanceof JRadioButton) n++;
        }
        return n;
    }

    private static boolean hasLabelContaining(Container root, String text) {
        for (Component c : flatten(root)) {
            if (c instanceof JLabel) {
                String t = ((JLabel) c).getText();
                if (t != null && t.contains(text)) return true;
            }
        }
        return false;
    }


    @Test
    void notLoggedIn_showsLoginMessage_andNoRadios() throws Exception {

        MutualBookingFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasLabelContaining(frame.getContentPane(), "You must login first"),
                    "Should show login required message");
            assertEquals(0, countRadios(frame.getContentPane()),
                    "Should not render any selectable slots when not logged in");
            frame.dispose();
        });
    }

    @Test
    void noMutualSlots_showsNoMutualSlotsMessage() throws Exception {
        auth.login("u1", "pw");

        Category other = new Category("Other");
        repo.addCategory(other);

        TimeSlot wrongCat = new TimeSlot(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0), 30, other);
        wrongCat.setAvailable(true);
        repo.addSlot(wrongCat);

        TimeSlot notAvailable = new TimeSlot(LocalDateTime.now().plusDays(5).withHour(11).withMinute(0), 30, catA);
        notAvailable.setAvailable(false);
        repo.addSlot(notAvailable);

        MutualBookingFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasLabelContaining(frame.getContentPane(), "No mutual slots available"),
                    "Should show 'No mutual slots available' message");
            assertEquals(0, countRadios(frame.getContentPane()),
                    "Should not show any radio buttons when no mutual slots");
            frame.dispose();
        });
    }

    @Test
    void mutualSlotsExist_rendersRadios() throws Exception {
        auth.login("u1", "pw");

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(5).withHour(10).withMinute(0), 30, catA);
        s1.setAvailable(true);
        repo.addSlot(s1);

        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(5).withHour(11).withMinute(0), 30, catA);
        s2.setAvailable(true);
        repo.addSlot(s2);

        MutualBookingFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(countRadios(frame.getContentPane()) >= 2,
                    "Expected at least 2 radio buttons for mutual slots");
            frame.dispose();
        });
    }

    @Test
    void busyUser_excludesOverlappingSlot() throws Exception {
        auth.login("u1", "pw");

        LocalDateTime base = LocalDateTime.now().plusDays(7).withHour(10).withMinute(0).withSecond(0).withNano(0);

        TimeSlot existing = new TimeSlot(base, 60, catA);
        existing.setAvailable(true);
        repo.addSlot(existing);

        Appointment ap = new Appointment(u1, existing, 60, 1);
        ap.confirm(); 
        repo.addAppointment(ap);

        TimeSlot overlapping = new TimeSlot(base.plusMinutes(10), 30, catA);
        overlapping.setAvailable(true);
        repo.addSlot(overlapping);

        TimeSlot free = new TimeSlot(base.plusHours(3), 30, catA);
        free.setAvailable(true);
        repo.addSlot(free);

        MutualBookingFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertEquals(1, countRadios(frame.getContentPane()),
                    "Overlapping slot should be filtered; only one free slot should remain selectable");
            frame.dispose();
        });
    }

    @Test
    void oneConfirmed_showsEmergencyRequiredHint() throws Exception {
        auth.login("u1", "pw");

        LocalDateTime base = LocalDateTime.now().plusDays(8).withHour(10).withMinute(0);

        TimeSlot s1 = new TimeSlot(base, 30, catA);
        s1.setAvailable(true);
        repo.addSlot(s1);

        Appointment a1 = new Appointment(u1, s1, 30, 1);
        a1.confirm();
        repo.addAppointment(a1);

        TimeSlot s2 = new TimeSlot(base.plusHours(2), 30, catA);
        s2.setAvailable(true);
        repo.addSlot(s2);

        MutualBookingFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasLabelContaining(frame.getContentPane(), "Next booking MUST be your EMERGENCY"),
                    "Should show emergency-required hint when there is exactly one confirmed booking");
            frame.dispose();
        });
    }

    @Test
    void twoConfirmed_blocksBookings_andNoRadios() throws Exception {
        auth.login("u1", "pw");

        LocalDateTime base = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0);

        TimeSlot s1 = new TimeSlot(base, 30, catA);
        s1.setAvailable(true);
        repo.addSlot(s1);
        Appointment a1 = new Appointment(u1, s1, 30, 1);
        a1.confirm();
        repo.addAppointment(a1);

        TimeSlot s2 = new TimeSlot(base.plusHours(2), 30, catA);
        s2.setAvailable(true);
        repo.addSlot(s2);
        Appointment a2 = new Appointment(u1, s2, 30, 1);
        a2.confirm();
        repo.addAppointment(a2);

        TimeSlot s3 = new TimeSlot(base.plusHours(4), 30, catA);
        s3.setAvailable(true);
        repo.addSlot(s3);

        MutualBookingFrame frame = createFrameOnEdt();

        SwingUtilities.invokeAndWait(() -> {
            assertTrue(hasLabelContaining(frame.getContentPane(), "No more bookings allowed"),
                    "Should show blocking message when confirmed >= 2");
            assertEquals(0, countRadios(frame.getContentPane()),
                    "No radio buttons should show when booking is blocked");
            frame.dispose();
        });
    }
}