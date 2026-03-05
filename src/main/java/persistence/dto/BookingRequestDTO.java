package persistence.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a serializable snapshot of a booking request.
 *
 * <p>This class is used for persistence/serialization (e.g., saving to JSON) and for restoring
 * {@code BookingRequest} domain objects when loading a {@code RepoSnapshot}.</p>
 *
 * <p>References to domain objects are represented using stable identifiers:
 * <ul>
 *   <li>User is represented by {@link #requesterUsername}</li>
 *   <li>Time slot is represented by {@link #slotStart} and {@link #categoryName}</li>
 *   <li>Status is stored as a string (typically the enum {@code name()})</li>
 * </ul>
 * </p>
 */
public class BookingRequestDTO {

    /**
     * Unique identifier of the booking request.
     */
    public int id;

    /**
     * Username of the user who submitted the booking request.
     */
    public String requesterUsername;

    /**
     * Start date-time of the requested time slot.
     */
    public LocalDateTime slotStart;

    /**
     * Category name associated with the requested slot.
     */
    public String categoryName;

    /**
     * Requested duration, in minutes.
     */
    public int durationInMinutes;

    /**
     * Number of participants included in the booking request.
     */
    public int participants;

    /**
     * Username of the category administrator responsible for handling the request.
     */
    public String categoryAdminUsername;

    /**
     * Booking request status as a string (typically the enum {@code name()}).
     */
    public String status;

    /**
     * Timestamp indicating when the request was created.
     */
    public LocalDateTime createdAt;

    /**
     * Timestamp indicating when the category administrator made a decision.
     */
    public LocalDateTime categoryDecisionAt;

    /**
     * Timestamp indicating when the (super/big) administrator made a decision.
     */
    public LocalDateTime bigAdminDecisionAt;

    /**
     * Username of the category administrator who acted on the request (if any).
     */
    public String categoryAdminActor;

    /**
     * Username of the big administrator who acted on the request (if any).
     */
    public String bigAdminActor;

    /**
     * Reason for rejection (if the request was rejected), otherwise {@code null}.
     */
    public String rejectReason;
}