package Test;
import domain.*;
import persistence.DataRepository;
import service.OneBookingPerCategoryRule;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OneBookingPerCategoryRuleTest {

    @Test
    void prevents_second_confirmed_in_same_category_for_user() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("Meet");
        repo.addCategory(cat);

        User u = new User("F", "L", "alex", "pw", null, "alex@example.com");
        repo.addUser(u);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(2), 60, cat);
        repo.addSlot(s1);
        repo.addSlot(s2);

        Appointment ap1 = new Appointment(u, s1, 60, 1);
        ap1.confirm();
        repo.addAppointment(ap1);

        Appointment ap2 = new Appointment(u, s2, 60, 1);

        OneBookingPerCategoryRule rule = new OneBookingPerCategoryRule(repo);
        assertFalse(rule.isValid(ap2));
        assertTrue(rule.getErrorMessage().toLowerCase().contains("already"));
    }
}