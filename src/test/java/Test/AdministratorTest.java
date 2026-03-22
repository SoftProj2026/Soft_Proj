package Test;

import domain.Administrator;
import domain.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdministratorTest {

    @Test
    void administrator_inherits_user_and_has_username() {
        Administrator admin = new Administrator("admin", "pw");
        assertNotNull(admin);
        assertTrue(admin instanceof User, "Administrator should be an instance of User");
        assertEquals("admin", admin.getUsername(), "Username should be set by constructor");
    }
}