package Test;

import domain.Appointment;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import persistence.DataRepository;
import Service.BookingEmailReminderService;
import Service.EmailSender;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class BookingEmailReminderServiceMockitoTest {

    @Test
    void reminder_sent_whenWithin24h_andConfirmed_andUserHasEmail() {
        DataRepository repo = new DataRepository();

        User u = new User("A", "B", "user", "pass",
                java.time.LocalDate.of(2000, 1, 1),
                "user@test.com"
        );
        repo.addUser(u);

        Category cat = new Category("Conference Hall");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusHours(2), 60, cat);

        Appointment a = new Appointment(u, slot, 30, 1);
        a.confirm();

        EmailSender sender = Mockito.mock(EmailSender.class);
        BookingEmailReminderService svc = new BookingEmailReminderService(repo, sender);

        svc.send24hReminderIfNeeded(a);

        verify(sender, times(1)).send(anyString(), eq("user@test.com"), contains("Reminder"), anyString());
        verifyNoMoreInteractions(sender);
    }
}