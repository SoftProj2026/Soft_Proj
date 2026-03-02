package domain;

import java.time.LocalDateTime;

/**
 * Represents a single audit log entry in the system.
 * <p>
 * The audit log is used by the admin to review important user actions such as:
 * <ul>
 *   <li>Sending a message (contact request) to a provider</li>
 *   <li>Confirming (booking) an appointment</li>
 *   <li>Cancelling an appointment</li>
 * </ul>
 * </p>
 *
 * <p>
 * Each event contains:
 * <ul>
 *   <li>a unique ID</li>
 *   <li>an event {@link Type}</li>
 *   <li>the actor username (who did it)</li>
 *   <li>a target (who/what was affected)</li>
 *   <li>free-text details</li>
 *   <li>a timestamp</li>
 * </ul>
 * </p>
 */
public class AuditEvent {

    /**
     * Supported audit event types.
     */
    public enum Type {
        /** A customer sent a message/contact request. */
        MESSAGE_SENT,
        /** A user confirmed (booked) an appointment. */
        APPOINTMENT_CONFIRMED,
        /** A user cancelled an appointment. */
        APPOINTMENT_CANCELLED
    }

    /** Counter for generating unique event IDs. */
    private static int counter = 1;

    private final int id;
    private final Type type;
    private final String actorUsername;
    private final String target;
    private final String details;
    private final LocalDateTime at;

    /**
     * Creates a new audit event with the current timestamp.
     *
     * @param type          event type
     * @param actorUsername username who performed the action (may be empty)
     * @param target        affected target (e.g., provider username, category name, etc.)
     * @param details       additional human-readable details
     */
    public AuditEvent(Type type, String actorUsername, String target, String details) {
        this.id = counter++;
        this.type = type;
        this.actorUsername = actorUsername != null ? actorUsername.trim() : "";
        this.target = target != null ? target.trim() : "";
        this.details = details != null ? details.trim() : "";
        this.at = LocalDateTime.now();
    }

    /**
     * Returns unique audit event ID.
     *
     * @return audit event id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the audit event type.
     *
     * @return event type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the username who performed the action.
     *
     * @return actor username
     */
    public String getActorUsername() {
        return actorUsername;
    }

    /**
     * Returns the affected target (meaning depends on {@link #getType()}).
     * <p>
     * Examples:
     * <ul>
     *   <li>provider username for {@link Type#MESSAGE_SENT}</li>
     *   <li>category name for appointment-related events</li>
     * </ul>
     * </p>
     *
     * @return target string
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns additional details for the event.
     *
     * @return details string
     */
    public String getDetails() {
        return details;
    }

    /**
     * Returns event timestamp.
     *
     * @return time of the event
     */
    public LocalDateTime getAt() {
        return at;
    }
}