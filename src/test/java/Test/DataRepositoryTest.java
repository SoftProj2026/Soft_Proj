package Test;

import domain.*;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for persistence.DataRepository
 */
class DataRepositoryTest {

    @Test
    void add_and_get_collections_work() {
        DataRepository repo = new DataRepository();

        User u = new User("fn", "ln", "u1", "pw", null, ""); 
        repo.addUser(u);
        assertTrue(repo.getUsers().contains(u));

        Provider p = new Provider("prov", "pw", "N", null, null, null);
        repo.addProvider(p);
        assertTrue(repo.getProviders().contains(p));
        assertTrue(repo.getUsers().contains(p), "addProvider should also add provider to users list");

        Category c = new Category("CatA");
        repo.addCategory(c);
        assertTrue(repo.getCategories().contains(c));

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(2), 60, c);
        repo.addSlot(slot);
        assertTrue(repo.getSlots().contains(slot));

        Appointment ap = new Appointment(u, slot, 30, 1);
        repo.addAppointment(ap);
        assertTrue(repo.getAppointments().contains(ap));

        ContactRequest cr = new ContactRequest("alice", "prov", "msg");
        repo.addContactRequest(cr);
        assertTrue(repo.getContactRequests().contains(cr));
        assertFalse(repo.getAuditEvents().isEmpty(), "addContactRequest should add an audit event");
    }

    @Test
    void purgeCategories_removes_related_objects_and_category_admin_users() {
        DataRepository repo = new DataRepository();

        Category toRemove = new Category("Doctor Appointment");
        Category keep = new Category("KeepMe");
        repo.addCategory(toRemove);
        repo.addCategory(keep);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(3), 60, toRemove);
        repo.addSlot(s1);

        User u = new User("a", "b");
        repo.addUser(u);

        Appointment ap = new Appointment(u, s1, 20, 1);
        ap.confirm();
        repo.addAppointment(ap);

        BookingRequest br = new BookingRequest(u, s1, 20, 1, "someAdmin");
        repo.addBookingRequest(br);

        repo.addAuditEvent(new AuditEvent(AuditEvent.Type.MESSAGE_SENT, "actor", "some", "booking Doctor Appointment was created"));

        repo.addUser(new User("x", "y", "da123", "pw", null, ""));

        int removed = repo.purgeCategories(Set.of("Doctor Appointment"));
        assertTrue(removed >= 1, "Expected at least one category removed");

        assertFalse(repo.getCategories().stream().anyMatch(ch -> "Doctor Appointment".equalsIgnoreCase(ch.getName())));
        assertFalse(repo.getSlots().stream().anyMatch(sl -> sl.getCategory() != null && "Doctor Appointment".equalsIgnoreCase(sl.getCategory().getName())));
        assertFalse(repo.getAppointments().stream().anyMatch(a -> a.getSlot() != null && a.getSlot().getCategory() != null
                && "Doctor Appointment".equalsIgnoreCase(a.getSlot().getCategory().getName())));
        assertFalse(repo.getBookingRequests().stream().anyMatch(r -> r.getSlot() != null && r.getSlot().getCategory() != null
                && "Doctor Appointment".equalsIgnoreCase(r.getSlot().getCategory().getName())));

        assertFalse(repo.getUsers().stream().anyMatch(user -> "da123".equalsIgnoreCase(user.getUsername())));
    }

    @Test
    void contact_requests_and_mark_read_and_requests_for_provider() {
        DataRepository repo = new DataRepository();
        ContactRequest cr = new ContactRequest("john", "prov1", "hi");
        repo.addContactRequest(cr);

        List<ContactRequest> forProv = repo.getRequestsForProvider("prov1");
        assertEquals(1, forProv.size());
        assertEquals(cr.getId(), forProv.get(0).getId());

        boolean marked = repo.markRequestRead(cr.getId());
        assertTrue(marked);
        assertTrue(cr.isRead());
    }

    @Test
    void booking_request_workflow_category_admin_and_big_admin_paths() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("TestCat");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(4), 60, cat);
        repo.addSlot(slot);

        User requester = new User("fn", "ln", "reqUser", "pw", null, ""); // empty email to avoid SMTP
        repo.addUser(requester);

        BookingRequest br = new BookingRequest(requester, slot, 30, 1, "catAdminUser");
        repo.addBookingRequest(br);

        List<BookingRequest> forCatAdmin = repo.getRequestsForCategoryAdmin("catAdminUser");
        assertTrue(forCatAdmin.stream().anyMatch(r -> r.getId() == br.getId()));

        String wrong = repo.approveByCategoryAdmin(br.getId(), "other");
        assertTrue(wrong.toLowerCase().contains("not allowed"));

        String ok = repo.approveByCategoryAdmin(br.getId(), "catAdminUser");
        assertTrue(ok.toLowerCase().contains("approved"));
        assertEquals(BookingRequestStatus.PENDING_BIG_ADMIN, br.getStatus());
        assertTrue(repo.getAuditEvents().stream().anyMatch(e -> e.getDetails().contains("sent to big admin")));

        String rejectNotAllowed = repo.rejectByCategoryAdmin(br.getId(), "catAdminUser", "no");
        assertTrue(rejectNotAllowed.toLowerCase().contains("request is not pending category admin"));

        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(5), 60, cat);
        repo.addSlot(s2);
        BookingRequest br2 = new BookingRequest(requester, s2, 30, 1, "catX");
        repo.addBookingRequest(br2);

        s2.hold(br2.getId());
        assertTrue(s2.isHeld());
        String rej = repo.rejectByCategoryAdmin(br2.getId(), "catX", "not ok");
        assertTrue(rej.toLowerCase().contains("rejected"));
        assertFalse(s2.isHeld(), "rejectByCategoryAdmin should release hold");
    }

    @Test
    void approve_by_big_admin_success_and_limit_exceeded() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("SomeCat");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(6), 60, cat);
        repo.addSlot(slot);

        User requester = new User("f", "l", "bob", "pw", null, ""); // empty email to skip SMTP
        repo.addUser(requester);

        BookingRequest br = new BookingRequest(requester, slot, 30, 1, "catAdmin");
        repo.addBookingRequest(br);

        repo.approveByCategoryAdmin(br.getId(), "catAdmin");
        assertEquals(BookingRequestStatus.PENDING_BIG_ADMIN, br.getStatus());

        String res = repo.approveByBigAdmin(br.getId(), "bigAdmin");
        assertTrue(res.toLowerCase().contains("appointment"));
        assertTrue(repo.getAppointments().stream().anyMatch(a -> a.getUser().getUsername().equalsIgnoreCase("bob")));

        TimeSlot sA = new TimeSlot(LocalDateTime.now().plusDays(7), 60, cat);
        TimeSlot sB = new TimeSlot(LocalDateTime.now().plusDays(8), 60, cat);
        repo.addSlot(sA);
        repo.addSlot(sB);

        Appointment a1 = new Appointment(requester, sA, 30, 1);
        a1.confirm();
        repo.addAppointment(a1);

        Appointment a2 = new Appointment(requester, sB, 30, 1);
        a2.confirm();
        repo.addAppointment(a2);

        TimeSlot sC = new TimeSlot(LocalDateTime.now().plusDays(9), 60, cat);
        repo.addSlot(sC);
        BookingRequest br3 = new BookingRequest(requester, sC, 30, 1, "catAdminZ");
        repo.addBookingRequest(br3);
        repo.approveByCategoryAdmin(br3.getId(), "catAdminZ");

        String rejected = repo.approveByBigAdmin(br3.getId(), "bigAdminZ");
        assertTrue(rejected.toLowerCase().contains("rejected"), "Expected rejection due to two confirmed bookings already");
        assertTrue(sC.isAvailable());
    }

    @Test
    void cancel_and_admin_cancel_and_one_per_category_rule_and_modify_appointment() {
        DataRepository repo = new DataRepository();
        Category cat = new Category("CMod");
        repo.addCategory(cat);

        TimeSlot s1 = new TimeSlot(LocalDateTime.now().plusDays(10), 60, cat);
        TimeSlot s2 = new TimeSlot(LocalDateTime.now().plusDays(11), 60, cat);
        repo.addSlot(s1);
        repo.addSlot(s2);

        User u = new User("fn", "ln", "userX", "pw", null, "");
        repo.addUser(u);

        Appointment ap1 = new Appointment(u, s1, 30, 1);
        ap1.confirm();
        repo.addAppointment(ap1);

        String cancelRes = repo.cancelAppointment(ap1);
        assertTrue(cancelRes.toLowerCase().contains("cancelled"));
        assertEquals(AppointmentStatus.CANCELLED, ap1.getStatus());

        Appointment ap2 = new Appointment(u, s2, 30, 1);
        ap2.confirm();
        repo.addAppointment(ap2);

        String cancelSecond = repo.cancelAppointment(ap2);
        assertTrue(cancelSecond.toLowerCase().contains("not allowed") || cancelSecond.toLowerCase().contains("only one"), "Expected cancellation to be denied due to one-cancellation-per-category rule");

        String adminCancel = repo.adminCancelAppointment(ap2, "adminGuy");
        assertTrue(adminCancel.toLowerCase().contains("cancelled"));
        assertEquals(AppointmentStatus.CANCELLED, ap2.getStatus());

        TimeSlot sNew = new TimeSlot(LocalDateTime.now().plusDays(12), 60, cat);
        repo.addSlot(sNew);
        Appointment ap3 = new Appointment(u, sNew, 30, 1);
        ap3.confirm();
        repo.addAppointment(ap3);

        TimeSlot dest = new TimeSlot(LocalDateTime.now().plusDays(13), 60, cat);
        repo.addSlot(dest);

        assertTrue(dest.isAvailable());

        String modRes = repo.modifyAppointment(ap3, dest, 45, 2, "userX");
        assertEquals("Booking modified successfully.", modRes);
        assertTrue(repo.getAppointments().stream().anyMatch(a -> a.getSlot().getStartDateTime().equals(dest.getStartDateTime())));
    }
}