package Test;

import domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for domain.User
 */
class UserTest {

    @Test
    void fullConstructor_sets_all_fields_and_trims_email() {
        LocalDate dob = LocalDate.of(1990, 5, 20);
        User u = new User("First", "Last", "theuser", "pw123", dob, "  user@example.com  ");

        assertEquals("First", u.getFirstName());
        assertEquals("Last", u.getLastName());
        assertEquals("theuser", u.getUsername());
        assertEquals("pw123", u.getPassword());
        assertEquals(dob, u.getDateOfBirth());
        assertEquals("user@example.com", u.getEmail()); 
    }

    @Test
    void constructor_without_email_uses_empty_string() {
        LocalDate dob = LocalDate.of(1985, 1, 1);
        User u = new User("F", "L", "u2", "pw2", dob);

        assertEquals("F", u.getFirstName());
        assertEquals("L", u.getLastName());
        assertEquals("u2", u.getUsername());
        assertEquals("pw2", u.getPassword());
        assertEquals(dob, u.getDateOfBirth());
        assertEquals("", u.getEmail());
    }

    @Test
    void basic_username_password_constructor_sets_defaults() {
        User u = new User("simpleUser", "secret");

        assertEquals("simpleUser", u.getUsername());
        assertEquals("secret", u.getPassword());

        assertEquals("", u.getFirstName());
        assertEquals("", u.getLastName());
        assertNull(u.getDateOfBirth());
        assertEquals("", u.getEmail());
    }

    @Test
    void setEmail_trims_and_accepts_null() {
        User u = new User("a", "b");
        u.setEmail("  me@x.com  ");
        assertEquals("me@x.com", u.getEmail());

        u.setEmail(null);
        assertEquals("", u.getEmail(), "setEmail(null) should store empty string, not null");
    }
}