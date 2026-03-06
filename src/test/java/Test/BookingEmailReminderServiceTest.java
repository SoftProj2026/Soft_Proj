/*package Test;

import Service.BookingEmailReminderService;
import Service.EmailSender;
import domain.Appointment;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BookingEmailReminderServiceTest {

    @Test
    void sendsEmailForConfirmedAppointmentWithin24h() {
        DataRepository repo = new DataRepository();

        // user with email (required)
        User u = new User("A", "B", "user1", "pass", LocalDate.of(2000, 1, 1), "user1@example.com");
        repo.addUser(u);

        // provider for "from" email
        repo.addProvider(new domain.Provider(
                "qrbooking",
                "Comp@1234",
                "QR Booking",
                "+0000000000",
                "company@example.com",
                "N/A"
        ));

        Category cat = new Category("Exam Hall");

        LocalDateTime start = LocalDateTime.now().plusHours(2);
        TimeSlot slot = new TimeSlot(start, 60, cat);

        Appointment appt = new Appointment(u, slot, 60, 1);
        appt.confirm(); // CONFIRMED
        repo.addAppointment(appt);

        EmailSender sender = mock(EmailSender.class);
        BookingEmailReminderService svc = new BookingEmailReminderService(repo, sender);

        svc.send24hRemindersForUser("user1");

        verify(sender, times(1)).send(
                eq("company@example.com"),
                eq("user1@example.com"),
                contains("Reminder"),
                anyString()
        );
    }
}*/