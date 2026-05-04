package Test;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import service.BookingRequestService;
import service.BookingResult;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BookingRequestServiceTest {

    private DataRepository repo;
    private BookingRequestService svc;

    private Category cat;
    private User user;
    private TimeSlot futureSlot;

    @BeforeEach
    void setUp() {
        repo = new DataRepository();
        svc = new BookingRequestService(repo);

        cat = new Category("Conference Hall");
        repo.addCategory(cat);

        user = new User("F", "L", "u1", "pw", LocalDate.of(1995, 1, 1), "u1@test.com");
        repo.addUser(user);

        futureSlot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(9).withMinute(0), 60, cat);
        repo.addSlot(futureSlot);
    }

    @Test
    void submitRequest_success_createsRequest_and_holdsSlot() {
        assertTrue(futureSlot.isAvailable());
        assertFalse(futureSlot.isHeld());

        BookingResult res = svc.submitRequest(user, futureSlot, 30, 2);

        assertTrue(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("request submitted"));

        assertEquals(1, repo.getBookingRequests().size(), "booking request should be stored in repo");

        BookingRequest r = repo.getBookingRequests().get(0);
        assertEquals(user.getUsername(), r.getRequester().getUsername());
        assertEquals(BookingRequestStatus.PENDING_CATEGORY_ADMIN, r.getStatus());

        assertTrue(futureSlot.isHeld());
        assertEquals(r.getId(), futureSlot.getHeldRequestId());
        assertFalse(futureSlot.isAvailable());
    }

    @Test
    void submitRequest_rejects_when_requesterOrSlotOrCategoryMissing() {
        BookingResult r1 = svc.submitRequest(null, futureSlot, 30, 1);
        assertFalse(r1.isSuccess());

        TimeSlot slotNoCat = new TimeSlot(LocalDateTime.now().plusDays(1), 60);
        BookingResult r2 = svc.submitRequest(user, slotNoCat, 30, 1);
        assertFalse(r2.isSuccess());
    }

    @Test
    void submitRequest_rejects_pastSlot() {
        TimeSlot past = new TimeSlot(LocalDateTime.now().minusHours(2), 60, cat);
        repo.addSlot(past);

        BookingResult res = svc.submitRequest(user, past, 30, 1);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("past"));
    }

    @Test
    void submitRequest_rejects_unavailableSlot() {
        // make slot unavailable by booking it
        futureSlot.book();
        assertFalse(futureSlot.isAvailable());

        BookingResult res = svc.submitRequest(user, futureSlot, 30, 1);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("not available"));
    }

    @Test
    void submitRequest_rejects_invalidParticipants_orDuration() {
        BookingResult p0 = svc.submitRequest(user, futureSlot, 30, 0);
        assertFalse(p0.isSuccess());
        assertTrue(p0.getMessage().toLowerCase().contains("participants"));

        BookingResult d0 = svc.submitRequest(user, futureSlot, 0, 1);
        assertFalse(d0.isSuccess());
        assertTrue(d0.getMessage().toLowerCase().contains("duration"));
    }

    @Test
    void submitRequest_rejects_durationLongerThanSlot() {
        BookingResult res = svc.submitRequest(user, futureSlot, 120, 1);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("max allowed"));
    }

    @Test
    void submitRequest_rejects_when_userAlreadyHasConfirmedInSameCategory() {
        // create confirmed appointment in same category
        TimeSlot slot2 = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(10), 60, cat);
        repo.addSlot(slot2);

        Appointment confirmed = new Appointment(user, slot2, 30, 1);
        confirmed.confirm();
        repo.addAppointment(confirmed);

        BookingResult res = svc.submitRequest(user, futureSlot, 30, 1);
        assertFalse(res.isSuccess());
        assertTrue(res.getMessage().toLowerCase().contains("already have an active"));
    }

    @Test
    void submitRequest_rejects_when_userAlreadyHasPendingRequestInSameCategory() {
        BookingResult first = svc.submitRequest(user, futureSlot, 30, 1);
        assertTrue(first.isSuccess());

        TimeSlot another = new TimeSlot(LocalDateTime.now().plusDays(3).withHour(11), 60, cat);
        repo.addSlot(another);

        BookingResult second = svc.submitRequest(user, another, 30, 1);
        assertFalse(second.isSuccess());
        assertTrue(second.getMessage().toLowerCase().contains("already have an active"));
    }
}
