package Test;

import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import Service.BookingRequestService;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingRequestServiceTest {

    @Test
    void submitRequest_rejectsPastSlot() {
        DataRepository repo = new DataRepository();
        Category c = new Category("Cat");
        repo.addCategory(c);

        TimeSlot past = new TimeSlot(LocalDateTime.now().minusDays(1), 60, c);
        repo.addSlot(past);

        BookingRequestService svc = new BookingRequestService(repo);
        User u = new User("user", "pass");

        var res = svc.submitRequest(u, past, 30, 1);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("past"));
    }

    @Test
    void submitRequest_rejectsNonAvailableSlot_and_invalidParticipants_and_duration() {
        DataRepository repo = new DataRepository();
        Category c = new Category("Cat");
        repo.addCategory(c);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);
        repo.addSlot(slot);

        BookingRequestService svc = new BookingRequestService(repo);
        User u = new User("user", "pass");

        slot.book();
        var r1 = svc.submitRequest(u, slot, 30, 1);
        assertFalse(r1.isSuccess());
        assertTrue(r1.getMessage().toLowerCase().contains("not available"));

        TimeSlot slot2 = new TimeSlot(LocalDateTime.now().plusDays(2), 60, c);
        repo.addSlot(slot2);
        var r2 = svc.submitRequest(u, slot2, 30, 0);
        assertFalse(r2.isSuccess());
        assertTrue(r2.getMessage().toLowerCase().contains("participants"));

        var r3 = svc.submitRequest(u, slot2, 120, 1);
        assertFalse(r3.isSuccess());
        assertTrue(r3.getMessage().toLowerCase().contains("invalid duration"));
    }

    @Test
    void submitRequest_disallows_when_alreadyActive_and_allows_when_ok() {
        DataRepository repo = new DataRepository();
        Category c = new Category("Cat");
        repo.addCategory(c);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);
        repo.addSlot(s1);

        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(2), 60, c);
        repo.addSlot(s2);

        User u = new User("u", "p");
        repo.addUser(u);

        BookingRequestService svc = new BookingRequestService(repo);

        var ok = svc.submitRequest(u, s1, 30, 1);
        assertTrue(ok.isSuccess());
        assertTrue(s1.isHeld());
        assertNotNull(s1.getHeldRequestId());
        var second = svc.submitRequest(u, s2, 30, 1);
        assertFalse(second.isSuccess());
        assertTrue(second.getMessage().toLowerCase().contains("already have an active"));
    }
}