package Test;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for domain.BookingRequest
 */
class BookingRequestTest {

    @Test
    void constructor_initializes_fields_and_trims_categoryAdminUsername() {
        User requester = new User("req", "pw");
        Category c = new Category("Cat");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);

        BookingRequest br = new BookingRequest(requester, slot, 30, 2, "  adminUser  ");

        assertEquals(requester, br.getRequester());
        assertEquals(slot, br.getSlot());
        assertEquals(30, br.getDurationInMinutes());
        assertEquals(2, br.getParticipants());
        assertEquals("adminUser", br.getCategoryAdminUsername());
        assertEquals(BookingRequestStatus.PENDING_CATEGORY_ADMIN, br.getStatus());
        assertNotNull(br.getCreatedAt());
        assertNull(br.getCategoryDecisionAt());
        assertNull(br.getBigAdminDecisionAt());
        assertEquals("", br.getCategoryAdminActor());
        assertEquals("", br.getBigAdminActor());
        assertEquals("", br.getRejectReason());
    }

    @Test
    void approve_by_category_admin_forwards_to_big_admin_and_records_actor_and_time() {
        BookingRequest br = new BookingRequest(new User("u","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("X")),
                30, 1, "catAdmin");

        LocalDateTime before = LocalDateTime.now();
        br.approveByCategoryAdmin(" adminActor ");
        LocalDateTime after = LocalDateTime.now();

        assertEquals(BookingRequestStatus.PENDING_BIG_ADMIN, br.getStatus());
        assertEquals("adminActor", br.getCategoryAdminActor());
        assertNotNull(br.getCategoryDecisionAt());
        assertFalse(br.getCategoryDecisionAt().isBefore(before.minusSeconds(1)));
        assertFalse(br.getCategoryDecisionAt().isAfter(after.plusSeconds(1)));
        assertEquals("", br.getRejectReason());
    }

    @Test
    void reject_by_category_admin_sets_rejected_and_reason_trimmed() {
        BookingRequest br = new BookingRequest(new User("u","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("X")),
                30, 1, "catAdmin");

        br.rejectByCategoryAdmin(" catAdmin ", "  not available  ");

        assertEquals(BookingRequestStatus.REJECTED_CATEGORY_ADMIN, br.getStatus());
        assertEquals("catAdmin", br.getCategoryAdminActor());
        assertNotNull(br.getCategoryDecisionAt());
        assertEquals("not available", br.getRejectReason());
    }

    @Test
    void approve_by_big_admin_confirms_and_records_actor_and_time() {
        BookingRequest br = new BookingRequest(new User("u","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("X")),
                30, 1, "catAdmin");

        br.approveByBigAdmin(" bigAdmin ");

        assertEquals(BookingRequestStatus.APPROVED_AND_CONFIRMED, br.getStatus());
        assertEquals("bigAdmin", br.getBigAdminActor());
        assertNotNull(br.getBigAdminDecisionAt());
        assertEquals("", br.getRejectReason());
    }

    @Test
    void reject_by_big_admin_sets_rejected_and_reason_trimmed() {
        BookingRequest br = new BookingRequest(new User("u","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("X")),
                30, 1, "catAdmin");

        br.rejectByBigAdmin(" bigAdmin ", "  reason  ");

        assertEquals(BookingRequestStatus.REJECTED_BIG_ADMIN, br.getStatus());
        assertEquals("bigAdmin", br.getBigAdminActor());
        assertNotNull(br.getBigAdminDecisionAt());
        assertEquals("reason", br.getRejectReason());
    }

    @Test
    void ids_increment_between_instances() {
        BookingRequest b1 = new BookingRequest(new User("a","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("A")),
                10, 1, "a");
        BookingRequest b2 = new BookingRequest(new User("b","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("B")),
                20, 1, "b");

        assertTrue(b2.getId() > b1.getId(), "Expected later instance id to be greater than earlier instance id");
    }

    @Test
    void timestamps_are_recent() {
        BookingRequest br = new BookingRequest(new User("u","pw"),
                new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("C")),
                15, 1, "cat");
        assertTrue(Duration.between(br.getCreatedAt(), LocalDateTime.now()).abs().getSeconds() < 5);
    }
}