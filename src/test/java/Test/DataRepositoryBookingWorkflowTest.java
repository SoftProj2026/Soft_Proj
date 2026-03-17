package Test;

import persistence.DataRepository;

import domain.*;
import org.junit.jupiter.api.Test;
import Service.BookingRequestService;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryBookingWorkflowTest {

    @Test
    void category_and_big_admin_approve_flow_creates_appointment_and_audit() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Conference Hall");
        repo.addCategory(cat);

        User u = new User("first", "last", "alice", "pw", null, "a@b.com");
        repo.addUser(u);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        BookingRequest br = new BookingRequest(u, slot, 30, 1, BookingRequestService.categoryAdminUsername(cat));
        slot.hold(br.getId());
        repo.addBookingRequest(br);

        String msg1 = repo.approveByCategoryAdmin(br.getId(), br.getCategoryAdminUsername());
        assertTrue(msg1.toLowerCase().contains("sent to big admin"));
        assertEquals(BookingRequestStatus.PENDING_BIG_ADMIN, br.getStatus());

        String msg2 = repo.approveByBigAdmin(br.getId(), "admin");
        assertTrue(msg2.toLowerCase().contains("appointment confirmed") || msg2.toLowerCase().contains("final approval"));
        assertEquals(BookingRequestStatus.APPROVED_AND_CONFIRMED, br.getStatus());

        assertFalse(repo.getAppointments().isEmpty());
        Appointment ap = repo.getAppointments().get(0);
        assertEquals(AppointmentStatus.CONFIRMED, ap.getStatus());

        boolean hasConfirmed = repo.getAuditEvents().stream()
                .anyMatch(e -> e.getType() == AuditEvent.Type.APPOINTMENT_CONFIRMED);
        assertTrue(hasConfirmed);
    }
}