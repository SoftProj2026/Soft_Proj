package Test;

import domain.TimeSlot;
import domain.Category;
import domain.User;
import persistence.DataRepository;
import service.AuthService;
import service.ScheduleService;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleServiceTest {

    @Test
    void getAvailableSlots_requires_login_and_returns_only_available_slots() {
        DataRepository repo = new DataRepository();
        AuthService auth = new AuthService(repo);
        ScheduleService schedule = new ScheduleService(repo, auth);

        TimeSlot s = new TimeSlot(LocalDateTime.now().plusDays(1), 60, new Category("C"));
        repo.addSlot(s);

        try {
            schedule.getAvailableSlots();
            fail("Expected IllegalStateException when not logged in");
        } catch (IllegalStateException ex) {
            
        }

        User u = new User("F", "L", "bob", "pw", null, "b@b.com");
        repo.addUser(u);
        boolean ok = auth.loginAsUser("bob");
        assertTrue(ok);

        List<domain.TimeSlot> available = schedule.getAvailableSlots();
        assertNotNull(available);
        assertTrue(available.stream().anyMatch(ts -> ts.getStartDateTime().equals(s.getStartDateTime())));
    }
}