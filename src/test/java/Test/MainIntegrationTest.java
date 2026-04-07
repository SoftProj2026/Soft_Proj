package Test;

import MainApp.Main;
import domain.Administrator;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistence.DataRepository;
import persistence.RepoStorage;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


class MainIntegrationTest {

    private static Object callPrivateStatic(Class<?> cls, String method, Class<?>[] sig, Object[] args) throws Exception {
        Method m = cls.getDeclaredMethod(method, sig);
        m.setAccessible(true);
        return m.invoke(null, args);
    }

    @Test
    void integration_endToEnd_seeding_purging_and_persistence_roundtrip(@TempDir Path tmpHome) throws Exception {
        System.setProperty("user.home", tmpHome.toString());

        DataRepository repo = RepoStorage.loadOrNew();
        assertNotNull(repo);

        @SuppressWarnings("unchecked")
        List<Category> categories = (List<Category>) callPrivateStatic(
                Main.class,
                "buildCategories",
                new Class<?>[]{},
                new Object[]{}
        );

        assertNotNull(categories);
        assertTrue(categories.size() >= 1, "Expected at least one seeded category");

        for (Category c : categories) repo.addCategory(c);

        callPrivateStatic(
                Main.class,
                "seedCategoryAdmins",
                new Class<?>[]{DataRepository.class, List.class},
                new Object[]{repo, categories}
        );

        for (Category c : categories) {
            String expectedUsername = Service.BookingRequestService.categoryAdminUsername(c);

            User found = repo.getUsers().stream()
                    .filter(u -> u != null && u.getUsername() != null)
                    .filter(u -> u.getUsername().equalsIgnoreCase(expectedUsername))
                    .findFirst()
                    .orElse(null);

            assertNotNull(found, "Expected category-admin user for: " + c.getName());
            assertTrue(found instanceof Administrator, "Category-admin should be Administrator type");
        }

        int daysAhead = 7;
        callPrivateStatic(
                Main.class,
                "seedTimeSlots",
                new Class<?>[]{DataRepository.class, List.class, int.class},
                new Object[]{repo, categories, daysAhead}
        );

        LocalDate today = LocalDate.now();
        int nonFridays = 0;
        for (int d = 0; d < daysAhead; d++) {
            if (today.plusDays(d).getDayOfWeek() != DayOfWeek.FRIDAY) nonFridays++;
        }
        int expectedSlots = nonFridays * categories.size() * 8;
        assertEquals(expectedSlots, repo.getSlots().size(), "seedTimeSlots should generate expected slots count");

        Category removedCat = new Category("Doctor Appointment");
        repo.addCategory(removedCat);
        repo.addSlot(new TimeSlot(java.time.LocalDateTime.now().plusDays(1), 60, removedCat));

        int catsBefore = repo.getCategories().size();
        int slotsBefore = repo.getSlots().size();

        callPrivateStatic(
                Main.class,
                "purgeRemovedCategories",
                new Class<?>[]{DataRepository.class},
                new Object[]{repo}
        );

        assertTrue(catsBefore >= repo.getCategories().size());
        assertFalse(repo.getCategories().stream().anyMatch(c -> c != null && "Doctor Appointment".equalsIgnoreCase(c.getName())),
                "purgeRemovedCategories should remove 'Doctor Appointment'");

        assertTrue(slotsBefore >= repo.getSlots().size());
        assertFalse(repo.getSlots().stream().anyMatch(s ->
                        s != null && s.getCategory() != null
                                && s.getCategory().getName() != null
                                && "Doctor Appointment".equalsIgnoreCase(s.getCategory().getName())
                ),
                "Slots for removed categories should be purged");

        RepoStorage.save(repo);

        Path expectedFile = tmpHome.resolve(".Soft_Proj").resolve("data.json");
        assertTrue(Files.exists(expectedFile), "RepoStorage should create data.json under temp user.home");

        DataRepository loaded = RepoStorage.loadOrNew();
        assertNotNull(loaded);

        assertEquals(repo.getCategories().size(), loaded.getCategories().size(), "Categories should persist");
        assertEquals(repo.getUsers().size(), loaded.getUsers().size(), "Users should persist");
        assertEquals(repo.getSlots().size(), loaded.getSlots().size(), "Slots should persist");

        assertFalse(loaded.getCategories().stream().anyMatch(c -> c != null && "Doctor Appointment".equalsIgnoreCase(c.getName())));
    }
}