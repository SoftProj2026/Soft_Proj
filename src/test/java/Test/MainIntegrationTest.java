package Test;

import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistence.DataRepository;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;
class MainIntegrationTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    private static Object invokePrivateStatic(Class<?> cls, String method, Class<?>[] paramTypes, Object[] args) throws Exception {
        Method m = cls.getDeclaredMethod(method, paramTypes);
        m.setAccessible(true);
        return m.invoke(null, args);
    }

    @Test
    void main_bootstrap_helpers_integration_should_seed_and_purge_correctly(@TempDir Path tmpHome) throws Exception {
        System.setProperty("user.home", tmpHome.toString());

        Class<?> mainClass = Class.forName("MainApp.Main");

        @SuppressWarnings("unchecked")
        List<Category> categories = (List<Category>) invokePrivateStatic(
                mainClass,
                "buildCategories",
                new Class<?>[]{},
                new Object[]{}
        );

        assertNotNull(categories);
        assertTrue(categories.size() >= 1);

        assertTrue(categories.stream().anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Photography Studio".equalsIgnoreCase(c.getName())));

        DataRepository repo = new DataRepository();
        for (Category c : categories) repo.addCategory(c);

        invokePrivateStatic(
                mainClass,
                "seedCategoryAdmins",
                new Class<?>[]{DataRepository.class, List.class},
                new Object[]{repo, categories}
        );

        assertFalse(repo.getUsers().isEmpty());
        assertTrue(repo.getUsers().stream().allMatch(u -> u != null && u.getUsername() != null));

        int daysAhead = 7;

        invokePrivateStatic(
                mainClass,
                "seedTimeSlots",
                new Class<?>[]{DataRepository.class, List.class, int.class},
                new Object[]{repo, categories, daysAhead}
        );

        assertFalse(repo.getSlots().isEmpty());

        for (TimeSlot s : repo.getSlots()) {
            assertNotNull(s);
            assertNotNull(s.getStartDateTime());
            assertNotNull(s.getEndDateTime());
            LocalTime st = s.getStartDateTime().toLocalTime();
            assertFalse(st.isBefore(LocalTime.of(9, 0)));
            assertFalse(st.isAfter(LocalTime.of(16, 0)));

            long mins = java.time.Duration.between(s.getStartDateTime(), s.getEndDateTime()).toMinutes();
            assertEquals(60, mins);
        }

        LocalDate today = LocalDate.now();
        int nonFridays = 0;
        for (int d = 0; d < daysAhead; d++) {
            if (today.plusDays(d).getDayOfWeek() != DayOfWeek.FRIDAY) nonFridays++;
        }

        int slotsPerDayPerCategory = 8;
        int expected = nonFridays * categories.size() * slotsPerDayPerCategory;
        assertEquals(expected, repo.getSlots().size(), "seedTimeSlots should generate the correct number of slots");

        Category removable = new Category("Doctor Appointment");
        repo.addCategory(removable);

        TimeSlot removableSlot = new TimeSlot(LocalDateTime.now().plusDays(2).withHour(10).withMinute(0), 60, removable);
        repo.addSlot(removableSlot);

        User u = new User("testuser", "pw");
        repo.addUser(u);

        invokePrivateStatic(
                mainClass,
                "purgeRemovedCategories",
                new Class<?>[]{DataRepository.class},
                new Object[]{repo}
        );

        assertFalse(repo.getCategories().stream().anyMatch(c -> c != null && "Doctor Appointment".equalsIgnoreCase(c.getName())),
                "Doctor Appointment should be purged from categories");

        assertFalse(repo.getSlots().stream().anyMatch(s -> s != null && s.getCategory() != null
                        && s.getCategory().getName() != null
                        && "Doctor Appointment".equalsIgnoreCase(s.getCategory().getName())),
                "Slots for removed category should be purged");

        Path storageDir = tmpHome.resolve(".Soft_Proj");
        Files.createDirectories(storageDir);
        assertTrue(Files.exists(storageDir));
    }
}