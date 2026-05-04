package Test;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import service.AppointmentTypeService;
import service.FakeEmailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AppointmentTypeService#setAppointmentType(...)
 */
class AppointmentTypeServiceTest {

    @Test
    void emergency_whenConfirmedAndHasEmail_sendsEmailAndReturnsSaved() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Conference Hall");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        User user = new User("First", "Last", "jdoe", "pw", null, "jdoe@example.com");
        repo.addUser(user);

        Appointment appt = new Appointment(user, slot, 30, 1);
        appt.confirm();
        repo.addAppointment(appt);

        FakeEmailSender fake = new FakeEmailSender();
        AppointmentTypeService svc = new AppointmentTypeService(repo, fake);

        String res = svc.setAppointmentType(
                appt,
                AppointmentType.EMERGENCY,
                null,
                null,
                slot.getStartDateTime()
        );

        assertEquals("Saved.", res);
        assertEquals(AppointmentType.EMERGENCY, appt.getAppointmentType());
        assertEquals(slot.getStartDateTime(), appt.getEmergencyPreferredSlotStart());

        assertEquals(1, fake.sent.size(), "Expected one email to be sent");
        FakeEmailSender.SentEmail email = fake.sent.get(0);

        assertEquals("jdoe@example.com", email.to);
        assertTrue(email.subject.toLowerCase().contains("emergency"));
        assertTrue(email.body.contains("Reference: #" + appt.getId()));
        assertTrue(email.body.contains(AppointmentTypeService.COMPANY_EMERGENCY_PHONE));
    }

    @Test
    void emergency_missingEmail_returnsMessageAndDoesNotSend() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("C");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        User user = new User("u", "p");
        repo.addUser(user);

        Appointment appt = new Appointment(user, slot, 30, 1);
        appt.confirm();
        repo.addAppointment(appt);

        FakeEmailSender fake = new FakeEmailSender();
        AppointmentTypeService svc = new AppointmentTypeService(repo, fake);

        String res = svc.setAppointmentType(
                appt,
                AppointmentType.EMERGENCY,
                null,
                null,
                slot.getStartDateTime()
        );

        assertEquals("Emergency selected, but user email is missing.", res);
        assertEquals(0, fake.sent.size(), "No email should be sent when user email is missing");
    }

    @Test
    void notConfirmed_returnsErrorAndDoesNotSend() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("C");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        User user = new User("First", "Last", "u1", "pw", null, "u1@example.com");
        repo.addUser(user);

        Appointment appt = new Appointment(user, slot, 30, 1);
        repo.addAppointment(appt);

        FakeEmailSender fake = new FakeEmailSender();
        AppointmentTypeService svc = new AppointmentTypeService(repo, fake);

        String res = svc.setAppointmentType(
                appt,
                AppointmentType.EMERGENCY,
                null,
                null,
                slot.getStartDateTime()
        );

        assertEquals("Only CONFIRMED appointments can be classified.", res);
        assertEquals(0, fake.sent.size());
    }

    @Test
    void review_missingTargetSlot_returnsErrorAndDoesNotSend() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("C");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        User user = new User("First", "Last", "u1", "pw", null, "u1@example.com");
        repo.addUser(user);

        Appointment appt = new Appointment(user, slot, 30, 1);
        appt.confirm();
        repo.addAppointment(appt);

        FakeEmailSender fake = new FakeEmailSender();
        AppointmentTypeService svc = new AppointmentTypeService(repo, fake);

        String res = svc.setAppointmentType(
                appt,
                AppointmentType.REVIEW,
                null,
                null, 
                null
        );

        assertEquals("Please select an available slot for Review.", res);
        assertEquals(0, fake.sent.size(), "Review should not send emails here");
    }

    @Test
    void group_withMissingGroupSize_returnsRulesErrorAndDoesNotSend() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("C");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        User user = new User("First", "Last", "u1", "pw", null, "u1@example.com");
        repo.addUser(user);

        Appointment appt = new Appointment(user, slot, 30, 3);
        appt.confirm();
        repo.addAppointment(appt);

        FakeEmailSender fake = new FakeEmailSender();
        AppointmentTypeService svc = new AppointmentTypeService(repo, fake);

        String res = svc.setAppointmentType(
                appt,
                AppointmentType.GROUP,
                null, 
                null,
                null
        );

        assertTrue(res.toLowerCase().contains("group size"), "Expected group-size validation error");
        assertEquals(0, fake.sent.size());
    }
}