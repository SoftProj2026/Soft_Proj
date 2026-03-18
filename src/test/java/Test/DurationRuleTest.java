package Test;

import Service.DurationRule;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DurationRuleTest {
    @Test
    void duration_within_limit_is_valid() {
        DurationRule rule = new DurationRule(45);
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("X"));
        Appointment ap = new Appointment(new User("u","p"), slot, 40, 1);
        assertTrue(rule.isValid(ap));
    }

    @Test
    void duration_exceeding_limit_is_invalid() {
        DurationRule rule = new DurationRule(30);
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("X"));
        Appointment ap = new Appointment(new User("u","p"), slot, 45, 1);
        assertFalse(rule.isValid(ap));
        assertEquals("Duration exceeds maximum allowed time.", rule.getErrorMessage());
    }
}