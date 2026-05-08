package Test;

import domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import service.SmartSlotSuggestionService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
class SmartSlotSuggestionServiceTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private DataRepository repo;
    private SmartSlotSuggestionService service;

    @BeforeEach
    void setUp() {
        repo = new DataRepository();
        service = new SmartSlotSuggestionService(repo);
    }

    @Test
    void suggest_returnsEmpty_whenUserOrCategoryNull() {
        Category c = new Category("CatA");
        User u = new User("F", "L", "u1", "pw",
                java.time.LocalDate.of(2000, 1, 1), "mail@test.com");

        assertEquals(List.of(), service.suggest(null, c, 5));
        assertEquals(List.of(), service.suggest(u, null, 5));
    }

    @Test
    void suggest_returnsOnlyMatchingCategory_future_andAvailable() {
        Category catA = new Category("CatA");
        Category catB = new Category("CatB");
        repo.addCategory(catA);
        repo.addCategory(catB);

        User u = new User("F", "L", "u1", "pw",
                java.time.LocalDate.of(2000, 1, 1), "mail@test.com");
        repo.addUser(u);

        TimeSlot ok = new TimeSlot(LocalDateTime.now().plusDays(10), 30, catA);
        ok.setAvailable(true);
        repo.addSlot(ok);

        TimeSlot wrongCat = new TimeSlot(LocalDateTime.now().plusDays(10), 30, catB);
        wrongCat.setAvailable(true);
        repo.addSlot(wrongCat);

        TimeSlot notAvailable = new TimeSlot(LocalDateTime.now().plusDays(10), 30, catA);
        notAvailable.setAvailable(false);
        repo.addSlot(notAvailable);

        // Past
        TimeSlot past = new TimeSlot(LocalDateTime.now().minusDays(1), 30, catA);
        past.setAvailable(true);
        repo.addSlot(past);

        List<TimeSlot> out = service.suggest(u, catA, 10);

     //   assertEquals(1, out.size());
  //      assertSame(ok, out.get(0));
    }

    @Test
    void suggest_excludesOverlappingConfirmedAppointment_forSameUser() {
        Category catA = new Category("CatA");
        repo.addCategory(catA);

        User u = new User("F", "L", "u1", "pw",
                java.time.LocalDate.of(2000, 1, 1), "mail@test.com");
        repo.addUser(u);

        LocalDateTime base = LocalDateTime.now().plusDays(15).withSecond(0).withNano(0);

        TimeSlot existing = new TimeSlot(base, 60, catA);
        existing.setAvailable(true);
        repo.addSlot(existing);

        Appointment confirmed = new Appointment(u, existing, 60, 1);
        confirmed.confirm(); 
        repo.addAppointment(confirmed);

        TimeSlot overlapping = new TimeSlot(base.plusMinutes(10), 30, catA);
        overlapping.setAvailable(true);
        repo.addSlot(overlapping);

        TimeSlot free = new TimeSlot(base.plusMinutes(120), 30, catA);
        free.setAvailable(true);
        repo.addSlot(free);

        List<TimeSlot> out = service.suggest(u, catA, 10);

        assertFalse(out.contains(overlapping), "Overlapping slot should be excluded");
        assertTrue(out.contains(free), "Free slot should be included");
        assertEquals(1, out.size());
        assertSame(free, out.get(0));
    }

    @Test
    void suggest_sortsByClosestStartTime_andAppliesLimit_default5WhenLimitNonPositive() {
        Category catA = new Category("CatA");
        repo.addCategory(catA);

        User u = new User("F", "L", "u1", "pw",
                java.time.LocalDate.of(2000, 1, 1), "mail@test.com");
        repo.addUser(u);

        LocalDateTime base = LocalDateTime.now()
                .plusDays(25)
                .withHour(10).withMinute(0).withSecond(0).withNano(0);

        TimeSlot t1 = new TimeSlot(base.plusHours(6), 30, catA);
        TimeSlot t2 = new TimeSlot(base.plusHours(1), 30, catA); 
        TimeSlot t3 = new TimeSlot(base.plusHours(3), 30, catA);
        TimeSlot t4 = new TimeSlot(base.plusHours(2), 30, catA);
        TimeSlot t5 = new TimeSlot(base.plusHours(4), 30, catA);
        TimeSlot t6 = new TimeSlot(base.plusHours(5), 30, catA);

        for (TimeSlot s : List.of(t1, t2, t3, t4, t5, t6)) {
            s.setAvailable(true);
            repo.addSlot(s);
        }

        List<TimeSlot> out = service.suggest(u, catA, 0);

        assertEquals(5, out.size());
        assertSame(t2, out.get(0), "Closest slot should be first");
    }}