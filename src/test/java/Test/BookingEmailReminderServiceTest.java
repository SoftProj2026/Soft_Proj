package Test;

import domain.*;
import persistence.DataRepository;
import service.BookingEmailReminderService;
import service.FakeEmailSender;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that BookingEmailReminderService triggers email sending when appointment is within 24 hours.
 */
class BookingEmailReminderServiceTest {

    @Test
    void send24hReminderIfNeeded_sendsEmail_whenAppointmentWithin24h() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("C");
        repo.addCategory(cat);

        LocalDateTime start = LocalDateTime.now().plusHours(12);
        TimeSlot slot = new TimeSlot(start, 60, cat);
        repo.addSlot(slot);

        User user = new User("First", "Last", "jdoe", "pw", null, "jdoe@example.com");
        repo.addUser(user);

        Appointment ap = new Appointment(user, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        FakeEmailSender fake = new FakeEmailSender();
        BookingEmailReminderService svc = new BookingEmailReminderService(repo, fake);

        svc.send24hReminderIfNeeded(ap);

        List<FakeEmailSender.SentEmail> sent = fake.sent;
        assertEquals(1, sent.size(), "One reminder email should be sent");
        FakeEmailSender.SentEmail e = sent.get(0);
        assertTrue(e.subject.startsWith("Reminder:"), "Subject should start with 'Reminder:'");
        assertEquals("jdoe@example.com", e.to);
        assertTrue(e.body.contains("Appointment ID") || e.body.contains("booking"), "Body should include booking details");
    }

    @Test
    void send24hReminderIfNeeded_doesNotSend_whenEmailMissing() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("C2");
        repo.addCategory(cat);

        LocalDateTime start = LocalDateTime.now().plusHours(10);
        TimeSlot slot = new TimeSlot(start, 60, cat);
        repo.addSlot(slot);

        User user = new User("nouser", "pw"); 
        repo.addUser(user);

        Appointment ap = new Appointment(user, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        FakeEmailSender fake = new FakeEmailSender();
        BookingEmailReminderService svc = new BookingEmailReminderService(repo, fake);

        svc.send24hReminderIfNeeded(ap);

        assertEquals(0, fake.sent.size(), "No email should be sent when user email is missing");
    }
}