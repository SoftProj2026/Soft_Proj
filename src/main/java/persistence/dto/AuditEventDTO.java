package persistence.dto;

import java.time.LocalDateTime;

/**
 * Data transfer object representing an audit event for persistence/serialization.
 *
 * <p>This DTO is used inside repository snapshots to store audit log entries in a JSON-friendly structure.</p>
 */
public class AuditEventDTO {

    /** Audit event identifier as stored in the snapshot. */
    public int id;

    /**
     * Audit event type stored as text.
     *
     * <p>Common values include: {@code "MESSAGE_SENT"}, {@code "APPOINTMENT_CONFIRMED"},
     * {@code "APPOINTMENT_CANCELLED"}.</p>
     */
    public String type;

    /** Username of the actor who triggered the event. */
    public String actorUsername;

    /** Target identifier or label related to the event (best-effort, depends on event type). */
    public String target;

    /** Additional event details. */
    public String details;

    /** Timestamp when the event occurred. */
    public LocalDateTime at;
}