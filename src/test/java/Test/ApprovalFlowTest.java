package Test;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import persistence.DataRepository;
public class ApprovalFlowTest {

    @Test
    void approveByCategoryAdmin_thenBigAdmin_createsConfirmedAppointment_andBooksSlot() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Conference Hall");
        repo.addCategory(cat);

        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10), 60, cat);
        repo.addSlot(slot);

        BookingRequest req = new BookingRequest(u, slot, 30, 2, "ch123");
        slot.hold(req.getId());
        repo.addBookingRequest(req);

        String msg1 = repo.approveByCategoryAdmin(req.getId(), "ch123");
        assertTrue(msg1.toLowerCase().contains("sent to big admin"));

        String msg2 = repo.approveByBigAdmin(req.getId(), "admin");
        assertTrue(msg2.toLowerCase().contains("appointment confirmed"));

        assertEquals(1, repo.getAppointments().size());
        Appointment a = repo.getAppointments().get(0);
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
        assertFalse(slot.isAvailable());
        assertFalse(slot.isHeld());
    }

    @Test
    void rejectByCategoryAdmin_releasesHold() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Conference Hall");
        repo.addCategory(cat);

        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10), 60, cat);

        BookingRequest req = new BookingRequest(u, slot, 30, 2, "ch123");
        slot.hold(req.getId());
        repo.addBookingRequest(req);

        assertTrue(slot.isHeld());

        String msg = repo.rejectByCategoryAdmin(req.getId(), "ch123", "No");
        assertTrue(msg.toLowerCase().contains("available"));

        assertFalse(slot.isHeld());
        assertTrue(slot.isAvailable());
        assertEquals(BookingRequestStatus.REJECTED_CATEGORY_ADMIN, req.getStatus());
    }
}