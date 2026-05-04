package Test;

import domain.Appointment;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import service.BlockedSlotsRule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class BlockedSlotsRuleTest {

    @Test
    void getBlockMessageIfBlocked_returnsMessage_duringBreak() {
        BlockedSlotsRule rule = new BlockedSlotsRule();

        Category cat = new Category("C");
        TimeSlot slot = new TimeSlot(
                LocalDateTime.of(2026, 4, 2, 12, 0), 
                60,
                cat
        );

        String msg = rule.getBlockMessageIfBlocked(slot);
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("12:00"));
    }

    @Test
    void getBlockMessageIfBlocked_returnsNull_outsideBreak() {
        BlockedSlotsRule rule = new BlockedSlotsRule();

        Category cat = new Category("C");

        TimeSlot beforeBreak = new TimeSlot(LocalDateTime.of(2026, 4, 2, 11, 0), 60, cat);
        assertNull(rule.getBlockMessageIfBlocked(beforeBreak));

        TimeSlot atBreakEnd = new TimeSlot(LocalDateTime.of(2026, 4, 2, 13, 0), 60, cat);
        assertNull(rule.getBlockMessageIfBlocked(atBreakEnd));

        TimeSlot afterBreak = new TimeSlot(LocalDateTime.of(2026, 4, 2, 14, 0), 60, cat);
        assertNull(rule.getBlockMessageIfBlocked(afterBreak));
    }

    @Test
    void isValid_falseDuringBreak_and_setsErrorMessage() {
        BlockedSlotsRule rule = new BlockedSlotsRule();

        User u = new User("u", "pw");
        Category cat = new Category("C");
        TimeSlot breakSlot = new TimeSlot(LocalDateTime.of(2026, 4, 2, 12, 30), 60, cat);

        Appointment appt = new Appointment(u, breakSlot, 30, 1);

        assertFalse(rule.isValid(appt));
        assertTrue(rule.getErrorMessage().toLowerCase().contains("12:00"));
    }

    @Test
    void isValid_trueOutsideBreak() {
        BlockedSlotsRule rule = new BlockedSlotsRule();

        User u = new User("u", "pw");
        Category cat = new Category("C");
        TimeSlot okSlot = new TimeSlot(LocalDateTime.of(2026, 4, 2, 11, 30), 60, cat);

        Appointment appt = new Appointment(u, okSlot, 30, 1);

        assertTrue(rule.isValid(appt));
    }

    @Test
    void isValid_falseWhenAppointmentNull() {
        BlockedSlotsRule rule = new BlockedSlotsRule();
        assertFalse(rule.isValid(null));
    }
}