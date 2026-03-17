package Test;

import persistence.DataRepository;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryCancelModifyTest {

    @Test
    void user_cancellation_enforced_once_per_category_and_admin_cancel() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("CancelCat");
        repo.addCategory(cat);

        User u = new User("F","L","bill","pw", null, "bill@example.com");
        repo.addUser(u);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, cat);
        repo.addSlot(slot);

        Appointment ap = new Appointment(u, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        String r1 = repo.cancelAppointment(ap);
        assertTrue(r1.toLowerCase().contains("cancelled"));

        String r2 = repo.cancelAppointment(ap);
        assertTrue(r2.toLowerCase().contains("only confirmed"));

        TimeSlot slot2 = new TimeSlot(LocalDateTime.now().plusDays(3), 60, cat);
        repo.addSlot(slot2);
        Appointment ap2 = new Appointment(u, slot2, 30, 1);
        ap2.confirm();
        repo.addAppointment(ap2);

        String adminRes = repo.adminCancelAppointment(ap2, "adminUser");
        assertTrue(adminRes.toLowerCase().contains("cancelled by admin") || adminRes.toLowerCase().contains("cancelled"));
    }

    @Test
    void modifyAppointment_moves_and_creates_new_confirmed() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("ModCat");
        repo.addCategory(cat);

        User u = new User("F","L","sam","pw", null, "s@e.com");
        repo.addUser(u);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(4), 60, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(5), 60, cat);
        repo.addSlot(s1);
        repo.addSlot(s2);

        Appointment ap = new Appointment(u, s1, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        String msg = repo.modifyAppointment(ap, s2, 20, 1, "actor");
        assertEquals("Booking modified successfully.", msg);

        boolean found = repo.getAppointments().stream()
                .anyMatch(a -> a.getSlot() != null && a.getSlot().getStartDateTime().equals(s2.getStartDateTime()));
        assertTrue(found);
    }
}