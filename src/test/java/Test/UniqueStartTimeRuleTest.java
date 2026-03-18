package Test;

import Service.UniqueStartTimeRule;

import domain.*;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UniqueStartTimeRuleTest {
    @Test
    void unique_start_time_passes() {
        DataRepository repo = new DataRepository();
        UniqueStartTimeRule rule = new UniqueStartTimeRule(repo);

        Category cat = new Category("C1");
        repo.addCategory(cat);
        User u = new User("x", "p");
        repo.addUser(u);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(7), 60, cat);
        Appointment ap = new Appointment(u, slot, 30, 1);

        assertTrue(rule.isValid(ap));
    }

    @Test
    void duplicate_start_time_fails() {
        DataRepository repo = new DataRepository();
        UniqueStartTimeRule rule = new UniqueStartTimeRule(repo);

        Category cat = new Category("C1");
        repo.addCategory(cat);
        User u = new User("alex", "pw");
        repo.addUser(u);

        LocalDateTime time = LocalDateTime.now().plusDays(7);
        TimeSlot slot1 = new TimeSlot(time, 60, cat);
        Appointment ap1 = new Appointment(u, slot1, 30, 1);
        ap1.confirm();
        repo.addAppointment(ap1);

        TimeSlot slot2 = new TimeSlot(time, 60, cat);
        Appointment ap2 = new Appointment(new User("b", "s"), slot2, 30, 1);

        assertFalse(rule.isValid(ap2));
        assertTrue(rule.getErrorMessage().contains("already booked"));
    }
}