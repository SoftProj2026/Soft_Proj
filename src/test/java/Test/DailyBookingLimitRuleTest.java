package Test;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import service.DailyBookingLimitRule;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DailyBookingLimitRuleTest {

    DataRepository repo;
    DailyBookingLimitRule rule;
    User user;
    Category cat;

    @BeforeEach
    void setUp() {
        repo = new DataRepository();
        rule = new DailyBookingLimitRule(repo);

        user = new User("First", "Last", "user1", "pw", LocalDate.of(1995,1,1), "a@b.com");
        repo.addUser(user);

        cat = new Category("TestCat");
        repo.addCategory(cat);
    }

    @Test
    void allowsBookingWhenLessThanLimit() {
        LocalDateTime base = LocalDate.now().plusDays(1).atTime(10, 0);

        for (int i=0;i<2;i++) {
            TimeSlot s = new TimeSlot(base.plusHours(i), 30, cat);
            repo.addSlot(s);
            Appointment a = new Appointment(user, s, 30, 1);
            a.confirm();
            repo.addAppointment(a);
        }

        TimeSlot newSlot = new TimeSlot(base.plusHours(2), 30, cat);
        Appointment newAppt = new Appointment(user, newSlot, 30, 1);

        assertTrue(rule.isValid(newAppt), "Should allow when less than max per day");
    }

    @Test
    void blocksBookingAtLimit() {
        LocalDateTime base = LocalDate.now().plusDays(2).atTime(10, 0);

        for (int i=0;i<3;i++) {
            TimeSlot s = new TimeSlot(base.plusHours(i), 30, cat);
            repo.addSlot(s);
            Appointment a = new Appointment(user, s, 30, 1);
            a.confirm();
            repo.addAppointment(a);
        }

        TimeSlot newSlot = new TimeSlot(base.plusHours(5), 30, cat);
        Appointment newAppt = new Appointment(user, newSlot, 30, 1);

        assertFalse(rule.isValid(newAppt), "Should block if already at daily max");
        assertEquals("You cannot book more than 3 appointments per day.", rule.getErrorMessage());
    }

    @Test
    void onlyCountsConfirmedAppointments() {
        LocalDateTime base = LocalDate.now().plusDays(3).atTime(10, 0);

        for (int i=0;i<2;i++) {
            TimeSlot s = new TimeSlot(base.plusHours(i), 30, cat);
            repo.addSlot(s);
            Appointment a = new Appointment(user, s, 30, 1);
            a.confirm();
            repo.addAppointment(a);
        }
        for (int i=2;i<4;i++) {
            TimeSlot s = new TimeSlot(base.plusHours(i), 30, cat);
            repo.addSlot(s);
            Appointment a = new Appointment(user, s, 30, 1);
            repo.addAppointment(a);
        }

        TimeSlot newSlot = new TimeSlot(base.plusHours(4), 30, cat);
        Appointment newAppt = new Appointment(user, newSlot, 30, 1);

        assertTrue(rule.isValid(newAppt), "Should only count confirmed; pending not counted");
    }
}