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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MainBootstrapCoverageTest {

    private void callBootstrap(DataRepository repo) throws Exception {
        Method m = mainapp.Main.class.getDeclaredMethod("bootstrap", DataRepository.class);
        m.setAccessible(true);
        m.invoke(null, repo);
    }

    @Test
    void bootstrap_emptyRepo_addsAdminProviderCategoriesAdminsAndSlots() throws Exception {
        DataRepository repo = new DataRepository();

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            callBootstrap(repo);

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> u != null && "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername())));

            assertFalse(repo.getCategories().isEmpty());
            assertFalse(repo.getSlots().isEmpty());

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));

            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), atLeastOnce());
        }
    }

    @Test
    void bootstrap_keepThisCategory_replacesItWithRealCategories() throws Exception {
        DataRepository repo = new DataRepository();
        repo.addCategory(new Category("Keep This"));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            callBootstrap(repo);

            assertFalse(repo.getCategories().stream()
                    .anyMatch(c -> "Keep This".equalsIgnoreCase(c.getName())));

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));

            assertFalse(repo.getSlots().isEmpty());
        }
    }

    @Test
    void bootstrap_existingData_keepsExistingCategoryAndAddsMissingDefaults() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Custom Category"));
        repo.addUser(new User("F", "L", "user1", "pw", null, "u@test.com"));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            callBootstrap(repo);

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
    void bootstrap_existingAdminAndProvider_doesNotDuplicateThem() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Custom Category"));
        repo.addUser(new Administrator("admin", "x"));
        repo.addProvider(new Provider(
                "qrbooking",
                "x",
                "QR Booking",
                "",
                "",
                ""
        ));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            callBootstrap(repo);

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
    void bootstrap_removedDoctorAppointmentCategory_isPurged() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Doctor Appointment"));
        repo.addCategory(new Category("Custom Category"));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            callBootstrap(repo);

            assertFalse(repo.getCategories().stream()
                    .anyMatch(c -> "Doctor Appointment".equalsIgnoreCase(c.getName())));

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Custom Category".equalsIgnoreCase(c.getName())));
        }
    }
}