package Test;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import Service.AdditionalAppointmentService;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AdditionalAppointmentServiceTest {

    @Test
    void createNewAppointment_validatesAndSaves() {
        DataRepository repo = new DataRepository();
        Category c = new Category("C");
        repo.addCategory(c);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);
        repo.addSlot(slot);

        User u = new User("first", "last", "bob", "pw", null, "bob@example.com");
        repo.addUser(u);

        AdditionalAppointmentService svc = new AdditionalAppointmentService(repo);

        String tooLong = svc.createNewAppointment(u, slot, 120, 1, AppointmentType.NEW_APPOINTMENT, null, null);
        assertTrue(tooLong.toLowerCase().contains("invalid duration"));

        String badParticipants = svc.createNewAppointment(u, slot, 30, 0, AppointmentType.NEW_APPOINTMENT, null, null);
        assertTrue(badParticipants.toLowerCase().contains("participants"));

        String ok = svc.createNewAppointment(u, slot, 30, 1, AppointmentType.NEW_APPOINTMENT, null, null);
        assertEquals("Saved.", ok);

        assertFalse(repo.getAppointments().isEmpty());
        Appointment a = repo.getAppointments().get(0);
        assertEquals(AppointmentStatus.CONFIRMED, a.getStatus());
        assertTrue(slot.getStartDateTime().isEqual(a.getSlot().getStartDateTime()));
        assertTrue(slot.isAvailable() == false); // slot booked
    }
}