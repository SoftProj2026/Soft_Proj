package domain;

import java.time.LocalDateTime;

/**
 * Represents a single audit log entry in the system.
 *
 * <p>The audit log is used by administrators to review important actions performed by users,
 * such as sending messages, confirming appointments, and cancelling appointments.</p>
 *
 * <p>Each event contains a unique id, an event {@link Type}, actor username, target identifier,
 * free-text details, and a timestamp.</p>
 *
 * @author s12219530-cpu (remaa)
 * @version 1.0
 */
public class AuditEvent {

    /**
     * Supported audit event types.
     *
     * @author Qussaialaw
     * @version 1.0
     */
    public enum Type {

        /**
         * A customer sent a message/contact request.
         */
        MESSAGE_SENT,

        /**
         * A user confirmed (booked) an appointment.
         */
        APPOINTMENT_CONFIRMED,

        /**
         * A user cancelled an appointment.
         */
        APPOINTMENT_CANCELLED
    }

    /**
     * Counter for generating unique event identifiers.
     */
    private static int counter = 1;

    /**
     * Unique audit event identifier.
     */
    private final int id;

    /**
     * The event type.
     */
    private final Type type;

    /**
     * Username of the actor who performed the action.
     */
    private final String actorUsername;

    /**
     * Target that was affected by the action (meaning depends on {@link #type}).
     */
    private final String target;

    /**
     * Additional human-readable details.
     */
    private final String details;

    /**
     * Timestamp of the event.
     */
    private final LocalDateTime at;

    /**
     * Creates a new audit event with the current timestamp.
     *
     * @param type          the audit event type
     * @param actorUsername the username of the actor who performed the action (may be {@code null} or empty)
     * @param target        the affected target (may be {@code null} or empty)
     * @param details       additional event details (may be {@code null} or empty)
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
     * Returns the unique audit event id.
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
     * Returns the username of the actor who performed the action.
     *
     * @return actor username (may be empty)
     */
    public String getActorUsername() {
        return actorUsername;
    }

    /**
     * Returns the affected target (meaning depends on {@link #getType()}).
     *
     * @return target string (may be empty)
     */
    public String getTarget() {
        return target;
    }

    /**
     * Returns additional details describing the event.
     *
     * @return details string (may be empty)
     */
    public String getDetails() {
        return details;
    }

    /**
     * Returns the timestamp when the event occurred.
     *
     * @return event timestamp
     */
    public LocalDateTime getAt() {
        return at;
    }
}