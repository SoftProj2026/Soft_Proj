package Test;

import domain.*;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentTest {
    @Test
    void status_transitions_work_correctly() {
        User u = new User("user", "pass");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("cat"));
        Appointment ap = new Appointment(u, slot, 20, 1);

        assertEquals(AppointmentStatus.PENDING, ap.getStatus());
        ap.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, ap.getStatus());
        ap.complete();
        assertEquals(AppointmentStatus.COMPLETED, ap.getStatus());
        ap.cancel();
        assertEquals(AppointmentStatus.CANCELLED, ap.getStatus());
    }

    @Test
    void appointment_type_fields_can_be_set() {
        User u = new User("user", "pass");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, new Category("cat"));
        Appointment ap = new Appointment(u, slot, 20, 3);

        ap.setAppointmentType(AppointmentType.GROUP); 
        ap.setGroupSize(4);
        assertEquals(AppointmentType.GROUP, ap.getAppointmentType());
        assertEquals(4, ap.getGroupSize());

        ap.setAppointmentType(AppointmentType.REVIEW);
        ap.setReviewTargetSlotStart(slot.getStartDateTime().plusDays(1));
        assertEquals(AppointmentType.REVIEW, ap.getAppointmentType());
        assertNotNull(ap.getReviewTargetSlotStart());
    }
}