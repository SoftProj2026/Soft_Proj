package Test;


import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import persistence.DataRepository;
public class ModifyRulesTest {

    @Test
    void modifyAppointment_movesToNewSlot_andBooksNewSlot_releasesOldSlot() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("Conference Hall");

        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot oldSlot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10), 60, cat);
        TimeSlot newSlot = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(11), 60, cat);

        Appointment a = new Appointment(u, oldSlot, 30, 2);
        a.confirm();

        repo.addAppointment(a);

        assertFalse(oldSlot.isAvailable());

        String msg = repo.modifyAppointment(a, newSlot, 45, 3, "user");
        assertTrue(msg.toLowerCase().contains("modified"));

        assertTrue(oldSlot.isAvailable());
        assertFalse(newSlot.isAvailable());
        assertEquals(1, repo.getAppointments().size());
        assertEquals(AppointmentStatus.CONFIRMED, repo.getAppointments().get(0).getStatus());
    }

    @Test
    void modifyAppointment_rejectsIfNewSlotNotAvailable() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("Conference Hall");

        User u = new User("user", "pass");
        repo.addUser(u);

        TimeSlot oldSlot = new TimeSlot(LocalDateTime.now().plusDays(1).withHour(10), 60, cat);
        TimeSlot newSlot = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(11), 60, cat);
        newSlot.book();

        Appointment a = new Appointment(u, oldSlot, 30, 2);
        a.confirm();

        repo.addAppointment(a);

        String msg = repo.modifyAppointment(a, newSlot, 45, 3, "user");
        assertTrue(msg.toLowerCase().contains("not available"));
    }
}