package Test;

import domain.ContactRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for domain.ContactRequest
 */
class ContactRequestTest {

    @Test
    void constructor_trims_inputs_and_sets_defaults() {
        LocalDateTime before = LocalDateTime.now();
        ContactRequest cr = new ContactRequest("  alice  ", "  providerX  ", "  Hello there  ");
        LocalDateTime after = LocalDateTime.now();

        assertEquals("alice", cr.getFromUsername());
        assertEquals("providerX", cr.getToProviderUsername());
        assertEquals("Hello there", cr.getMessage());

        assertNotNull(cr.getCreatedAt());
        assertFalse(cr.isRead(), "New contact requests should be unread by default");

        // timestamp within reasonable bounds
        assertFalse(cr.getCreatedAt().isBefore(before.minusSeconds(1)));
        assertFalse(cr.getCreatedAt().isAfter(after.plusSeconds(1)));

        assertTrue(cr.getId() > 0);
    }

    @Test
    void null_inputs_become_empty_strings_and_mark_read_changes_flag() {
        ContactRequest cr = new ContactRequest(null, null, null);

        assertEquals("", cr.getFromUsername());
        assertEquals("", cr.getToProviderUsername());
        assertEquals("", cr.getMessage());
        assertFalse(cr.isRead());

        cr.markRead();
        assertTrue(cr.isRead(), "markRead() should set the read flag to true");
    }

    @Test
    void ids_increment_between_instances() {
        ContactRequest c1 = new ContactRequest("u1", "p1", "m1");
        ContactRequest c2 = new ContactRequest("u2", "p2", "m2");
        assertTrue(c2.getId() > c1.getId(), "Subsequent instances should have increasing ids");
    }

    @Test
    void createdAt_is_recent() {
        ContactRequest cr = new ContactRequest("x", "y", "z");
        assertTrue(Duration.between(cr.getCreatedAt(), LocalDateTime.now()).abs().getSeconds() < 5,
                "createdAt timestamp should be recent");
    }
}