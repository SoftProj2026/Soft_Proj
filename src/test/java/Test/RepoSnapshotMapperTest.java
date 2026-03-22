package Test;

import persistence.RepoSnapshotMapper;
import persistence.DataRepository;
import domain.*;
import org.junit.jupiter.api.Test;
import persistence.dto.*;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RepoSnapshotMapperAdvancedTest {

    @Test
    void fullCoverageTest() throws Exception {
        DataRepository repo = new DataRepository();

        Category cat1 = new Category("Medical");
        Category cat2 = new Category("Dental");
        repo.addCategory(cat1);
        repo.addCategory(cat2);

        User user = new User("A", "B", "user1", "pass",
                LocalDate.of(2000, 1, 1), "u@test.com");

        Provider provider = new Provider(
                "prov1", "123", "Clinic", "059", "p@test.com", "Nablus"
        );

        Administrator admin = new Administrator("admin", "admin123");

        repo.addUser(user);
        repo.addProvider(provider);
        repo.addUser(admin);

        TimeSlot slot1 = new TimeSlot(LocalDateTime.now(), 30, cat1);
        TimeSlot slot2 = new TimeSlot(LocalDateTime.now().plusHours(1), 60, cat2);

        slot1.book();
        slot2.hold(5);

        repo.addSlot(slot1);
        repo.addSlot(slot2);

        Appointment app = new Appointment(user, slot1, 30, 2);
        repo.addAppointment(app);

        BookingRequest br = new BookingRequest(user, slot2, 60, 3, "admin");
        repo.addBookingRequest(br);

        ContactRequest cr = new ContactRequest("user1", "prov1", "help");
        repo.addContactRequest(cr);

        AuditEvent ae = new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT, // use an existing enum value
                "admin",
                "system",
                "created"
        );
        repo.getAuditEvents().add(ae);

        Field f = DataRepository.class.getDeclaredField("cancelUsedByUserCategory");
        f.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Boolean> underlying = (Map<String, Boolean>) f.get(repo);
        underlying.put("user1|medical", true);

        RepoSnapshot snapshot = RepoSnapshotMapper.fromRepo(repo);

        assertNotNull(snapshot);
        assertEquals(2, snapshot.categories.size(), "categories count");
        assertEquals(3, snapshot.users.size(), "users count (user + provider + admin)");
        assertEquals(2, snapshot.slots.size(), "slots count");
        assertEquals(1, snapshot.appointments.size(), "appointments count");
        assertEquals(1, snapshot.bookingRequests.size(), "bookingRequests count");
        assertEquals(1, snapshot.contactRequests.size(), "contactRequests count");
        assertTrue(snapshot.auditEvents.size() >= 1, "audit events count");
        assertEquals(1, snapshot.cancelUsed.size(), "cancelUsed entries");

        assertTrue(snapshot.slots.stream().anyMatch(s -> s.booked), "should have at least one booked slot");
        assertTrue(snapshot.slots.stream().anyMatch(s -> s.held), "should have at least one held slot");

      
        DataRepository restored = RepoSnapshotMapper.toRepo(snapshot);

        assertEquals(2, restored.getCategories().size(), "restored categories count");
        assertEquals(2, restored.getSlots().size(), "restored slots count");
        assertEquals(1, restored.getAppointments().size(), "restored appointments count");
        assertEquals(1, restored.getBookingRequests().size(), "restored booking requests count");

        assertTrue(restored.getUsers().stream().anyMatch(u -> u.getUsername().equals("user1")));
        assertTrue(restored.getUsers().stream().anyMatch(u -> u.getUsername().equals("prov1")));
        assertTrue(restored.getUsers().stream().anyMatch(u -> u.getUsername().equals("admin")));

        Field f2 = DataRepository.class.getDeclaredField("cancelUsedByUserCategory");
        f2.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Boolean> restoredMap =
                (Map<String, Boolean>) f2.get(restored);

        assertTrue(restoredMap.containsKey("user1|medical"));

        assertTrue(snapshot.nextAppointmentId > 0);
        assertTrue(snapshot.nextAuditEventId > 0);
    }

    @Test
    void testNullSafety() {
        RepoSnapshot snapshot = new RepoSnapshot();

        UserDTO user = new UserDTO();
        snapshot.users.add(user);

        TimeSlotDTO slot = new TimeSlotDTO();
        slot.start = LocalDateTime.now();
        snapshot.slots.add(slot);

        DataRepository repo = RepoSnapshotMapper.toRepo(snapshot);

        assertNotNull(repo);
    }

    @Test
    void testHeldVsBookedLogic() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("Test");
        repo.addCategory(cat);

        TimeSlot slot = new TimeSlot(LocalDateTime.now(), 30, cat);
        slot.hold(10);

        repo.addSlot(slot);

        RepoSnapshot snapshot = RepoSnapshotMapper.fromRepo(repo);

        assertTrue(snapshot.slots.get(0).held);
        assertFalse(snapshot.slots.get(0).booked);
    }

    @Test
    void testAdministratorMapping() {
        DataRepository repo = new DataRepository();

        Administrator admin = new Administrator("admin", "123");
        repo.addUser(admin);

        RepoSnapshot snapshot = RepoSnapshotMapper.fromRepo(repo);

        assertEquals("Administrator", snapshot.users.get(0).type);
    }

    @Test
    void testProviderFallbackCreation() {
        RepoSnapshot snapshot = new RepoSnapshot();

        ProviderDTO p = new ProviderDTO();
        p.username = "provX";
        p.password = "123";
        p.displayName = "ClinicX";
        p.phone = "059";
        p.email = "x@test.com";
        p.address = "Nablus";

        snapshot.providers.add(p);

        DataRepository repo = RepoSnapshotMapper.toRepo(snapshot);

        assertEquals(1, repo.getProviders().size());
    }
}