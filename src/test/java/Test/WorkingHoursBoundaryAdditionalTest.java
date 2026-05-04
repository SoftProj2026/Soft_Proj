package Test;

import domain.*;
import service.WorkingHoursRule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * اختبارات على حدود ساعات العمل (الساعة 9:00 والساعة 17:00)
 */
class WorkingHoursBoundaryAdditionalTest {

    @Test
    void start_at_9_and_end_at_17_are_valid_or_invalid_as_expected() {
        WorkingHoursRule rule = new WorkingHoursRule();

        TimeSlot atNine = new TimeSlot(LocalDateTime.of(2026,3,21,9,0), 60, new Category("W"));
        Appointment a1 = new Appointment(new User("u","p"), atNine, 30, 1);
        assertTrue(rule.isValid(a1), "Start at 9:00 should be allowed");

        TimeSlot endAtFive = new TimeSlot(LocalDateTime.of(2026,3,21,16,0), 60, new Category("W"));
        Appointment a2 = new Appointment(new User("u","p"), endAtFive, 60, 1);
        assertNotNull(rule.getErrorMessage()); 
        rule.isValid(a2);
    }
}