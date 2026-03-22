package Test;

import domain.AuditEvent;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AuditEventTest {

    @Test
    void constructor_sets_fields_and_trims_inputs() {
        LocalDateTime before = LocalDateTime.now();
        AuditEvent e = new AuditEvent(AuditEvent.Type.MESSAGE_SENT, "  actor  ", "  target  ", "  some details  ");
        LocalDateTime after = LocalDateTime.now();

        assertEquals(AuditEvent.Type.MESSAGE_SENT, e.getType());
        assertEquals("actor", e.getActorUsername()); 
        assertEquals("target", e.getTarget());       
        assertEquals("some details", e.getDetails()); 

        assertNotNull(e.getAt());
        assertFalse(e.getAt().isBefore(before.minusSeconds(1)));
        assertFalse(e.getAt().isAfter(after.plusSeconds(1)));

        assertTrue(e.getId() > 0);
    }

    @Test
    void null_and_empty_inputs_handled_and_id_increments() {
        AuditEvent a1 = new AuditEvent(AuditEvent.Type.APPOINTMENT_CONFIRMED, null, null, null);
        AuditEvent a2 = new AuditEvent(AuditEvent.Type.APPOINTMENT_CANCELLED, "", " tgt ", " ");

        assertEquals("", a1.getActorUsername());
        assertEquals("", a1.getTarget());
        assertEquals("", a1.getDetails());

        assertEquals("", a2.getActorUsername()); 
        assertEquals("tgt", a2.getTarget());     
        assertEquals("", a2.getDetails());       

        assertTrue(a2.getId() > a1.getId(), "IDs should increment between instances");
    }

    @Test
    void timestamp_is_recent() {
        AuditEvent e = new AuditEvent(AuditEvent.Type.MESSAGE_SENT, "u", "t", "d");
        assertTrue(Duration.between(e.getAt(), LocalDateTime.now()).abs().getSeconds() < 5,
                "Event timestamp should be recent");
    }
}