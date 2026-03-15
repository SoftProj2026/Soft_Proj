package domain;

import java.time.LocalDateTime;

/**
 * Represents a booking request that must be approved through a two-step workflow.
 *
 * <p>Workflow:</p>
 * <ol>
 *   <li>Category administrator approves or rejects the request.</li>
 *   <li>If approved, the request is forwarded to the big administrator for final approval or rejection.</li>
 *   <li>If the big administrator approves, the request becomes confirmed and an appointment is created.</li>
 * </ol>
 *
 * <p>A request is associated with a {@link TimeSlot}. While pending, the slot is expected to be held
 * (reserved temporarily) to prevent other bookings.</p>
 *
 * @author s12219530-cpu (remaa)
 * @version 1.0
 */
public class BookingRequest {

    /**
     * Static counter used to generate unique request identifiers.
     */
    private static int counter = 1;

    /**
     * Unique request identifier.
     */
    private final int id;

    /**
     * User who submitted the request.
     */
    private final User requester;

    /**
     * Requested time slot.
     */
    private final TimeSlot slot;

    /**
     * Requested duration in minutes.
     */
    private final int durationInMinutes;

    /**
     * Number of participants for the requested booking.
     */
    private final int participants;

    /**
     * Username of the assigned category administrator responsible for the first approval step.
     */
    private final String categoryAdminUsername;

    /**
     * Current status of the request in the approval workflow.
     */
    private BookingRequestStatus status;

    /**
     * Timestamp when the request was created.
     */
    private final LocalDateTime createdAt;

    /**
     * Timestamp when the category administrator made a decision.
     */
    private LocalDateTime categoryDecisionAt;

    /**
     * Timestamp when the big administrator made a decision.
     */
    private LocalDateTime bigAdminDecisionAt;

    /**
     * Username of the category administrator who acted on the request (if any).
     */
    private String categoryAdminActor;

    /**
     * Username of the big administrator who acted on the request (if any).
     */
    private String bigAdminActor;

    /**
     * Rejection reason (if rejected), otherwise empty.
     */
    private String rejectReason;

    /**
     * Creates a new booking request.
     *
     * @param requester             the user who submitted the request
     * @param slot                  the requested time slot
     * @param durationInMinutes     requested duration in minutes
     * @param participants          number of participants
     * @param categoryAdminUsername assigned category administrator username
     */
    public BookingRequest(User requester,
                          TimeSlot slot,
                          int durationInMinutes,
                          int participants,
                          String categoryAdminUsername) {

        this.id = counter++;

        this.requester = requester;
        this.slot = slot;
        this.durationInMinutes = durationInMinutes;
        this.participants = participants;

        this.categoryAdminUsername = categoryAdminUsername != null ? categoryAdminUsername.trim() : "";
        this.status = BookingRequestStatus.PENDING_CATEGORY_ADMIN;

        this.createdAt = LocalDateTime.now();
        this.categoryDecisionAt = null;
        this.bigAdminDecisionAt = null;

        this.categoryAdminActor = "";
        this.bigAdminActor = "";
        this.rejectReason = "";
    }

    /**
     * Returns the request identifier.
     *
     * @return request id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user who submitted the request.
     *
     * @return requester user
     */
    public User getRequester() {
        return requester;
    }

    /**
     * Returns the requested time slot.
     *
     * @return requested slot
     */
    public TimeSlot getSlot() {
        return slot;
    }

    /**
     * Returns the requested duration in minutes.
     *
     * @return duration in minutes
     */
    public int getDurationInMinutes() {
        return durationInMinutes;
    }

    /**
     * Returns the number of participants.
     *
     * @return participants count
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * Returns the assigned category administrator username.
     *
     * @return category admin username (may be empty)
     */
    public String getCategoryAdminUsername() {
        return categoryAdminUsername;
    }

    /**
     * Returns the current request status.
     *
     * @return booking request status
     */
    public BookingRequestStatus getStatus() {
        return status;
    }

    /**
     * Returns the request creation timestamp.
     *
     * @return created at timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the category administrator decision timestamp.
     *
     * @return category decision timestamp, or {@code null} if not decided
     */
    public LocalDateTime getCategoryDecisionAt() {
        return categoryDecisionAt;
    }

    /**
     * Returns the big administrator decision timestamp.
     *
     * @return big admin decision timestamp, or {@code null} if not decided
     */
    public LocalDateTime getBigAdminDecisionAt() {
        return bigAdminDecisionAt;
    }

    /**
     * Returns the username of the category administrator who acted on the request.
     *
     * @return category admin actor username (may be empty)
     */
    public String getCategoryAdminActor() {
        return categoryAdminActor;
    }

    /**
     * Returns the username of the big administrator who acted on the request.
     *
     * @return big admin actor username (may be empty)
     */
    public String getBigAdminActor() {
        return bigAdminActor;
    }

    /**
     * Returns the rejection reason if the request was rejected.
     *
     * @return rejection reason (may be empty)
     */
    public String getRejectReason() {
        return rejectReason;
    }

    /**
     * Approves the request by the category administrator and forwards it to the big administrator.
     *
     * @param adminUsername the acting category administrator username
     */
    public void approveByCategoryAdmin(String adminUsername) {
        this.status = BookingRequestStatus.PENDING_BIG_ADMIN;
        this.categoryAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.categoryDecisionAt = LocalDateTime.now();
        this.rejectReason = "";
    }

    /**
     * Rejects the request by the category administrator.
     *
     * @param adminUsername the acting category administrator username
     * @param reason        rejection reason (may be {@code null})
     */
    public void rejectByCategoryAdmin(String adminUsername, String reason) {
        this.status = BookingRequestStatus.REJECTED_CATEGORY_ADMIN;
        this.categoryAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.categoryDecisionAt = LocalDateTime.now();
        this.rejectReason = reason != null ? reason.trim() : "";
    }

    /**
     * Approves the request by the big administrator and marks it as confirmed.
     *
     * @param adminUsername the acting big administrator username
     */
    public void approveByBigAdmin(String adminUsername) {
        this.status = BookingRequestStatus.APPROVED_AND_CONFIRMED;
        this.bigAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.bigAdminDecisionAt = LocalDateTime.now();
        this.rejectReason = "";
    }

    /**
     * Rejects the request by the big administrator.
     *
     * @param adminUsername the acting big administrator username
     * @param reason        rejection reason (may be {@code null})
     */
    public void rejectByBigAdmin(String adminUsername, String reason) {
        this.status = BookingRequestStatus.REJECTED_BIG_ADMIN;
        this.bigAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.bigAdminDecisionAt = LocalDateTime.now();
        this.rejectReason = reason != null ? reason.trim() : "";
    }
}