package Test;

import domain.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import persistence.DataRepository;
import service.AppointmentTypeService;
import service.EmailSender;
import service.FakeEmailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Demonstrates Mockito usage: mock EmailSender and verify send() called with expected recipient/contents.
 */
@ExtendWith(MockitoExtension.class)
class AppointmentTypeServiceMockitoTest {

    @Mock
    EmailSender emailSender; 

    @Captor
    ArgumentCaptor<String> fromCaptor;

    @Captor
    ArgumentCaptor<String> toCaptor;

    @Captor
    ArgumentCaptor<String> subjectCaptor;

    @Captor
    ArgumentCaptor<String> bodyCaptor;

    @Test
    void emergency_withEmail_sendsEmail_and_returnsSaved() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("Conference");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        User user = new User("First", "Last", "jdoe", "pw", null, "jdoe@example.com");
        repo.addUser(user);

        Appointment appt = new Appointment(user, slot, 30, 1);
        appt.confirm();
        repo.addAppointment(appt);

        AppointmentTypeService svc = new AppointmentTypeService(repo, emailSender);

        String res = svc.setAppointmentType(appt,
                AppointmentType.EMERGENCY,
                null,
                null,
                slot.getStartDateTime());

        assertEquals("Saved.", res);

        verify(emailSender, times(1)).send(fromCaptor.capture(),
                toCaptor.capture(),
                subjectCaptor.capture(),
                bodyCaptor.capture());

        String to = toCaptor.getValue();
        String body = bodyCaptor.getValue();
        String subject = subjectCaptor.getValue();

        assertEquals("jdoe@example.com", to);
        assertNotNull(subject);
        assertTrue(subject.contains("Emergency Appointment") || subject.toLowerCase().contains("emergency"));
        assertNotNull(body);
        assertTrue(body.contains("Reference: #" + appt.getId()));
        assertTrue(body.contains(AppointmentTypeService.COMPANY_EMERGENCY_PHONE));
    }
}