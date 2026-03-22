package Test;

import domain.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for domain.Appointment
 */
class AppointmentTest {

    @Test
    void constructor_and_getters_initial_state() {
        User user = new User("alice", "pw");
        Category cat = new Category("C");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);

        Appointment ap = new Appointment(user, slot, 30, 2);

        assertEquals(user, ap.getUser());

        assertEquals(slot, ap.getSlot());
        assertEquals(30, ap.getDurationInMinutes());
        assertEquals(2, ap.getParticipants());
        assertEquals(AppointmentStatus.PENDING, ap.getStatus());
        assertNotNull(ap.getCreatedAt());
        assertNull(ap.getConfirmedAt());
        assertNull(ap.getCancelledAt());
        assertNull(ap.getAppointmentType());
        assertNull(ap.getGroupSize());
    }

    @Test
    void confirm_sets_confirmed_and_books_slot() {
        User user = new User("bob", "pw");
        Category cat = new Category("Cat");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);

        Appointment ap = new Appointment(user, slot, 20, 1);
        assertTrue(slot.isAvailable());

        ap.confirm();

        assertEquals(AppointmentStatus.CONFIRMED, ap.getStatus());
        assertNotNull(ap.getConfirmedAt());
        assertFalse(slot.isAvailable(), "Slot should be booked after confirm()");
    }

    @Test
    void cancel_sets_cancelled_and_releases_slot() {
        User user = new User("chris", "pw");
        Category cat = new Category("Cat2");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, cat);

        Appointment ap = new Appointment(user, slot, 45, 1);
        ap.confirm();
        assertFalse(slot.isAvailable());

        ap.cancel();

        assertEquals(AppointmentStatus.CANCELLED, ap.getStatus());
        assertNotNull(ap.getCancelledAt());
        assertTrue(slot.isAvailable(), "Slot should be available after cancel()");
    }

    @Test
    void complete_only_transitions_from_confirmed() {
        User user = new User("dan", "pw");
        Category cat = new Category("Cat3");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(3), 60, cat);

        Appointment ap = new Appointment(user, slot, 30, 1);

        ap.complete();
        assertNotEquals(AppointmentStatus.COMPLETED, ap.getStatus());

        ap.confirm();
        assertEquals(AppointmentStatus.CONFIRMED, ap.getStatus());

        ap.complete();
        assertEquals(AppointmentStatus.COMPLETED, ap.getStatus());
    }

    @Test
    void appointment_type_changes_clear_inapplicable_fields() {
        User user = new User("ela", "pw");
        Category cat = new Category("TypeCat");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(4), 60, cat);

        Appointment ap = new Appointment(user, slot, 30, 3);

        ap.setAppointmentType(AppointmentType.GROUP);
        ap.setGroupSize(4);
        assertEquals(AppointmentType.GROUP, ap.getAppointmentType());
        assertEquals(Integer.valueOf(4), ap.getGroupSize());

        ap.setAppointmentType(AppointmentType.REVIEW);
        ap.setReviewTargetSlotStart(slot.getStartDateTime().plusDays(1));
        assertEquals(AppointmentType.REVIEW, ap.getAppointmentType());
        assertNull(ap.getGroupSize());
        assertNotNull(ap.getReviewTargetSlotStart());

        ap.setAppointmentType(AppointmentType.EMERGENCY);
        ap.setEmergencyPreferredSlotStart(slot.getStartDateTime().plusHours(1));
        assertEquals(AppointmentType.EMERGENCY, ap.getAppointmentType());
        assertNull(ap.getReviewTargetSlotStart());
        assertNotNull(ap.getEmergencyPreferredSlotStart());

        ap.setAppointmentType(AppointmentType.GROUP);
        assertNull(ap.getEmergencyPreferredSlotStart());
    }

    @Test
    void setters_accept_nulls_and_do_not_throw() {
        User user = new User("frank", "pw");
        Category cat = new Category("NullCheck");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(5), 60, cat);

        Appointment ap = new Appointment(user, slot, 30, 1);
        assertDoesNotThrow(() -> {
            ap.setGroupSize(null);
            ap.setReviewTargetSlotStart(null);
            ap.setEmergencyPreferredSlotStart(null);
            ap.setAppointmentType(null);
        });

        assertNull(ap.getGroupSize());
        assertNull(ap.getReviewTargetSlotStart());
        assertNull(ap.getEmergencyPreferredSlotStart());
        assertNull(ap.getAppointmentType());
    }
}