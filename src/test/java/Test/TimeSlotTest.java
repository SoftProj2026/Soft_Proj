package Test;

import domain.Category;
import domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotTest {

    @Test
    void constructor_sets_start_and_end_and_category() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);
        TimeSlot slot = new TimeSlot(start, 90, new Category("catA"));

        assertEquals(start, slot.getStartDateTime());
        assertEquals(start.plusMinutes(90), slot.getEndDateTime());
        assertTrue(slot.isAvailable());
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
    }

    @Test
    void hold_sets_held_and_request_id_and_release_clears() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusHours(2), 60, null);
        assertTrue(slot.isAvailable());

        slot.hold(42);
        assertTrue(slot.isHeld());
        assertFalse(slot.isAvailable());
        assertEquals(Integer.valueOf(42), slot.getHeldRequestId());

        slot.releaseHold();
        assertFalse(slot.isHeld());
        assertTrue(slot.isAvailable());
        assertNull(slot.getHeldRequestId());
    }

    @Test
    void cannot_hold_when_already_booked() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusHours(3), 30, null);
        slot.book();

        // after booking, hold should have no effect
        slot.hold(7);
        assertFalse(slot.isHeld(), "hold() should not set held when slot is already booked");
        assertNull(slot.getHeldRequestId());
        assertFalse(slot.isAvailable(), "booked slot should not be available");
    }

    @Test
    void book_clears_hold_and_cancel_restores_availability() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusHours(4), 45, null);

        // hold then book: booking should clear hold info
        slot.hold(55);
        assertTrue(slot.isHeld());
        slot.book();
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertFalse(slot.isAvailable(), "after booking slot should not be available");

        // cancel should make slot available again
        slot.cancel();
        assertTrue(slot.isAvailable(), "after cancel slot should be available");
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
    }
}