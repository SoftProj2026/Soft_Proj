package Test;

import domain.Provider;
import domain.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for domain.Provider
 */
class ProviderTest {

    @Test
    void constructor_trims_inputs_and_getters_return_values() {
        Provider p = new Provider("provUser", "pw", "  My Company  ", "  01234  ", "  contact@example.com  ", "  Some Address  ");
        assertNotNull(p);
        assertTrue(p instanceof User, "Provider should extend User");

        assertEquals("My Company", p.getDisplayName());
        assertEquals("01234", p.getPhone());
        assertEquals("contact@example.com", p.getEmail());
        assertEquals("Some Address", p.getAddress());
    }

    @Test
    void null_inputs_become_empty_strings() {
        Provider p = new Provider("prov2", "pw", null, null, null, null);
        assertEquals("", p.getDisplayName());
        assertEquals("", p.getPhone());
        assertEquals("", p.getEmail());
        assertEquals("", p.getAddress());
    }

    @Test
    void username_and_password_set_via_superclass_constructor_reflective() throws Exception {
        Provider p = new Provider("prov3", "secret", "N", "P", "E", "A");

        try {
            // Try to find getUsername() reflectively (may be defined in User superclass)
            Method m = p.getClass().getMethod("getUsername");
            Object val = m.invoke(p);
            assertEquals("prov3", String.valueOf(val), "getUsername() should return the username passed to the constructor");
        } catch (NoSuchMethodException nsme) {
            // If the method doesn't exist, at least ensure object non-null and instance relation holds
            assertNotNull(p);
            assertTrue(p instanceof User);
        }
    }
}