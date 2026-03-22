package Test;

import domain.BookingRequestStatus;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BookingRequestStatusTest {

    @Test
    void all_expected_constants_present() {
        BookingRequestStatus[] vals = BookingRequestStatus.values();
        assertEquals(5, vals.length, "Expected 5 status constants");

        assertEquals(BookingRequestStatus.PENDING_CATEGORY_ADMIN, BookingRequestStatus.valueOf("PENDING_CATEGORY_ADMIN"));
        assertEquals(BookingRequestStatus.REJECTED_CATEGORY_ADMIN, BookingRequestStatus.valueOf("REJECTED_CATEGORY_ADMIN"));
        assertEquals(BookingRequestStatus.PENDING_BIG_ADMIN, BookingRequestStatus.valueOf("PENDING_BIG_ADMIN"));
        assertEquals(BookingRequestStatus.REJECTED_BIG_ADMIN, BookingRequestStatus.valueOf("REJECTED_BIG_ADMIN"));
        assertEquals(BookingRequestStatus.APPROVED_AND_CONFIRMED, BookingRequestStatus.valueOf("APPROVED_AND_CONFIRMED"));
    }

    @Test
    void logical_ordering_checks() {
        assertTrue(BookingRequestStatus.PENDING_CATEGORY_ADMIN.ordinal() < BookingRequestStatus.PENDING_BIG_ADMIN.ordinal());
        assertTrue(BookingRequestStatus.PENDING_BIG_ADMIN.ordinal() < BookingRequestStatus.APPROVED_AND_CONFIRMED.ordinal());
    }
}