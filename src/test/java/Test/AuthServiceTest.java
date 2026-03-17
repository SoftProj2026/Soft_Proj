package Test;

import Service.AuthService;

import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void register_and_login_and_admin_behaviour() {
        DataRepository repo = new DataRepository();
        AuthService auth = new AuthService(repo);

        LocalDate dob = LocalDate.now().minusYears(25);
        AuthService.RegisterResult r = auth.register("F", "L", "john", "Password1!", dob, "john@example.com");
        assertEquals(AuthService.RegisterResult.SUCCESS, r);

        AuthService.RegisterResult r2 = auth.register("F", "L", "john", "Password1!", dob, "john2@example.com");
        assertEquals(AuthService.RegisterResult.USERNAME_TAKEN, r2);

        AuthService.RegisterResult r3 = auth.register("A","B","teen","pw", LocalDate.now().minusYears(16), "t@example.com");
        assertEquals(AuthService.RegisterResult.UNDER_18, r3);

        assertTrue(auth.login("john", "Password1!"));
        assertTrue(auth.isLoggedIn());
        assertNotNull(auth.getCurrentUser());
        assertEquals("john", auth.getCurrentUser().getUsername());

        auth.logout();
        assertFalse(auth.isLoggedIn());

        assertTrue(auth.loginAsUser("john"));

        repo.addUser(new domain.Administrator("admin", "pw"));
        AuthService auth2 = new AuthService(repo);
        assertTrue(auth2.loginAsAdmin());
    }
}