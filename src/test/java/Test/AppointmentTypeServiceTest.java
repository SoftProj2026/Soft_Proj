package Test;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import Service.AppointmentTypeService;
import Service.FakeEmailSender;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentTypeServiceTest {

    @Test
    void emergency_requiresEmail_and_sendsEmail_when_present() {
        DataRepository repo = new DataRepository();
        Category c = new Category("C");
        repo.addCategory(c);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, c);
        repo.addSlot(slot);

        User uWithNoEmail = new User("u", "p");
        repo.addUser(uWithNoEmail);

        Appointment a1 = new Appointment(uWithNoEmail, slot, 30, 1);
        a1.confirm();
        repo.addAppointment(a1);

        AppointmentTypeService svcNoEmail = new AppointmentTypeService(repo, new FakeEmailSender());
        String resNoEmail = svcNoEmail.setAppointmentType(
                a1,
                AppointmentType.EMERGENCY,
                null,
                null,
                slot.getStartDateTime()    
        );
        assertEquals("Emergency selected, but user email is missing.", resNoEmail);

        User u = new User("first", "last", "jane", "pw", null, "jane@example.com");
        repo.addUser(u);
        TimeSlot slot2 = new TimeSlot(LocalDateTime.now().plusDays(2), 60, c);
        repo.addSlot(slot2);
        Appointment a2 = new Appointment(u, slot2, 30, 1);
        a2.confirm();
        repo.addAppointment(a2);

        FakeEmailSender fake = new FakeEmailSender();
        AppointmentTypeService svc = new AppointmentTypeService(repo, fake);
        String ok = svc.setAppointmentType(
                a2,
                AppointmentType.EMERGENCY,
                null,
                null,
                slot2.getStartDateTime()
        );
        assertEquals("Saved.", ok);

        List<FakeEmailSender.SentEmail> sent = fake.sent;
        assertEquals(1, sent.size());
        assertEquals("jane@example.com", sent.get(0).to);
        assertTrue(sent.get(0).subject.contains("Emergency Appointment"));
    }
}