package Test;


import domain.Administrator;
import persistence.DataRepository;
import org.junit.jupiter.api.Test;
import Service.AuthService;
import Service.BookingEmailReminderService ;

import static org.junit.jupiter.api.Assertions.*;

public class AuthServiceTest {

    @Test
    void admin_login_success() {
        DataRepository repo = new DataRepository();
        repo.addUser(new Administrator("admin", "Admin@123"));

        AuthService auth = new AuthService(repo);

        assertTrue(auth.login("admin", "Admin@123"));
        assertTrue(auth.isLoggedIn());
    }

    @Test
    void login_invalid_fails() {
        DataRepository repo = new DataRepository();
        repo.addUser(new Administrator("admin", "Admin@123"));

        AuthService auth = new AuthService(repo);

        assertFalse(auth.login("admin", "wrong"));
        assertFalse(auth.isLoggedIn());
    }
}
