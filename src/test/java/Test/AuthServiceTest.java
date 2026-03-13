package Test;

import domain.Administrator;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import Service.AuthService;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    void login_validCredentials_succeeds() {
        DataRepository repo = new DataRepository();
        repo.addUser(new User("John", "Doe", "john", "Pass@1234", LocalDate.of(2000, 1, 1), "john@test.com"));

        AuthService auth = new AuthService(repo);

        assertTrue(auth.login("john", "Pass@1234"));
        assertNotNull(auth.getCurrentUser());
        assertEquals("john", auth.getCurrentUser().getUsername());
    }

    @Test
    void login_invalidCredentials_fails() {
        DataRepository repo = new DataRepository();
        repo.addUser(new User("John", "Doe", "john", "Pass@1234", LocalDate.of(2000, 1, 1), "john@test.com"));

        AuthService auth = new AuthService(repo);

        assertFalse(auth.login("john", "wrong"));
        assertNull(auth.getCurrentUser());
    }

    @Test
    void loginAsAdmin_succeedsWhenAdminUserExists() {
        DataRepository repo = new DataRepository();
        repo.addUser(new Administrator("admin", "Admin@123"));

        AuthService auth = new AuthService(repo);

        assertTrue(auth.loginAsAdmin());
        assertNotNull(auth.getCurrentUser());
        assertEquals("admin", auth.getCurrentUser().getUsername());
    }

    @Test
    void register_under18_rejected() {
        DataRepository repo = new DataRepository();
        AuthService auth = new AuthService(repo);

        AuthService.RegisterResult r = auth.register(
                "A", "B", "u1", "Pass@1234",
                LocalDate.now().minusYears(17),
                "u1@test.com"
        );

        assertEquals(AuthService.RegisterResult.UNDER_18, r);
    }

    @Test
    void register_duplicateUsername_rejected() {
        DataRepository repo = new DataRepository();
        repo.addUser(new User("A", "B", "u1", "p", LocalDate.of(2000, 1, 1), "x@test.com"));

        AuthService auth = new AuthService(repo);

        AuthService.RegisterResult r = auth.register(
                "A", "B", "u1", "Pass@1234",
                LocalDate.of(2000, 1, 1),
                "u1@test.com"
        );

        assertEquals(AuthService.RegisterResult.USERNAME_TAKEN, r);
    }

    @Test
    void register_invalidEmail_rejected() {
        DataRepository repo = new DataRepository();
        AuthService auth = new AuthService(repo);

        AuthService.RegisterResult r = auth.register(
                "A", "B", "u1", "Pass@1234",
                LocalDate.of(2000, 1, 1),
                "invalidEmail"
        );

        assertEquals(AuthService.RegisterResult.INVALID_INPUT, r);
    }
}