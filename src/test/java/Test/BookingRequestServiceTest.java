package Test;


import domain.BookingRequest;
import domain.BookingRequestStatus;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import Service.BookingRequestService;
import java.time.LocalDateTime;
import Service.BookingResult;

import static org.junit.jupiter.api.Assertions.*;

public class BookingRequestServiceTest {

    @Test
    void submitRequest_holdsSlot_andCreatesRequest() {
        DataRepository repo = new DataRepository();
        BookingRequestService svc = new BookingRequestService(repo);

        Category cat = new Category("Conference Hall");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), 60, cat);
        repo.addSlot(slot);

        User u = new User("user", "pass");
        repo.addUser(u);

        BookingResult r = svc.submitRequest(u, slot, 30, 2);

        assertTrue(r.isSuccess());
        assertTrue(slot.isHeld());
        assertNotNull(slot.getHeldRequestId());

        assertEquals(1, repo.getBookingRequests().size());

        BookingRequest br = repo.getBookingRequests().get(0);
        assertEquals(BookingRequestStatus.PENDING_CATEGORY_ADMIN, br.getStatus());
        assertEquals(u.getUsername(), br.getRequester().getUsername());
        assertEquals(cat.getName(), br.getSlot().getCategory().getName());
    }

    @Test
    void submitRequest_rejectsPastSlot() {
        DataRepository repo = new DataRepository();
        BookingRequestService svc = new BookingRequestService(repo);

        Category cat = new Category("Conference Hall");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().minusHours(2), 60, cat);

        User u = new User("user", "pass");

        BookingResult r = svc.submitRequest(u, slot, 30, 2);
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().toLowerCase().contains("past"));
    }

    @Test
    void submitRequest_rejectsIfSlotNotAvailable() {
        DataRepository repo = new DataRepository();
        BookingRequestService svc = new BookingRequestService(repo);

        Category cat = new Category("Conference Hall");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0), 60, cat);
        slot.book();

        User u = new User("user", "pass");

        BookingResult r = svc.submitRequest(u, slot, 30, 2);
        assertFalse(r.isSuccess());
        assertTrue(r.getMessage().toLowerCase().contains("not available"));
    }

    @Test
    void submitRequest_blocksThirdActiveItemInCategory() {
        DataRepository repo = new DataRepository();
        BookingRequestService svc = new BookingRequestService(repo);

        Category cat = new Category("Conference Hall");
        repo.addCategory(cat);

        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(9), 60, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10), 60, cat);
        TimeSlot s3 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(11), 60, cat);

        repo.addSlot(s1);
        repo.addSlot(s2);
        repo.addSlot(s3);

        assertTrue(svc.submitRequest(u, s1, 30, 1).isSuccess());
        assertTrue(svc.submitRequest(u, s2, 30, 1).isSuccess());

        BookingResult third = svc.submitRequest(u, s3, 30, 1);
        assertFalse(third.isSuccess());
        assertTrue(third.getMessage().toLowerCase().contains("main"));
    }
}