package Test;

import persistence.DataRepository;
import service.AuthService;
import domain.User;
import domain.Administrator;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Service.AuthService
 */
public class AuthServiceTest {

    @Test
    void loginAsUser_finds_user_by_username_case_insensitive_and_handles_missing() {

        DataRepository repo = new DataRepository();

        User u = new User(
                "First",
                "Last",
                "john",
                "pw",
                LocalDate.of(1990, 1, 1),
                "j@x.com"
        );

        repo.addUser(u);

        AuthService svc = new AuthService(repo);

        assertFalse(svc.isLoggedIn());
        assertNull(svc.getCurrentUser());

        assertTrue(svc.loginAsUser("john"));
        assertTrue(svc.isLoggedIn());
        assertEquals("john", svc.getCurrentUser().getUsername());

        svc.logout();

        assertFalse(svc.isLoggedIn());

        assertTrue(svc.loginAsUser("JoHn"));
        assertEquals("john", svc.getCurrentUser().getUsername());

        svc.logout();

        assertFalse(svc.loginAsUser("noone"));
        assertNull(svc.getCurrentUser());

        assertFalse(svc.loginAsUser(null));
        assertFalse(svc.loginAsUser("   "));
    }

    @Test
    void login_with_username_and_password_success_and_failures() {

        DataRepository repo = new DataRepository();

        User u = new User(
                "F",
                "L",
                "alice",
                "secret",
                LocalDate.of(1985, 5, 5),
                "a@b.com"
        );

        repo.addUser(u);

        AuthService svc = new AuthService(repo);

        assertTrue(svc.login("alice", "secret"));
        assertTrue(svc.isLoggedIn());
        assertEquals("alice", svc.getCurrentUser().getUsername());

        svc.logout();

        assertFalse(svc.login("alice", "wrong"));
        assertFalse(svc.isLoggedIn());

        assertFalse(svc.login("unknown", "x"));

        assertFalse(svc.login(null, "pw"));
        assertFalse(svc.login("alice", null));

        assertFalse(svc.login("   ", "pw"));
    }

    @Test
    void loginAsAdmin_logs_in_when_admin_user_present() {

        DataRepository repo = new DataRepository();

        Administrator admin =
                new Administrator("admin", "pw");

        repo.addUser(admin);

        AuthService svc = new AuthService(repo);

        assertTrue(svc.loginAsAdmin());
        assertTrue(svc.isLoggedIn());
        assertEquals("admin",
                svc.getCurrentUser().getUsername());

        svc.logout();

        // no admin
        DataRepository repo2 =
                new DataRepository();

        AuthService svc2 =
                new AuthService(repo2);

        assertFalse(svc2.loginAsAdmin());
    }

    @Test
    void legacy_register_overload_returns_invalid_input() {

        DataRepository repo =
                new DataRepository();

        AuthService svc =
                new AuthService(repo);

        AuthService.RegisterResult r =
                svc.register(
                        "F",
                        "L",
                        "u1",
                        "pw",
                        LocalDate.of(1990, 1, 1)
                );

        assertEquals(
                AuthService.RegisterResult.INVALID_INPUT,
                r
        );
    }

    @Test
    void register_success_adds_user_and_allows_login() {

        DataRepository repo =
                new DataRepository();

        AuthService svc =
                new AuthService(repo);

        LocalDate dob =
                LocalDate.now().minusYears(25);

        AuthService.RegisterResult res =
                svc.register(
                        "First",
                        "Last",
                        "newuser",
                        "pwd",
                        dob,
                        "me@example.com"
                );

        assertEquals(
                AuthService.RegisterResult.SUCCESS,
                res
        );

        assertTrue(
                svc.login("newuser", "pwd")
        );

        assertEquals(
                "newuser",
                svc.getCurrentUser().getUsername()
        );
    }

    @Test
    void register_username_taken_is_detected_case_insensitively() {

        DataRepository repo =
                new DataRepository();

        User existing =
                new User(
                        "T",
                        "T",
                        "Tom",
                        "pw",
                        LocalDate.of(1990, 1, 1),
                        "t@x.com"
                );

        repo.addUser(existing);

        AuthService svc =
                new AuthService(repo);

        LocalDate dob =
                LocalDate.now().minusYears(30);

        AuthService.RegisterResult res =
                svc.register(
                        "X",
                        "Y",
                        "tom",
                        "pw2",
                        dob,
                        "x@y.com"
                );

        assertEquals(
                AuthService.RegisterResult.USERNAME_TAKEN,
                res
        );
    }

    @Test
    void register_under_18_is_rejected() {

        DataRepository repo =
                new DataRepository();

        AuthService svc =
                new AuthService(repo);

        LocalDate under18 =
                LocalDate.now().minusYears(17);

        AuthService.RegisterResult res =
                svc.register(
                        "F",
                        "L",
                        "young",
                        "pw",
                        under18,
                        "y@x.com"
                );

        assertEquals(
                AuthService.RegisterResult.UNDER_18,
                res
        );
    }

    @Test
    void session_management_behaviour() {

        DataRepository repo =
                new DataRepository();

        User u =
                new User(
                        "A",
                        "B",
                        "sam",
                        "pw",
                        LocalDate.of(1990, 1, 1),
                        "s@x.com"
                );

        repo.addUser(u);

        AuthService svc =
                new AuthService(repo);

        assertNull(
                svc.getCurrentUser()
        );

        assertFalse(
                svc.isLoggedIn()
        );

        assertTrue(
                svc.login("sam", "pw")
        );

        assertNotNull(
                svc.getCurrentUser()
        );

        assertTrue(
                svc.isLoggedIn()
        );

        svc.logout();

        assertNull(
                svc.getCurrentUser()
        );

        assertFalse(
                svc.isLoggedIn()
        );
    }
}