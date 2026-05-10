package Test;

import domain.Administrator;
import domain.Category;
import domain.Provider;
import domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import persistence.RepoStorage;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MainConstantsCoverageTest {

    private static Object invokePrivateStatic(
            String methodName,
            Class<?>[] parameterTypes,
            Object[] args
    ) throws Exception {
        Method method = mainapp.Main.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(null, args);
    }

    @SuppressWarnings("unchecked")
    private static List<Category> buildCategories() throws Exception {
        return (List<Category>) invokePrivateStatic(
                "buildCategories",
                new Class<?>[]{},
                new Object[]{}
        );
    }

    private static void bootstrap(DataRepository repo) throws Exception {
        Method method = mainapp.Main.class.getDeclaredMethod("bootstrap", DataRepository.class);
        method.setAccessible(true);
        method.invoke(null, repo);
    }

    @Test
    void bootstrap_nullRepo_doesNotThrow() {
        assertDoesNotThrow(() -> bootstrap(null));
    }

    @Test
    void bootstrap_emptyRepo_addsDefaultAdminProviderCategoriesAdminsAndSlots() throws Exception {
        DataRepository repo = new DataRepository();

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            bootstrap(repo);

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> u != null && "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername())));

            assertFalse(repo.getCategories().isEmpty());
            assertFalse(repo.getSlots().isEmpty());

            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), atLeastOnce());
        }
    }

    @Test
    void bootstrap_keepThisCategory_replacesItWithDefaultCategories() throws Exception {
        DataRepository repo = new DataRepository();
        repo.addCategory(new Category("Keep This"));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            bootstrap(repo);

            assertFalse(repo.getCategories().stream()
                    .anyMatch(c -> "Keep This".equalsIgnoreCase(c.getName())));

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));

            assertFalse(repo.getSlots().isEmpty());
        }
    }

    @Test
    void bootstrap_existingAdminAndProvider_doesNotDuplicateThem() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Existing Category"));
        repo.addUser(new Administrator("admin", "x"));
        repo.addProvider(new Provider(
                "qrbooking",
                "x",
                "Existing Provider",
                "",
                "",
                ""
        ));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            bootstrap(repo);

            long adminCount = repo.getUsers().stream()
                    .filter(u -> u != null && "admin".equalsIgnoreCase(u.getUsername()))
                    .count();

            long providerCount = repo.getProviders().stream()
                    .filter(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername()))
                    .count();

            assertEquals(1, adminCount);
            assertEquals(1, providerCount);
        }
    }

    @Test
    void bootstrap_existingData_keepsExistingCategoryAndAddsMissingDefaults() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Custom Category"));
        repo.addUser(new User("First", "Last", "user1", "pw", null, "u@test.com"));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            bootstrap(repo);

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Custom Category".equalsIgnoreCase(c.getName())));

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> "user1".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> "qrbooking".equalsIgnoreCase(p.getUsername())));
        }
    }

    @Test
    void buildCategories_returnsExpectedDefaultCategories() throws Exception {
        List<Category> categories = buildCategories();

        assertEquals(9, categories.size());
        assertTrue(categories.stream().anyMatch(c -> "Conference Hall".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Training Room".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Wedding Hall".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Birthday Venue".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Photography Studio".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Legal Consultation".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Apartment for rent".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Car for rent".equals(c.getName())));
        assertTrue(categories.stream().anyMatch(c -> "Meeting with a building contractor".equals(c.getName())));
    }

    @Test
    void seedTimeSlots_generatesEightSlotsPerNonFridayPerCategory() throws Exception {
        DataRepository repo = new DataRepository();
        List<Category> categories = buildCategories();

        invokePrivateStatic(
                "seedTimeSlots",
                new Class<?>[]{DataRepository.class, List.class, int.class},
                new Object[]{repo, categories, 7}
        );

        LocalDate today = LocalDate.now();
        int nonFridays = 0;

        for (int i = 0; i < 7; i++) {
            if (today.plusDays(i).getDayOfWeek() != DayOfWeek.FRIDAY) {
                nonFridays++;
            }
        }

        int expectedSlots = nonFridays * categories.size() * 8;

        assertEquals(expectedSlots, repo.getSlots().size());

        repo.getSlots().forEach(slot -> {
            assertFalse(slot.getStartDateTime().toLocalTime().isBefore(LocalTime.of(9, 0)));
            assertFalse(slot.getStartDateTime().toLocalTime().isAfter(LocalTime.of(16, 0)));
            assertEquals(60, slot.getDuration());
            assertNotEquals(DayOfWeek.FRIDAY, slot.getStartDateTime().getDayOfWeek());
        });
    }

    @Test
    void purgeRemovedCategories_removesDoctorAppointment() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Doctor Appointment"));
        repo.addCategory(new Category("Conference Hall"));

        invokePrivateStatic(
                "purgeRemovedCategories",
                new Class<?>[]{DataRepository.class},
                new Object[]{repo}
        );

        assertFalse(repo.getCategories().stream()
                .anyMatch(c -> "Doctor Appointment".equalsIgnoreCase(c.getName())));

        assertTrue(repo.getCategories().stream()
                .anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));
    }
}