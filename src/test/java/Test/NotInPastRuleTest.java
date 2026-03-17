package Test;


import domain.Appointment;
import domain.TimeSlot;
import domain.User;
import domain.Category;
import org.junit.jupiter.api.Test;
import Service.NotInPastRule;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotInPastRuleTest {

    @Test
    void disallowPast_appointment() {
        Category c = new Category("X");
        TimeSlot past = new TimeSlot(LocalDateTime.now().minusHours(2), 60, c);
        Appointment a = new Appointment(new User("u", "p"), past, 30, 1);

        NotInPastRule rule = new NotInPastRule();
        assertFalse(rule.isValid(a));
        assertEquals("Cannot book an appointment in the past.", rule.getErrorMessage());
    }

    @Test
    void allowFuture_appointment() {
        Category c = new Category("X");
        TimeSlot future = new TimeSlot(LocalDateTime.now().plusHours(2), 60, c);
        Appointment a = new Appointment(new User("u", "p"), future, 30, 1);

        NotInPastRule rule = new NotInPastRule();
        assertTrue(rule.isValid(a));
    }
}