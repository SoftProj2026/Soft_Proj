package Test;

import Service.WorkingHoursRule;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class WorkingHoursRuleTest {
    @Test
    void appointment_within_working_hours() {
        WorkingHoursRule rule = new WorkingHoursRule();
        TimeSlot slot = new TimeSlot(LocalDateTime.of(2026, 3, 20, 10, 0), 60, new Category("Z"));
        Appointment ap = new Appointment(new User("u","p"), slot, 30, 1);
        assertTrue(rule.isValid(ap));
    }

    @Test
    void appointment_outside_working_hours() {
        WorkingHoursRule rule = new WorkingHoursRule();
        TimeSlot slot = new TimeSlot(LocalDateTime.of(2026, 3, 20, 20, 0), 60, new Category("Z"));
        Appointment ap = new Appointment(new User("u","p"), slot, 30, 1);
        assertFalse(rule.isValid(ap));
        assertEquals("Appointment must be between 9:00 AM and 5:00 PM.", rule.getErrorMessage());
    }
}