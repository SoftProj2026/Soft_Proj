package Test;

import Service.MinimumNoticeRule;

import domain.Appointment;
import domain.TimeSlot;
import domain.User;
import domain.Category;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MinimumNoticeRuleTest {

    @Test
    void requiresAtLeastOneHourNotice() {
        Category c = new Category("C");
        TimeSlot close = new TimeSlot(LocalDateTime.now().plusMinutes(30), 60, c);
        Appointment aClose = new Appointment(new User("u", "p"), close, 30, 1);

        MinimumNoticeRule rule = new MinimumNoticeRule();
        assertFalse(rule.isValid(aClose), "Appointment less than 1 hour should be invalid.");

        TimeSlot later = new TimeSlot(LocalDateTime.now().plusHours(2), 60, c);
        Appointment aLater = new Appointment(new User("u", "p"), later, 30, 1);
        assertTrue(rule.isValid(aLater), "Appointment >=1 hour should be valid.");
    }
}