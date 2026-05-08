package Test;

import domain.Administrator;
import domain.Category;
import domain.Provider;
import domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import persistence.DataRepository;
import persistence.RepoStorage;
import presentation.LoginFrame;
import presentation.UITheme;
import service.BookingRequestService;

import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MainTest {

    private static Class<?> mainClass() throws Exception {
        return Class.forName("mainapp.Main");
    }

    private static Object invokePrivateStatic(
            String methodName,
            Class<?>[] parameterTypes,
            Object[] args
    ) throws Exception {
        Method method = mainClass().getDeclaredMethod(methodName, parameterTypes);
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

    @Test
    void buildCategories_returns_expected_application_categories() throws Exception {
        List<Category> categories = buildCategories();

        assertNotNull(categories);
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
    void seedCategoryAdmins_adds_admin_user_for_each_category() throws Exception {
        DataRepository repo = new DataRepository();
        List<Category> categories = buildCategories();

        invokePrivateStatic(
                "seedCategoryAdmins",
                new Class<?>[]{DataRepository.class, List.class},
                new Object[]{repo, categories}
        );

        assertEquals(categories.size(), repo.getUsers().size());

        for (Category c : categories) {
            String expectedUsername = BookingRequestService.categoryAdminUsername(c);

            boolean found = repo.getUsers().stream()
                    .anyMatch(u -> expectedUsername.equals(u.getUsername()));

            assertTrue(found, "Missing category admin for: " + c.getName());
        }
    }

    @Test
    void seedTimeSlots_generates_slots_for_non_friday_days_only() throws Exception {
        DataRepository repo = new DataRepository();
        List<Category> categories = buildCategories();

        int daysAhead = 7;

        invokePrivateStatic(
                "seedTimeSlots",
                new Class<?>[]{DataRepository.class, List.class, int.class},
                new Object[]{repo, categories, daysAhead}
        );

        LocalDate today = LocalDate.now();
        int nonFridays = 0;

        for (int i = 0; i < daysAhead; i++) {
            if (today.plusDays(i).getDayOfWeek() != DayOfWeek.FRIDAY) {
                nonFridays++;
            }
        }

        int expectedSlots = nonFridays * categories.size() * 8;

        assertEquals(expectedSlots, repo.getSlots().size());

        repo.getSlots().forEach(slot -> {
            assertNotNull(slot.getStartDateTime());
            assertNotNull(slot.getEndDateTime());

            assertFalse(slot.getStartDateTime().toLocalTime().isBefore(LocalTime.of(9, 0)));
            assertFalse(slot.getStartDateTime().toLocalTime().isAfter(LocalTime.of(16, 0)));

            assertEquals(60, java.time.Duration.between(
                    slot.getStartDateTime(),
                    slot.getEndDateTime()
            ).toMinutes());

            assertNotEquals(DayOfWeek.FRIDAY, slot.getStartDateTime().getDayOfWeek());
        });
    }

    @Test
    void seedTimeSlots_withZeroDays_addsNoSlots() throws Exception {
        DataRepository repo = new DataRepository();
        List<Category> categories = buildCategories();

        invokePrivateStatic(
                "seedTimeSlots",
                new Class<?>[]{DataRepository.class, List.class, int.class},
                new Object[]{repo, categories, 0}
        );

        assertTrue(repo.getSlots().isEmpty());
    }

    @Test
    void purgeRemovedCategories_handlesNullRepoWithoutThrowing() {
        assertDoesNotThrow(() -> invokePrivateStatic(
                "purgeRemovedCategories",
                new Class<?>[]{DataRepository.class},
                new Object[]{null}
        ));
    }

    @Test
    void purgeRemovedCategories_removesDoctorAppointmentOnly() throws Exception {
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

    @Test
    void purgeRemovedCategories_whenNoRemovedCategory_keepsExistingCategories() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Conference Hall"));
        repo.addCategory(new Category("Training Room"));

        invokePrivateStatic(
                "purgeRemovedCategories",
                new Class<?>[]{DataRepository.class},
                new Object[]{repo}
        );

        assertEquals(2, repo.getCategories().size());

        assertTrue(repo.getCategories().stream()
                .anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));

        assertTrue(repo.getCategories().stream()
                .anyMatch(c -> "Training Room".equalsIgnoreCase(c.getName())));
    }

    @Test
    void ensureBigAdminAndProviderExist_handlesNullRepoWithoutThrowing() {
        assertDoesNotThrow(() -> invokePrivateStatic(
                "ensureBigAdminAndProviderExist",
                new Class<?>[]{DataRepository.class},
                new Object[]{null}
        ));
    }

    @Test
    void ensureBigAdminAndProviderExist_addsMissingAdminAndProvider() throws Exception {
        DataRepository repo = new DataRepository();

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            invokePrivateStatic(
                    "ensureBigAdminAndProviderExist",
                    new Class<?>[]{DataRepository.class},
                    new Object[]{repo}
            );

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> "qrbooking".equalsIgnoreCase(p.getUsername())));

            storage.verify(() -> RepoStorage.save(repo), times(1));
        }
    }

    @Test
    void ensureBigAdminAndProviderExist_doesNotDuplicateExistingAdminOrProvider() throws Exception {
        DataRepository repo = new DataRepository();

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

            invokePrivateStatic(
                    "ensureBigAdminAndProviderExist",
                    new Class<?>[]{DataRepository.class},
                    new Object[]{repo}
            );

            long adminCount = repo.getUsers().stream()
                    .filter(u -> u != null && "admin".equalsIgnoreCase(u.getUsername()))
                    .count();

            long providerCount = repo.getProviders().stream()
                    .filter(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername()))
                    .count();

            assertEquals(1, adminCount);
            assertEquals(1, providerCount);

            storage.verify(() -> RepoStorage.save(repo), times(1));
        }
    }

    @Test
    void ensureBigAdminAndProviderExist_whenAdminExistsButProviderMissing_addsOnlyProvider() throws Exception {
        DataRepository repo = new DataRepository();

        repo.addUser(new Administrator("admin", "x"));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            invokePrivateStatic(
                    "ensureBigAdminAndProviderExist",
                    new Class<?>[]{DataRepository.class},
                    new Object[]{repo}
            );

            long adminCount = repo.getUsers().stream()
                    .filter(u -> u != null && "admin".equalsIgnoreCase(u.getUsername()))
                    .count();

            long providerCount = repo.getProviders().stream()
                    .filter(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername()))
                    .count();

            assertEquals(1, adminCount);
            assertEquals(1, providerCount);

            storage.verify(() -> RepoStorage.save(repo), times(1));
        }
    }

    @Test
    void ensureBigAdminAndProviderExist_whenProviderExistsButAdminMissing_addsOnlyAdmin() throws Exception {
        DataRepository repo = new DataRepository();

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

            invokePrivateStatic(
                    "ensureBigAdminAndProviderExist",
                    new Class<?>[]{DataRepository.class},
                    new Object[]{repo}
            );

            long adminCount = repo.getUsers().stream()
                    .filter(u -> u != null && "admin".equalsIgnoreCase(u.getUsername()))
                    .count();

            long providerCount = repo.getProviders().stream()
                    .filter(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername()))
                    .count();

            assertEquals(1, adminCount);
            assertEquals(1, providerCount);

            storage.verify(() -> RepoStorage.save(repo), times(1));
        }
    }

    @Test
    void ensureBigAdminAndProviderExist_ignoresNullAndNullUsernameEntries() throws Exception {
        DataRepository repo = new DataRepository();

        repo.getUsers().add(null);
        repo.addUser(new User("F", "L", null, "pw", null, ""));

        repo.getProviders().add(null);
        repo.addProvider(new Provider(
                null,
                "pw",
                "No Username Provider",
                "",
                "",
                ""
        ));

        try (MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class)) {
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            invokePrivateStatic(
                    "ensureBigAdminAndProviderExist",
                    new Class<?>[]{DataRepository.class},
                    new Object[]{repo}
            );

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> u != null && "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> p != null && "qrbooking".equalsIgnoreCase(p.getUsername())));

            storage.verify(() -> RepoStorage.save(repo), times(1));
        }
    }

    @Test
    void main_whenRepoEmpty_seedsDataAndLaunchesLoginFrame() {
        DataRepository repo = new DataRepository();

        try (MockedStatic<UITheme> theme = mockStatic(UITheme.class);
             MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class);
             MockedConstruction<LoginFrame> loginFrames =
                     mockConstruction(LoginFrame.class, (mock, context) ->
                             doNothing().when(mock).setVisible(anyBoolean())
                     )) {

            theme.when(UITheme::apply).thenAnswer(inv -> null);
            storage.when(RepoStorage::loadOrNew).thenReturn(repo);
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            assertDoesNotThrow(() -> mainapp.Main.main(new String[0]));

            assertFalse(repo.getCategories().isEmpty(), "main should seed categories");
            assertFalse(repo.getSlots().isEmpty(), "main should seed slots");

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> "qrbooking".equalsIgnoreCase(p.getUsername())));

            assertEquals(1, loginFrames.constructed().size());
            verify(loginFrames.constructed().get(0), times(1)).setVisible(true);

            theme.verify(UITheme::apply, times(1));
            storage.verify(RepoStorage::loadOrNew, times(1));
            storage.verify(() -> RepoStorage.save(any(DataRepository.class)), atLeastOnce());
        }
    }

    @Test
    void main_whenOnlyKeepThisCategory_replacesItWithApplicationCategories() {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Keep This"));

        try (MockedStatic<UITheme> theme = mockStatic(UITheme.class);
             MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class);
             MockedConstruction<LoginFrame> loginFrames =
                     mockConstruction(LoginFrame.class, (mock, context) ->
                             doNothing().when(mock).setVisible(anyBoolean())
                     )) {

            theme.when(UITheme::apply).thenAnswer(inv -> null);
            storage.when(RepoStorage::loadOrNew).thenReturn(repo);
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            assertDoesNotThrow(() -> mainapp.Main.main(new String[0]));

            assertFalse(repo.getCategories().stream()
                    .anyMatch(c -> "Keep This".equalsIgnoreCase(c.getName())));

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Conference Hall".equalsIgnoreCase(c.getName())));

            assertFalse(repo.getSlots().isEmpty());

            assertEquals(1, loginFrames.constructed().size());
            verify(loginFrames.constructed().get(0), times(1)).setVisible(true);
        }
    }

    @Test
    void main_whenRepoAlreadyHasData_doesNotClearExistingCategories() {
        DataRepository repo = new DataRepository();

        Category custom = new Category("Custom Category");
        repo.addCategory(custom);
        repo.addUser(new User("first", "last", "existingUser", "pw", null, "x@y.com"));

        try (MockedStatic<UITheme> theme = mockStatic(UITheme.class);
             MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class);
             MockedConstruction<LoginFrame> loginFrames =
                     mockConstruction(LoginFrame.class, (mock, context) ->
                             doNothing().when(mock).setVisible(anyBoolean())
                     )) {

            theme.when(UITheme::apply).thenAnswer(inv -> null);
            storage.when(RepoStorage::loadOrNew).thenReturn(repo);
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            assertDoesNotThrow(() -> mainapp.Main.main(new String[0]));

            assertTrue(repo.getCategories().stream()
                    .anyMatch(c -> "Custom Category".equals(c.getName())));

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> "existingUser".equals(u.getUsername())));

            assertTrue(repo.getUsers().stream()
                    .anyMatch(u -> "admin".equalsIgnoreCase(u.getUsername())));

            assertTrue(repo.getProviders().stream()
                    .anyMatch(p -> "qrbooking".equalsIgnoreCase(p.getUsername())));

            assertEquals(1, loginFrames.constructed().size());
            verify(loginFrames.constructed().get(0), times(1)).setVisible(true);
        }
    }

    @Test
    void main_acceptsNonEmptyArgs() {
        DataRepository repo = new DataRepository();

        repo.addCategory(new Category("Existing Category"));

        try (MockedStatic<UITheme> theme = mockStatic(UITheme.class);
             MockedStatic<RepoStorage> storage = mockStatic(RepoStorage.class);
             MockedConstruction<LoginFrame> loginFrames =
                     mockConstruction(LoginFrame.class, (mock, context) ->
                             doNothing().when(mock).setVisible(anyBoolean())
                     )) {

            theme.when(UITheme::apply).thenAnswer(inv -> null);
            storage.when(RepoStorage::loadOrNew).thenReturn(repo);
            storage.when(() -> RepoStorage.save(any(DataRepository.class))).thenAnswer(inv -> null);

            assertDoesNotThrow(() -> mainapp.Main.main(new String[]{"anything"}));

            assertEquals(1, loginFrames.constructed().size());
            verify(loginFrames.constructed().get(0), times(1)).setVisible(true);
        }
    }
}