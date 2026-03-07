package Test;

import Service.FakeEmailSender;
import Service.BookingEmailReminderService ;

import domain.*;
import persistence.DataRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class BookingEmailReminderService_ManualMock_Test {

    @Test
    void sends_reminder_when_confirmed_and_within_24h() {
        DataRepository repo = new DataRepository();

        User u = new User("A", "B", "user1", "pass",
                LocalDate.of(2000, 1, 1),
                "user1@test.com");
        repo.addUser(u);

        Category cat = new Category("Doctor Appointment");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusHours(2), 60, cat);

        Appointment a = new Appointment(u, slot, 30, 1);
        a.confirm(); 

        FakeEmailSender fake = new FakeEmailSender();
        BookingEmailReminderService svc = new BookingEmailReminderService(repo, fake);

        svc.send24hReminderIfNeeded(a);

        assertEquals(1, fake.sent.size());
        assertEquals("user1@test.com", fake.sent.get(0).to);
        assertTrue(fake.sent.get(0).subject.toLowerCase().contains("reminder"));
    }
}