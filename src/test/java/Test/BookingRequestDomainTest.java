package Test;


import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import domain.TimeSlot;
import domain.Category;
import domain.User;
import domain.BookingRequest;
import domain.BookingRequestStatus;

class BookingRequestDomainTest {

    @Test
    void workflow_approve_and_reject_setters() {
        User u = new User("first","last","bob","pw", null, "a@b.com");
        Category c = new Category("Cat");
        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);

        BookingRequest r = new BookingRequest(u, s, 30, 1, "ca123");
        assertEquals(BookingRequestStatus.PENDING_CATEGORY_ADMIN, r.getStatus());

        r.approveByCategoryAdmin("ca123");
        assertEquals(BookingRequestStatus.PENDING_BIG_ADMIN, r.getStatus());
        assertEquals("ca123", r.getCategoryAdminActor());
        assertNotNull(r.getCategoryDecisionAt());

        r.rejectByBigAdmin("admin", "reason");
        assertEquals(BookingRequestStatus.REJECTED_BIG_ADMIN, r.getStatus());
        assertEquals("admin", r.getBigAdminActor());
        assertEquals("reason", r.getRejectReason());
        assertNotNull(r.getBigAdminDecisionAt());
    }
}