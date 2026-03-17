package Test;

import domain.Category;
import domain.TimeSlot;
import org.junit.jupiter.api.Test;
import Service.BlockedSlotsRule;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class BlockedSlotsRuleTest {

    @Test
    void blockMessage_whenStartInsideBreak() {
        Category c = new Category("C");
        LocalDateTime start = LocalDateTime.of(2026, 3, 17, 12, 0); 
        TimeSlot slot = new TimeSlot(start, 60, c);

        BlockedSlotsRule rule = new BlockedSlotsRule();
        String msg = rule.getBlockMessageIfBlocked(slot);
        assertNotNull(msg);
        assertTrue(msg.toLowerCase().contains("12:00"));
        assertFalse(rule.isValid(null)); 
    }

    @Test
    void notBlocked_whenOutsideBreak() {
        Category c = new Category("C");
        LocalDateTime start = LocalDateTime.of(2026, 3, 17, 11, 0); 
        TimeSlot slot = new TimeSlot(start, 60, c);

        BlockedSlotsRule rule = new BlockedSlotsRule();
        assertNull(rule.getBlockMessageIfBlocked(slot));
        assertFalse(rule.isValid(null));
    }
}