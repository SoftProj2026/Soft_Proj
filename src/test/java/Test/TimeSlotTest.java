package Test;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import domain.TimeSlot;
import domain.Category;
class TimeSlotTest {

    @Test
    void hold_release_book_cancel_behaviour() {
        Category c = new Category("X");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);

        assertTrue(slot.isAvailable());
        slot.hold(999);
        assertTrue(slot.isHeld());
        assertEquals(Integer.valueOf(999), slot.getHeldRequestId());
        slot.releaseHold();
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertTrue(slot.isAvailable());

        slot.book();
        assertFalse(slot.isAvailable());
        slot.cancel();
        assertTrue(slot.isAvailable());
    }
}