package Test;

import domain.*;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataRepositoryExtraCoverageTest {

    private Category category(String name) {
        return new Category(name);
    }

    private User user(String username) {
        return new User("First", "Last", username, "pw", null, "");
    }

    private TimeSlot futureSlot(Category category) {
        return new TimeSlot(LocalDateTime.now().plusDays(5), 60, category);
    }

    private Appointment confirmedAppointment(User user, TimeSlot slot) {
        Appointment appointment = new Appointment(user, slot, 30, 1);
        appointment.confirm();
        return appointment;
    }

    @Test
    void addNullObjects_areIgnoredWhereRepositorySupportsNullGuard() {
        DataRepository repo = new DataRepository();

        repo.addProvider(null);
        repo.addContactRequest(null);
        repo.addAuditEvent(null);
        repo.addBookingRequest(null);

        assertTrue(repo.getProviders().isEmpty());
        assertTrue(repo.getContactRequests().isEmpty());
        assertTrue(repo.getAuditEvents().isEmpty());
        assertTrue(repo.getBookingRequests().isEmpty());
    }

    @Test
    void purgeCategories_nullEmptyBlankAndNonMatchingInputs_returnZeroAndKeepData() {
        DataRepository repo = new DataRepository();
        Category keep = category("Keep");
        repo.addCategory(keep);

        assertEquals(0, repo.purgeCategories(null));
        assertEquals(0, repo.purgeCategories(Set.of()));
        assertEquals(0, repo.purgeCategories(Set.of("   ")));
        assertEquals(0, repo.purgeCategories(Set.of("Unknown")));

        assertEquals(1, repo.getCategories().size());
        assertSame(keep, repo.getCategories().get(0));
    }

    @Test
    void purgeCategories_removesProviderAndAuditByTargetOrDetails() {
        DataRepository repo = new DataRepository();

        Category remove = category("Legal Consultation");
        repo.addCategory(remove);

        Provider categoryAdminProvider = new Provider(
                "lc123",
                "pw",
                "Legal Admin",
                "",
                "",
                ""
        );
        repo.addProvider(categoryAdminProvider);

        repo.addAuditEvent(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                "actor",
                "Legal Consultation",
                "target match"
        ));

        repo.addAuditEvent(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                "actor",
                "other",
                "details mention legal consultation category"
        ));

        int removed = repo.purgeCategories(Set.of("legal consultation"));

        assertEquals(1, removed);
        assertTrue(repo.getCategories().isEmpty());
        assertTrue(repo.getProviders().stream()
                .noneMatch(p -> "lc123".equalsIgnoreCase(p.getUsername())));
        assertTrue(repo.getAuditEvents().isEmpty());
    }

    @Test
    void markRequestRead_returnsFalseWhenRequestNotFound() {
        DataRepository repo = new DataRepository();

        assertFalse(repo.markRequestRead(999));
    }

    @Test
    void getRequestsForProvider_handlesNullAndWhitespaceCaseInsensitive() {
        DataRepository repo = new DataRepository();

        ContactRequest request = new ContactRequest("alice", " ProviderA ", "hello");
        repo.addContactRequest(request);

        assertEquals(1, repo.getRequestsForProvider("providera").size());
        assertEquals(1, repo.getRequestsForProvider("  ProviderA  ").size());
        assertTrue(repo.getRequestsForProvider(null).isEmpty());
    }

    @Test
    void getRequestsForCategoryAdmin_andBigAdmin_filterCorrectStatuses() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("u1");

        TimeSlot s1 = futureSlot(cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(6), 60, cat);

        BookingRequest pendingCategory = new BookingRequest(u, s1, 30, 1, "catAdmin");
        BookingRequest pendingBig = new BookingRequest(u, s2, 30, 1, "catAdmin");

        repo.addBookingRequest(pendingCategory);
        repo.addBookingRequest(pendingBig);

        repo.approveByCategoryAdmin(pendingBig.getId(), "catAdmin");

        assertEquals(1, repo.getRequestsForCategoryAdmin("catAdmin").size());
        assertEquals(pendingCategory.getId(), repo.getRequestsForCategoryAdmin("catAdmin").get(0).getId());

        assertEquals(1, repo.getRequestsForBigAdmin().size());
        assertEquals(pendingBig.getId(), repo.getRequestsForBigAdmin().get(0).getId());
    }

    @Test
    void approveByCategoryAdmin_negativeBranches() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("u1");
        TimeSlot slot = futureSlot(cat);

        assertEquals("Request not found.", repo.approveByCategoryAdmin(404, "admin"));

        BookingRequest request = new BookingRequest(u, slot, 30, 1, "catAdmin");
        repo.addBookingRequest(request);

        assertEquals(
                "Not allowed: this request is not assigned to you.",
                repo.approveByCategoryAdmin(request.getId(), null)
        );

        assertEquals(
                "Not allowed: this request is not assigned to you.",
                repo.approveByCategoryAdmin(request.getId(), "wrong")
        );

        assertEquals(
                "Approved by category admin. Sent to big admin.",
                repo.approveByCategoryAdmin(request.getId(), "catAdmin")
        );

        assertEquals(
                "Request is not pending category admin.",
                repo.approveByCategoryAdmin(request.getId(), "catAdmin")
        );
    }

    @Test
    void rejectByCategoryAdmin_negativeBranches() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("u1");
        TimeSlot slot = futureSlot(cat);

        assertEquals("Request not found.", repo.rejectByCategoryAdmin(404, "admin", "reason"));

        BookingRequest request = new BookingRequest(u, slot, 30, 1, "catAdmin");
        repo.addBookingRequest(request);

        assertEquals(
                "Not allowed: this request is not assigned to you.",
                repo.rejectByCategoryAdmin(request.getId(), null, "reason")
        );

        assertEquals(
                "Not allowed: this request is not assigned to you.",
                repo.rejectByCategoryAdmin(request.getId(), "wrong", "reason")
        );

        slot.hold(request.getId());
        assertTrue(slot.isHeld());

        assertEquals(
                "Rejected by category admin. Slot is available again.",
                repo.rejectByCategoryAdmin(request.getId(), "catAdmin", "reason")
        );

        assertFalse(slot.isHeld());

        assertEquals(
                "Request is not pending category admin.",
                repo.rejectByCategoryAdmin(request.getId(), "catAdmin", "reason")
        );
    }

    @Test
    void approveByBigAdmin_negativeBranches() {
        DataRepository repo = new DataRepository();

        assertEquals("Request not found.", repo.approveByBigAdmin(404, "admin"));

        Category cat = category("Cat");
        User u = user("u1");
        TimeSlot slot = futureSlot(cat);

        BookingRequest notPendingBig = new BookingRequest(u, slot, 30, 1, "catAdmin");
        repo.addBookingRequest(notPendingBig);

        assertEquals(
                "Request is not pending big admin.",
                repo.approveByBigAdmin(notPendingBig.getId(), "bigAdmin")
        );

        BookingRequest missingRequester = new BookingRequest(null, slot, 30, 1, "catAdmin2");
        repo.addBookingRequest(missingRequester);
        repo.approveByCategoryAdmin(missingRequester.getId(), "catAdmin2");

        assertEquals(
                "Invalid request data (missing requester/slot/category).",
                repo.approveByBigAdmin(missingRequester.getId(), "bigAdmin")
        );

        BookingRequest missingSlot = new BookingRequest(u, null, 30, 1, "catAdmin3");
        repo.addBookingRequest(missingSlot);
        repo.approveByCategoryAdmin(missingSlot.getId(), "catAdmin3");

        assertEquals(
                "Invalid request data (missing requester/slot/category).",
                repo.approveByBigAdmin(missingSlot.getId(), "bigAdmin")
        );

        TimeSlot noCategorySlot = new TimeSlot(LocalDateTime.now().plusDays(8), 60, null);
        BookingRequest missingCategory = new BookingRequest(u, noCategorySlot, 30, 1, "catAdmin4");
        repo.addBookingRequest(missingCategory);
        repo.approveByCategoryAdmin(missingCategory.getId(), "catAdmin4");

        assertEquals(
                "Invalid request data (missing requester/slot/category).",
                repo.approveByBigAdmin(missingCategory.getId(), "bigAdmin")
        );
    }

    @Test
    void rejectByBigAdmin_negativeAndSuccessBranches() {
        DataRepository repo = new DataRepository();

        assertEquals("Request not found.", repo.rejectByBigAdmin(404, "admin", "reason"));

        Category cat = category("Cat");
        User u = user("u1");
        TimeSlot slot = futureSlot(cat);

        BookingRequest request = new BookingRequest(u, slot, 30, 1, "catAdmin");
        repo.addBookingRequest(request);

        assertEquals(
                "Request is not pending big admin.",
                repo.rejectByBigAdmin(request.getId(), "bigAdmin", "reason")
        );

        repo.approveByCategoryAdmin(request.getId(), "catAdmin");
        slot.hold(request.getId());

        assertEquals(
                "Rejected by big admin. Slot is available again.",
                repo.rejectByBigAdmin(request.getId(), null, "reason")
        );

        assertFalse(slot.isHeld());
        assertEquals(BookingRequestStatus.REJECTED_BIG_ADMIN, request.getStatus());
    }

    @Test
    void countConfirmedAndPendingRequests_handleNullInputsAndCaseInsensitiveMatching() {
        DataRepository repo = new DataRepository();
        Category cat = category("TestCat");
        User u = user("Alex");

        TimeSlot slot = futureSlot(cat);
        Appointment appointment = confirmedAppointment(u, slot);
        repo.addAppointment(appointment);

        BookingRequest pending = new BookingRequest(u, futureSlot(cat), 30, 1, "catAdmin");
        repo.addBookingRequest(pending);

        assertEquals(1, repo.countConfirmedForUserCategory(" alex ", " testcat "));
        assertEquals(0, repo.countConfirmedForUserCategory(null, "testcat"));
        assertEquals(0, repo.countConfirmedForUserCategory("alex", null));

        assertEquals(1, repo.countPendingRequestsForUserCategory("alex", "testcat"));
        assertEquals(0, repo.countPendingRequestsForUserCategory(null, "testcat"));

        repo.approveByCategoryAdmin(pending.getId(), "catAdmin");
        assertEquals(1, repo.countPendingRequestsForUserCategory("alex", "testcat"));

        repo.rejectByBigAdmin(pending.getId(), "big", "no");
        assertEquals(0, repo.countPendingRequestsForUserCategory("alex", "testcat"));
    }

    @Test
    void cancelAppointment_negativeBranches() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("u1");

        assertEquals("Invalid booking.", repo.cancelAppointment(null));

        TimeSlot future = futureSlot(cat);
        Appointment notInRepo = confirmedAppointment(u, future);
        assertEquals("Booking not found.", repo.cancelAppointment(notInRepo));

        Appointment pending = new Appointment(u, futureSlot(cat), 30, 1);
        repo.addAppointment(pending);
        assertEquals("Only CONFIRMED bookings can be cancelled.", repo.cancelAppointment(pending));

        Appointment missingSlot = mock(Appointment.class);
        when(missingSlot.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
        when(missingSlot.getSlot()).thenReturn(null);
        repo.addAppointment(missingSlot);
        assertEquals("Cannot cancel booking (missing slot time).", repo.cancelAppointment(missingSlot));

        TimeSlot nullStartSlot = mock(TimeSlot.class);
        when(nullStartSlot.getStartDateTime()).thenReturn(null);

        Appointment missingStart = mock(Appointment.class);
        when(missingStart.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
        when(missingStart.getSlot()).thenReturn(nullStartSlot);
        repo.addAppointment(missingStart);
        assertEquals("Cannot cancel booking (missing slot time).", repo.cancelAppointment(missingStart));

        TimeSlot pastSlot = new TimeSlot(LocalDateTime.now().minusDays(1), 60, cat);
        Appointment past = confirmedAppointment(u, pastSlot);
        repo.addAppointment(past);
        assertEquals("Only FUTURE bookings can be cancelled.", repo.cancelAppointment(past));

        Appointment missingUser = confirmedAppointment(null, futureSlot(cat));
        repo.addAppointment(missingUser);
        assertEquals("Cannot cancel booking (missing user or category).", repo.cancelAppointment(missingUser));

        Appointment missingCategory = confirmedAppointment(u, new TimeSlot(LocalDateTime.now().plusDays(3), 60, null));
        repo.addAppointment(missingCategory);
        assertEquals("Cannot cancel booking (missing user or category).", repo.cancelAppointment(missingCategory));
    }

    @Test
    void adminCancelAppointment_negativeBranches() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("u1");

        assertEquals("Invalid booking.", repo.adminCancelAppointment(null, "admin"));

        Appointment notInRepo = confirmedAppointment(u, futureSlot(cat));
        assertEquals("Booking not found.", repo.adminCancelAppointment(notInRepo, "admin"));

        Appointment pending = new Appointment(u, futureSlot(cat), 30, 1);
        repo.addAppointment(pending);
        assertEquals("Only CONFIRMED bookings can be cancelled.", repo.adminCancelAppointment(pending, "admin"));

        Appointment missingSlot = mock(Appointment.class);
        when(missingSlot.getStatus()).thenReturn(AppointmentStatus.CONFIRMED);
        when(missingSlot.getSlot()).thenReturn(null);
        repo.addAppointment(missingSlot);
        assertEquals("Cannot cancel booking (missing slot time).", repo.adminCancelAppointment(missingSlot, "admin"));

        TimeSlot pastSlot = new TimeSlot(LocalDateTime.now().minusDays(1), 60, cat);
        Appointment past = confirmedAppointment(u, pastSlot);
        repo.addAppointment(past);
        assertEquals("Only FUTURE bookings can be cancelled.", repo.adminCancelAppointment(past, "admin"));
    }

    @Test
    void modifyAppointment_negativeBranches() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("u1");

        TimeSlot newSlot = futureSlot(cat);

        assertEquals("Invalid modification.", repo.modifyAppointment(null, newSlot, 30, 1, "actor"));

        Appointment appointment = confirmedAppointment(u, futureSlot(cat));
        assertEquals("Booking not found.", repo.modifyAppointment(appointment, newSlot, 30, 1, "actor"));

        Appointment pending = new Appointment(u, futureSlot(cat), 30, 1);
        repo.addAppointment(pending);
        assertEquals("Only CONFIRMED bookings can be modified.", repo.modifyAppointment(pending, newSlot, 30, 1, "actor"));

        Appointment confirmed = confirmedAppointment(u, futureSlot(cat));
        repo.addAppointment(confirmed);

        assertEquals("Invalid modification.", repo.modifyAppointment(confirmed, null, 30, 1, "actor"));

        TimeSlot pastNewSlot = new TimeSlot(LocalDateTime.now().minusDays(1), 60, cat);
        assertEquals("You cannot move a booking to a past time slot.",
                repo.modifyAppointment(confirmed, pastNewSlot, 30, 1, "actor"));

        TimeSlot unavailable = futureSlot(cat);
        unavailable.book();
        assertEquals("Selected new time slot is not available.",
                repo.modifyAppointment(confirmed, unavailable, 30, 1, "actor"));

        assertEquals("Invalid duration.",
                repo.modifyAppointment(confirmed, futureSlot(cat), 0, 1, "actor"));

        assertEquals("Invalid participants.",
                repo.modifyAppointment(confirmed, futureSlot(cat), 30, 0, "actor"));
    }

    @Test
    void modifyAppointment_typeOverload_rejectsInvalidRulesAndSetsGroupOnSuccess() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("actor");

        Appointment appointment = confirmedAppointment(u, futureSlot(cat));
        repo.addAppointment(appointment);

        TimeSlot destination = new TimeSlot(LocalDateTime.now().plusDays(10), 60, cat);

        String invalid = repo.modifyAppointment(
                appointment,
                destination,
                30,
                1,
                "actor",
                AppointmentType.GROUP,
                2
        );

        assertEquals("Group appointment must have at least 2 participants.", invalid);

        String success = repo.modifyAppointment(
                appointment,
                destination,
                30,
                2,
                "actor",
                AppointmentType.GROUP,
                2
        );

        assertEquals("Booking modified successfully.", success);

        Appointment updated = repo.getAppointments().stream()
                .filter(a -> a.getSlot() != null
                        && a.getSlot().getStartDateTime().equals(destination.getStartDateTime()))
                .findFirst()
                .orElseThrow();

        assertEquals(AppointmentType.GROUP, updated.getAppointmentType());
        assertEquals(Integer.valueOf(2), updated.getGroupSize());
    }

    @Test
    void modifyAppointment_typeOverload_whenBaseModifyFails_returnsBaseMessage() {
        DataRepository repo = new DataRepository();
        Category cat = category("Cat");
        User u = user("actor");

        Appointment appointment = confirmedAppointment(u, futureSlot(cat));

        String result = repo.modifyAppointment(
                appointment,
                futureSlot(cat),
                30,
                1,
                "actor",
                AppointmentType.NEW_APPOINTMENT,
                null
        );

        assertEquals("Booking not found.", result);
    }
}