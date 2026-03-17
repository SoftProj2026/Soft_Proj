package Test;

import Service.FakeEmailSender;

import Service.EmailReminderScheduler;
import Service.AuthService;
import Service.BookingEmailReminderService;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration-style test for EmailReminderScheduler: start the scheduler (it runs initial check immediately)
 * and ensure reminder service sends email for appointments within 24 hours.
 */
class EmailReminderSchedulerIntegrationTest {

    @Test
    void scheduler_triggers_reminder_and_tracks_sent() throws Exception {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Rem");
        repo.addCategory(cat);

        User u = new User("X","Y","tim","pw", null, "tim@example.com");
        repo.addUser(u);

        TimeSlot soon = new TimeSlot(LocalDateTime.now().plusHours(10), 60, cat);
        repo.addSlot(soon);

        Appointment a = new Appointment(u, soon, 30, 1);
        a.confirm();
        repo.addAppointment(a);

        FakeEmailSender fake = new FakeEmailSender();
        BookingEmailReminderService remService = new BookingEmailReminderService(repo, fake);
        AuthService auth = new AuthService(repo);
        auth.loginAsUser("tim");

        EmailReminderScheduler sched = new EmailReminderScheduler(repo, auth, remService, 1);
        try {
            sched.start();
            // scheduler's initial run is immediate (initial delay 0) so wait briefly for the thread to execute
            Thread.sleep(400);
            // Email should have been captured
            assertFalse(fake.sent.isEmpty());
            boolean found = fake.sent.stream().anyMatch(e -> e.to.equals("tim@example.com"));
            assertTrue(found);
        } finally {
            sched.stop();
        }
    }
}