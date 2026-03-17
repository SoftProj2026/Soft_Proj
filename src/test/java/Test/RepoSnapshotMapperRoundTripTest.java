package Test;

import persistence.DataRepository;
import Service.BookingRequestService;
import persistence.RepoSnapshotMapper;
import domain.*;
import persistence.dto.RepoSnapshot;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RepoSnapshotMapperRoundTripTest {

    @Test
    void snapshot_and_restore_roundtrip_keeps_counts_and_entities() {
        DataRepository repo = new DataRepository();

        Category cat = new Category("RoundCat");
        repo.addCategory(cat);

        User u = new User("F", "L", "userA", "pw", null, "u@a.com");
        repo.addUser(u);

        TimeSlot slot = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        repo.addSlot(slot);

        Appointment ap = new Appointment(u, slot, 30, 1);
        ap.confirm();
        repo.addAppointment(ap);

        ContactRequest cr = new ContactRequest("guest", "provider", "Hello");
        repo.addContactRequest(cr);

        BookingRequest br = new BookingRequest(u, slot, 30, 1, BookingRequestService.categoryAdminUsername(cat));
        repo.addBookingRequest(br);

        RepoSnapshot snap = RepoSnapshotMapper.fromRepo(repo);
        assertNotNull(snap, "Snapshot should not be null (fromRepo returned null)");

        DataRepository restored = RepoSnapshotMapper.toRepo(snap);
        assertNotNull(restored, "Restored repository should not be null");

        if (repo.getCategories().size() != restored.getCategories().size()
                || repo.getUsers().size() != restored.getUsers().size()
                || repo.getSlots().size() != restored.getSlots().size()
                || repo.getAppointments().size() != restored.getAppointments().size()) {

            System.out.println("DEBUG: original counts -> categories=" + repo.getCategories().size()
                    + ", users=" + repo.getUsers().size()
                    + ", slots=" + repo.getSlots().size()
                    + ", appointments=" + repo.getAppointments().size());

            System.out.println("DEBUG: restored counts -> categories=" + restored.getCategories().size()
                    + ", users=" + restored.getUsers().size()
                    + ", slots=" + restored.getSlots().size()
                    + ", appointments=" + restored.getAppointments().size());
        }

        assertEquals(repo.getCategories().size(), restored.getCategories().size(), "Category count should match after snapshot/restore");
        assertEquals(repo.getUsers().size(), restored.getUsers().size(), "User count should match after snapshot/restore");
        assertEquals(repo.getSlots().size(), restored.getSlots().size(), "Slot count should match after snapshot/restore");
        assertEquals(repo.getAppointments().size(), restored.getAppointments().size(), "Appointment count should match after snapshot/restore");
    }
}