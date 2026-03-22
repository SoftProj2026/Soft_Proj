package Test;

import Service.ParticipantLimitRule;
import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * تحقق حدود الحد الأدنى والعالي لعدد المشاركين (boundary conditions)
 */
class ParticipantLimitBoundaryAdditionalTest {

    @Test
    void participant_limit_boundary_values() {
        ParticipantLimitRule rule = new ParticipantLimitRule(3);
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("B"));
        Appointment apEqual = new Appointment(new User("u","p"), slot, 30, 3);
        assertTrue(rule.isValid(apEqual));
        Appointment apAbove = new Appointment(new User("u","p"), slot, 30, 4);
        assertFalse(rule.isValid(apAbove));
    }
}