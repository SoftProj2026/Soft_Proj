package Test;

import Service.ParticipantLimitRule;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ParticipantLimitRuleTest {
    @Test
    void participants_within_limit_is_valid() {
        ParticipantLimitRule rule = new ParticipantLimitRule(4);
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("Y"));
        Appointment ap = new Appointment(new User("u", "p"), slot, 30, 2);
        assertTrue(rule.isValid(ap));
    }

    @Test
    void exceeding_max_participants_is_invalid() {
        ParticipantLimitRule rule = new ParticipantLimitRule(2);
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("Y"));
        Appointment ap = new Appointment(new User("u", "p"), slot, 30, 5);
        assertFalse(rule.isValid(ap));
        assertEquals("Participant limit exceeded.", rule.getErrorMessage());
    }
}