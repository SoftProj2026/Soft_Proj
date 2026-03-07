package Test;
import Service.EmailSender;
import Service.BookingEmailReminderService ;
import domain.*;
import persistence.DataRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

public class BookingEmailReminderService_Mockito_Test {

    @Test
    void sends_email_within_24h() {
        DataRepository repo = new DataRepository();

        User u = new User("A", "B", "user1", "pass",
                LocalDate.of(2000, 1, 1),
                "user1@test.com");
        repo.addUser(u);

        Category cat = new Category("Doctor Appointment");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusHours(2), 60, cat);

        Appointment a = new Appointment(u, slot, 30, 1);
        a.confirm();

        EmailSender sender = mock(EmailSender.class);
        BookingEmailReminderService svc = new BookingEmailReminderService(repo, sender);

        svc.send24hReminderIfNeeded(a);

        verify(sender, times(1)).send(anyString(), eq("user1@test.com"), anyString(), anyString());
    }
}