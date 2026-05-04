package Test;

import domain.*;
import persistence.DataRepository;
import service.OverlapRule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OverlapRuleTest {

    @Test
    void detect_overlap_with_existing_appointment() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Cat");
        repo.addCategory(cat);

        TimeSlot existingSlot = new TimeSlot(LocalDateTime.now().plusHours(2), 60, cat);
        repo.addSlot(existingSlot);

        User u1 = new User("A", "B", "u1", "pw", null, "a@b.com");
        repo.addUser(u1);

        Appointment existing = new Appointment(u1, existingSlot, 60, 1);
        existing.confirm();
        repo.addAppointment(existing);

        TimeSlot newSlot = new TimeSlot(existingSlot.getStartDateTime().plusMinutes(30), 60, cat);

        User u2 = new User("u2", "pw");
        repo.addUser(u2);

        Appointment newAppt = new Appointment(u2, newSlot, 60, 1);

        OverlapRule rule = new OverlapRule(repo);
        assertFalse(rule.isValid(newAppt), "Expected overlap to be detected.");
        assertEquals("Time slot overlaps with an existing booking.", rule.getErrorMessage());
    }

    @Test
    void no_overlap_when_times_separate() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("Cat2");
        repo.addCategory(cat);

        TimeSlot slot1 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(9), 60, cat);
        repo.addSlot(slot1);

        User u = new User("F", "L", "user", "pw", null, "a@b.com");
        repo.addUser(u);

        Appointment ap1 = new Appointment(u, slot1, 60, 1);
        ap1.confirm();
        repo.addAppointment(ap1);

        TimeSlot nonOverlap = new TimeSlot(slot1.getEndDateTime().plusMinutes(1), 60, cat);

        User u2 = new User("u2", "p");
        repo.addUser(u2);

        Appointment newAppt = new Appointment(u2, nonOverlap, 60, 1);

        OverlapRule rule = new OverlapRule(repo);
        assertTrue(rule.isValid(newAppt));
    }
}