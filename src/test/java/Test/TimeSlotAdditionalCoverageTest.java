package Test;

import domain.Category;
import domain.TimeSlot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimeSlotAdditionalCoverageTest {

    @Test
    void constructorWithCategory_initializesAvailableSlot() {
        Category category = new Category("Conference Hall");
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        TimeSlot slot = new TimeSlot(start, 90, category);

        assertEquals(start, slot.getStartDateTime());
        assertEquals(start.plusMinutes(90), slot.getEndDateTime());
        assertEquals(category, slot.getCategory());
        assertTrue(slot.isAvailable());
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertEquals(90, slot.getDuration());
    }

    @Test
    void constructorWithoutCategory_setsNullCategory() {
        LocalDateTime start = LocalDateTime.now().plusDays(1);

        TimeSlot slot = new TimeSlot(start, 60);

        assertNull(slot.getCategory());
        assertEquals(60, slot.getDuration());
        assertTrue(slot.isAvailable());
    }

    @Test
    void hold_marksSlotAsHeldAndUnavailable() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.hold(55);

        assertTrue(slot.isHeld());
        assertEquals(55, slot.getHeldRequestId());
        assertFalse(slot.isAvailable());
    }

    @Test
    void releaseHold_clearsHeldStateAndMakesSlotAvailable() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.hold(10);
        slot.releaseHold();

        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertTrue(slot.isAvailable());
    }

    @Test
    void book_marksSlotBookedAndClearsHold() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.hold(20);
        slot.book();

        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertFalse(slot.isAvailable());
    }

    @Test
    void hold_doesNothingWhenSlotAlreadyBooked() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.book();
        slot.hold(99);

        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertFalse(slot.isAvailable());
    }

    @Test
    void cancel_makesBookedSlotAvailableAgain() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.book();
        slot.cancel();

        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
        assertTrue(slot.isAvailable());
    }

    @Test
    void setAvailableTrue_clearsBookedHeldAndRequestId() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.hold(77);
        slot.setAvailable(true);

        assertTrue(slot.isAvailable());
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
    }

    @Test
    void setAvailableFalse_marksSlotUnavailableAndClearsHold() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60);

        slot.hold(88);
        slot.setAvailable(false);

        assertFalse(slot.isAvailable());
        assertFalse(slot.isHeld());
        assertNull(slot.getHeldRequestId());
    }

    @Test
    void getDuration_returnsDurationBetweenStartAndEnd() {
        LocalDateTime start = LocalDateTime.now().plusDays(2);
        TimeSlot slot = new TimeSlot(start, 120);

        assertEquals(120, slot.getDuration());
    }
}