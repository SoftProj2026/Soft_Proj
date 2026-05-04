package Test;

import domain.*;
import persistence.DataRepository;
import service.BookingResult;
import service.BookingService;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {

    @Test
    void book_rejects_past_slot_and_returns_placeholder_for_future() {
        DataRepository repo = new DataRepository();
        BookingService service = new BookingService(repo);

        TimeSlot past = new TimeSlot(LocalDateTime.now().minusHours(1), 60, new Category("C"));
        Appointment aPast = new Appointment(new User("u", "p"), past, 30, 1);
        BookingResult r1 = service.book(aPast);
        assertFalse(r1.isSuccess());
        assertTrue(r1.getMessage().toLowerCase().contains("past") || r1.getMessage().toLowerCase().contains("cannot book"));

        TimeSlot future = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("C"));
        Appointment aFuture = new Appointment(new User("u", "p"), future, 30, 1);
        BookingResult r2 = service.book(aFuture);
        assertFalse(r2.isSuccess());
        assertTrue(r2.getMessage().toLowerCase().contains("not shown") || r2.getMessage().toLowerCase().contains("logic not shown"));
    }
}