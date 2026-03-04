package domain;

import java.time.LocalDateTime;

/**
 * Represents a booking request that must be approved through a two-step workflow.
 * <p>
 * Workflow:
 * </p>
 * <ol>
 *   <li>Category admin approves or rejects the request.</li>
 *   <li>If approved, the request is forwarded to the big admin for final approval or rejection.</li>
 *   <li>If the big admin approves, the request becomes confirmed and an appointment is created.</li>
 * </ol>
 *
 * <p>
 * A request is associated with a {@link TimeSlot}. While pending, the slot is expected to be held
 * (reserved temporarily) to prevent other bookings.
 * </p>
 */
public class BookingRequest {

    private static int counter = 1;

    private final int id;

    private final User requester;
    private final TimeSlot slot;
    private final int durationInMinutes;
    private final int participants;

    private final String categoryAdminUsername;

    private BookingRequestStatus status;

    private final LocalDateTime createdAt;
    private LocalDateTime categoryDecisionAt;
    private LocalDateTime bigAdminDecisionAt;

    private String categoryAdminActor;
    private String bigAdminActor;

    private String rejectReason;

    /**
     * Creates a new booking request.
     *
     * @param requester             request owner
     * @param slot                  requested slot
     * @param durationInMinutes     requested duration in minutes
     * @param participants          number of participants
     * @param categoryAdminUsername assigned category admin username
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
     * Returns the request id.
     *
     * @return request id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the user who created the request.
     *
     * @return requester
     */
    public User getRequester() {
        return requester;
    }

    /**
     * Returns the requested time slot.
     *
     * @return time slot
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
     * @return participants
     */
    public int getParticipants() {
        return participants;
    }

    /**
     * Returns the assigned category admin username.
     *
     * @return category admin username
     */
    public String getCategoryAdminUsername() {
        return categoryAdminUsername;
    }

    /**
     * Returns the current request status.
     *
     * @return status
     */
    public BookingRequestStatus getStatus() {
        return status;
    }

    /**
     * Returns request creation time.
     *
     * @return creation date/time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the category-admin decision time, if any.
     *
     * @return decision date/time or null if not decided yet
     */
    public LocalDateTime getCategoryDecisionAt() {
        return categoryDecisionAt;
    }

    /**
     * Returns the big-admin decision time, if any.
     *
     * @return decision date/time or null if not decided yet
     */
    public LocalDateTime getBigAdminDecisionAt() {
        return bigAdminDecisionAt;
    }

    /**
     * Returns the username of the category admin who acted on the request.
     *
     * @return category admin actor username (may be empty)
     */
    public String getCategoryAdminActor() {
        return categoryAdminActor;
    }

    /**
     * Returns the username of the big admin who acted on the request.
     *
     * @return big admin actor username (may be empty)
     */
    public String getBigAdminActor() {
        return bigAdminActor;
    }

    /**
     * Returns the reject reason if the request was rejected.
     *
     * @return reject reason (may be empty)
     */
    public String getRejectReason() {
        return rejectReason;
    }

    /**
     * Approves the request by the category admin and forwards it to the big admin.
     *
     * @param adminUsername acting category admin username
     */
    public void approveByCategoryAdmin(String adminUsername) {
        this.status = BookingRequestStatus.PENDING_BIG_ADMIN;
        this.categoryAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.categoryDecisionAt = LocalDateTime.now();
        this.rejectReason = "";
    }

    /**
     * Rejects the request by the category admin.
     *
     * @param adminUsername acting category admin username
     * @param reason        reject reason (may be null)
     */
    public void rejectByCategoryAdmin(String adminUsername, String reason) {
        this.status = BookingRequestStatus.REJECTED_CATEGORY_ADMIN;
        this.categoryAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.categoryDecisionAt = LocalDateTime.now();
        this.rejectReason = reason != null ? reason.trim() : "";
    }

    /**
     * Approves the request by the big admin and marks it as confirmed.
     *
     * @param adminUsername acting big admin username
     */
    public void approveByBigAdmin(String adminUsername) {
        this.status = BookingRequestStatus.APPROVED_AND_CONFIRMED;
        this.bigAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.bigAdminDecisionAt = LocalDateTime.now();
        this.rejectReason = "";
    }

    /**
     * Rejects the request by the big admin.
     *
     * @param adminUsername acting big admin username
     * @param reason        reject reason (may be null)
     */
    public void rejectByBigAdmin(String adminUsername, String reason) {
        this.status = BookingRequestStatus.REJECTED_BIG_ADMIN;
        this.bigAdminActor = adminUsername != null ? adminUsername.trim() : "";
        this.bigAdminDecisionAt = LocalDateTime.now();
        this.rejectReason = reason != null ? reason.trim() : "";
    }
}