package Test;


import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import persistence.DataRepository;
public class CancelRulesTest {

    @Test
    void cancelAppointment_onlyFutureConfirmedAllowed() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Conference Hall");
        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot pastSlot = new TimeSlot(LocalDateTime.now().minusDays(1).withHour(10), 60, cat);
        Appointment past = new Appointment(u, pastSlot, 30, 1);
        past.confirm();
        repo.addAppointment(past);

        String msg = repo.cancelAppointment(past);
        assertTrue(msg.toLowerCase().contains("future"));
    }

    @Test
    void cancelAppointment_onlyOncePerCategory() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("Conference Hall");

        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10), 60, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(10), 60, cat);

        Appointment a1 = new Appointment(u, s1, 30, 1);
        a1.confirm();

        Appointment a2 = new Appointment(u, s2, 30, 1);
        a2.confirm();

        repo.addAppointment(a1);
        repo.addAppointment(a2);

        String r1 = repo.cancelAppointment(a1);
        assertTrue(r1.toLowerCase().contains("cancelled"));

        String r2 = repo.cancelAppointment(a2);
        assertTrue(r2.toLowerCase().contains("only cancel one"));
    }
}