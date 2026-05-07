package Test;

import persistence.DataRepository;
import domain.Category;
import domain.User;
import domain.TimeSlot;
import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Assumptions;

class MainTest {
    @BeforeAll
    static void skipIfHeadless() {
        Assumptions.assumeFalse(
            java.awt.GraphicsEnvironment.isHeadless(),
            "Skipping GUI tests in CI headless mode"
        );
    }
    @Test
    void buildCategories_and_seedCategoryAdmins_adds_admin_users_for_each_category() throws Exception {
        Class<?> mainClass = Class.forName("mainApp.Main");

        Method buildCategories = mainClass.getDeclaredMethod("buildCategories");
        buildCategories.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Category> categories = (List<Category>) buildCategories.invoke(null);

        assertNotNull(categories);
        assertTrue(categories.size() > 0, "Expected buildCategories() to return at least one category");

        DataRepository repo = new DataRepository();

        Method seedCategoryAdmins = mainClass.getDeclaredMethod("seedCategoryAdmins", DataRepository.class, List.class);
        seedCategoryAdmins.setAccessible(true);
        seedCategoryAdmins.invoke(null, repo, categories);

        Class<?> brsClass = Class.forName("Service.BookingRequestService");
        Method catAdminName = brsClass.getDeclaredMethod("categoryAdminUsername", Category.class);

        for (Category c : categories) {
            String expected = String.valueOf(catAdminName.invoke(null, c));
            boolean found = repo.getUsers().stream()
                    .map(u -> {
                        try { return u.getUsername(); } catch (Throwable t) { return null; }
                    })
                    .anyMatch(un -> expected.equals(un));
            assertTrue(found, "Expected category-admin user for category: " + c.getName());
        }
    }

    @Test
    void seedTimeSlots_generates_slots_for_days_skipping_friday() throws Exception {
        Class<?> mainClass = Class.forName("mainApp.Main");

        Method buildCategories = mainClass.getDeclaredMethod("buildCategories");
        buildCategories.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<Category> categories = (List<Category>) buildCategories.invoke(null);

        DataRepository repo = new DataRepository();

        Method seedTimeSlots = mainClass.getDeclaredMethod("seedTimeSlots", DataRepository.class, List.class, int.class);
        seedTimeSlots.setAccessible(true);

        int daysAhead = 7;
        seedTimeSlots.invoke(null, repo, categories, daysAhead);

        LocalDate today = LocalDate.now();
        int nonFridays = 0;
        for (int d = 0; d < daysAhead; d++) {
            LocalDate date = today.plusDays(d);
            if (date.getDayOfWeek() != DayOfWeek.FRIDAY) nonFridays++;
        }

        int categoriesCount = categories.size();
        int slotsPerDayPerCategory = 8; 
        int expectedSlots = nonFridays * categoriesCount * slotsPerDayPerCategory;

        int actual = repo.getSlots().size();
        assertEquals(expectedSlots, actual, "Expected seedTimeSlots to add the correct number of slots");
    }

    @Test
    void purgeRemovedCategories_handles_null_repo_and_noop_when_nothing_removed() throws Exception {
        Class<?> mainClass = Class.forName("mainApp.Main");
        Method purge = mainClass.getDeclaredMethod("purgeRemovedCategories", DataRepository.class);
        purge.setAccessible(true);

        purge.invoke(null, new Object[] { null });

        DataRepository repo = new DataRepository();
        repo.addCategory(new Category("Keep This Category"));
        int before = repo.getCategories().size();

        purge.invoke(null, repo);
        int after = repo.getCategories().size();

        assertEquals(before, after, "When no matching categories exist, purgeRemovedCategories should not remove categories");
    }

    @Test
    void purgeRemovedCategories_removes_matched_categories() throws Exception {
        Class<?> mainClass = Class.forName("mainApp.Main");
        Method purge = mainClass.getDeclaredMethod("purgeRemovedCategories", DataRepository.class);
        purge.setAccessible(true);

        DataRepository repo = new DataRepository();
        repo.addCategory(new Category("Doctor Appointment"));
        repo.addCategory(new Category("Keep This"));

        assertTrue(repo.getCategories().stream().anyMatch(c -> "Doctor Appointment".equals(c.getName())));

        purge.invoke(null, repo);

        boolean stillThere = repo.getCategories().stream().anyMatch(c -> "Doctor Appointment".equals(c.getName()));
        assertFalse(stillThere, "Expected 'Doctor Appointment' category to be removed by purgeRemovedCategories");

        assertTrue(repo.getCategories().stream().anyMatch(c -> "Keep This".equals(c.getName())));
    }
}