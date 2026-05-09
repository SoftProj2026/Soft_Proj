package Test;

import domain.*;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DataRepositoryQuickBoostTest {

    @Test
    void approveByBigAdmin_withEmail_runsEmailBranchAndStillConfirms() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("EmailCat");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(3), 60, cat);
        User user = new User("F", "L", "emailUser", "pw", null, "email@test.com");

        BookingRequest request = new BookingRequest(user, slot, 30, 1, "catAdmin");
        repo.addBookingRequest(request);

        assertEquals(
                "Approved by category admin. Sent to big admin.",
                repo.approveByCategoryAdmin(request.getId(), "catAdmin")
        );

        String result = repo.approveByBigAdmin(request.getId(), null);

        assertTrue(result.contains("Final approval done"));
        assertEquals(BookingRequestStatus.APPROVED_AND_CONFIRMED, request.getStatus());
        assertEquals(1, repo.getAppointments().size());
        assertFalse(repo.getAuditEvents().isEmpty());
    }

    @Test
    void rejectByBigAdmin_withNullSlot_doesNotCrash() {
        DataRepository repo = new DataRepository();

        User user = new User("F", "L", "u1", "pw", null, "");
        BookingRequest request = new BookingRequest(user, null, 30, 1, "catAdmin");

        repo.addBookingRequest(request);
        repo.approveByCategoryAdmin(request.getId(), "catAdmin");

        String result = repo.rejectByBigAdmin(request.getId(), null, "no");

        assertEquals("Rejected by big admin. Slot is available again.", result);
        assertEquals(BookingRequestStatus.REJECTED_BIG_ADMIN, request.getStatus());
    }

    @Test
    void addBookingRequest_withNullRequesterAndNullSlot_usesFallbackAuditValues() {
        DataRepository repo = new DataRepository();

        BookingRequest request = new BookingRequest(null, null, 30, 1, "admin");
        repo.addBookingRequest(request);

        assertEquals(1, repo.getBookingRequests().size());
        assertEquals(1, repo.getAuditEvents().size());
        assertEquals("unknown", repo.getAuditEvents().get(0).getActorUsername());
        assertEquals("N/A", repo.getAuditEvents().get(0).getTarget());
    }

    @Test
    void countMethods_ignoreNonMatchingAndNonConfirmedRecords() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("CatA");
        Category otherCat = new Category("CatB");

        User user = new User("F", "L", "alex", "pw", null, "");
        User otherUser = new User("F", "L", "sam", "pw", null, "");

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, cat);
        TimeSlot otherSlot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, otherCat);

        Appointment pending = new Appointment(user, slot, 30, 1);
        repo.addAppointment(pending);

        Appointment confirmedOtherUser = new Appointment(otherUser, slot, 30, 1);
        confirmedOtherUser.confirm();
        repo.addAppointment(confirmedOtherUser);

        Appointment confirmedOtherCategory = new Appointment(user, otherSlot, 30, 1);
        confirmedOtherCategory.confirm();
        repo.addAppointment(confirmedOtherCategory);

        assertEquals(0, repo.countConfirmedForUserCategory("alex", "CatA"));

        BookingRequest reqOtherUser = new BookingRequest(otherUser, slot, 30, 1, "admin");
        BookingRequest reqOtherCat = new BookingRequest(user, otherSlot, 30, 1, "admin");

        repo.addBookingRequest(reqOtherUser);
        repo.addBookingRequest(reqOtherCat);

        assertEquals(0, repo.countPendingRequestsForUserCategory("alex", "CatA"));
    }

    @Test
    void purgeCategories_handlesNullObjectsInsideCollections() {
        DataRepository repo = new DataRepository();

        repo.getCategories().add(null);
        repo.getSlots().add(null);
        repo.getAppointments().add(null);
        repo.getBookingRequests().add(null);
        repo.getUsers().add(null);
        repo.getProviders().add(null);
        repo.getAuditEvents().add(null);

        Category removable = new Category("Remove Me");
        repo.addCategory(removable);

        int removed = repo.purgeCategories(Set.of("Remove Me"));

        assertEquals(1, removed);
        assertTrue(repo.getCategories().stream()
                .noneMatch(c -> c != null && "Remove Me".equals(c.getName())));
    }

    @Test
    void adminCancelAppointment_successWithNullAdminAndMissingUserUsesFallbacks() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Cat");
        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(4), 60, cat);

        Appointment appointment = new Appointment(null, slot, 30, 1);
        appointment.confirm();

        repo.addAppointment(appointment);

        String result = repo.adminCancelAppointment(appointment, null);

        assertEquals("Appointment cancelled by admin.", result);
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertEquals(1, repo.getAuditEvents().size());
        assertEquals("admin", repo.getAuditEvents().get(0).getActorUsername());
    }

    @Test
    void modifyAppointment_successWithNullActorAndNullCategoryUsesFallbacks() {
        DataRepository repo = new DataRepository();

        User user = new User("F", "L", "u1", "pw", null, "");
        TimeSlot oldSlot = new TimeSlot(LocalDateTime.now().plusDays(4), 60, null);
        TimeSlot newSlot = new TimeSlot(LocalDateTime.now().plusDays(5), 60, null);

        Appointment appointment = new Appointment(user, oldSlot, 30, 1);
        appointment.confirm();

        repo.addAppointment(appointment);

        String result = repo.modifyAppointment(appointment, newSlot, 30, 1, null);

        assertEquals("Booking modified successfully.", result);
        assertTrue(oldSlot.isAvailable());
        assertFalse(newSlot.isAvailable());
        assertEquals(1, repo.getAuditEvents().size());
        assertEquals("system", repo.getAuditEvents().get(0).getActorUsername());
        assertEquals("N/A", repo.getAuditEvents().get(0).getTarget());
    }

    @Test
    void modifyAppointment_typeOverload_withWrongActorDoesNotSetTypeButStillSucceeds() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Cat");
        User user = new User("F", "L", "correctUser", "pw", null, "");

        TimeSlot oldSlot = new TimeSlot(LocalDateTime.now().plusDays(4), 60, cat);
        TimeSlot newSlot = new TimeSlot(LocalDateTime.now().plusDays(5), 60, cat);

        Appointment appointment = new Appointment(user, oldSlot, 30, 1);
        appointment.confirm();

        repo.addAppointment(appointment);

        String result = repo.modifyAppointment(
                appointment,
                newSlot,
                30,
                1,
                "wrongUser",
                AppointmentType.NEW_APPOINTMENT,
                null
        );

        assertEquals("Booking modified successfully.", result);

        Appointment updated = repo.getAppointments().get(0);
        assertNull(updated.getAppointmentType());
    }
}
