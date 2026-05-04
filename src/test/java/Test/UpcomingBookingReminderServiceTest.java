package Test;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import service.UpcomingBookingReminderService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UpcomingBookingReminderService.
 */
class UpcomingBookingReminderServiceTest {

    private DataRepository repo;
    private UpcomingBookingReminderService svc;

    @BeforeEach
    void setUp() {
        repo = new DataRepository();
        svc = new UpcomingBookingReminderService(repo);
    }

    @Test
    void noAppointments_returnsEmptyList() {
        repo.addUser(new User("F", "L", "u1", "pw", LocalDate.of(1990, 1, 1), "u1@example.com"));
        List<String> msgs = svc.buildReminderMessages("u1");
        assertNotNull(msgs);
        assertTrue(msgs.isEmpty(), "Expected no reminders when there are no appointments");
    }

    @Test
    void confirmedAppointmentWithin24h_generatesReminder() {
        Category cat = new Category("Conference");
        repo.addCategory(cat);

        User u = new User("First", "Last", "jdoe", "pw", LocalDate.of(1990,1,1), "jdoe@example.com");
        repo.addUser(u);

        LocalDateTime start = LocalDateTime.now().plusHours(5); // within 24h
        TimeSlot slot = new TimeSlot(start, 60, cat);
        repo.addSlot(slot);

        Appointment ap = new Appointment(u, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        List<String> msgs = svc.buildReminderMessages("jdoe");
        assertNotNull(msgs);
        assertEquals(1, msgs.size(), "Expected one reminder for a confirmed appointment within 24h");

        String m = msgs.get(0);
        assertTrue(m.contains("Your booking starts in"), "Reminder should describe remaining time");
        assertTrue(m.contains("Category: " + cat.getName()), "Reminder should include category name");
        assertTrue(m.contains(start.toString()), "Reminder should include the start time");
    }

    @Test
    void appointmentAfter24h_isIgnored() {
        Category cat = new Category("Room");
        repo.addCategory(cat);

        User u = new User("No", "Email", "later", "pw", LocalDate.of(1990,1,1), "later@example.com");
        repo.addUser(u);

        LocalDateTime start = LocalDateTime.now().plusHours(30); // after 24h
        TimeSlot slot = new TimeSlot(start, 60, cat);
        repo.addSlot(slot);

        Appointment ap = new Appointment(u, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        List<String> msgs = svc.buildReminderMessages("later");
        assertNotNull(msgs);
        assertTrue(msgs.isEmpty(), "Appointments after 24 hours should not produce reminders");
    }

    @Test
    void pastOrNotConfirmedAppointments_areIgnored() {
        Category cat = new Category("TestCat");
        repo.addCategory(cat);

        User u = new User("Sam", "NoConfirm", "sam", "pw", LocalDate.of(1990,1,1), "sam@example.com");
        repo.addUser(u);

        TimeSlot pastSlot = new TimeSlot(LocalDateTime.now().minusHours(2), 60, cat);
        repo.addSlot(pastSlot);
        Appointment pastAp = new Appointment(u, pastSlot, 30, 1);
        pastAp.confirm();
        repo.addAppointment(pastAp);

        TimeSlot futureSlot = new TimeSlot(LocalDateTime.now().plusHours(3), 60, cat);
        repo.addSlot(futureSlot);
        Appointment pendingAp = new Appointment(u, futureSlot, 30, 1);
        repo.addAppointment(pendingAp);

        List<String> msgs = svc.buildReminderMessages("sam");
        assertNotNull(msgs);
        assertTrue(msgs.isEmpty(), "Past or non-confirmed appointments should not generate reminders");
    }

    @Test
    void usesDisplayName_whenFirstAndLastPresent() {
        Category cat = new Category("Consult");
        repo.addCategory(cat);

        User u = new User("Anna", "Smith", "ann", "pw", LocalDate.of(1990,1,1), "ann@example.com");
        repo.addUser(u);

        LocalDateTime start = LocalDateTime.now().plusHours(6);
        TimeSlot slot = new TimeSlot(start, 60, cat);
        repo.addSlot(slot);

        Appointment ap = new Appointment(u, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        List<String> msgs = svc.buildReminderMessages("ann");
        assertEquals(1, msgs.size());
        String m = msgs.get(0);
        assertTrue(m.startsWith("Reminder for Anna Smith:"), "Reminder should use full display name when available");
    }

    @Test
    void multipleConfirmedAppointmentsWithin24h_allIncluded() {
        Category cat = new Category("Multi");
        repo.addCategory(cat);

        User u = new User("Multi", "User", "multi", "pw", LocalDate.of(1990,1,1), "multi@example.com");
        repo.addUser(u);

        LocalDateTime now = LocalDateTime.now();
        TimeSlot s1 = new TimeSlot(now.plusHours(2), 60, cat);
        TimeSlot s2 = new TimeSlot(now.plusHours(10), 60, cat);
        repo.addSlot(s1);
        repo.addSlot(s2);

        Appointment a1 = new Appointment(u, s1, 30, 1); a1.confirm(); repo.addAppointment(a1);
        Appointment a2 = new Appointment(u, s2, 30, 1); a2.confirm(); repo.addAppointment(a2);

        List<String> msgs = svc.buildReminderMessages("multi");
        assertEquals(2, msgs.size(), "Should return reminders for all confirmed appointments within 24h");
    }
}