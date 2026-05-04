package Test;

import domain.*;
import service.SlotAvailabilityRule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SlotAvailabilityRuleTest {
    @Test
    void slot_is_available() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("A"));
        Appointment ap = new Appointment(new User("u", "p"), slot, 30, 1);
        SlotAvailabilityRule rule = new SlotAvailabilityRule();
        assertTrue(rule.isValid(ap));
    }

    @Test
    void slot_is_not_available() {
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("A"));
        slot.book(); 
        Appointment ap = new Appointment(new User("u", "p"), slot, 30, 1);
        SlotAvailabilityRule rule = new SlotAvailabilityRule();
        assertFalse(rule.isValid(ap));
        assertEquals("Selected time slot is already booked.", rule.getErrorMessage());
    }
}