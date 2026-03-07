package Test;

import Service.AuthService;

import domain.Administrator;
import persistence.DataRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LogoutTest {

    @Test
    void logout_clears_session() {
        DataRepository repo = new DataRepository();
        repo.addUser(new Administrator("admin", "Admin@123"));

        AuthService auth = new AuthService(repo);
        assertTrue(auth.login("admin", "Admin@123"));

        auth.logout();
        assertFalse(auth.isLoggedIn());
        assertNull(auth.getCurrentUser());
    }
}