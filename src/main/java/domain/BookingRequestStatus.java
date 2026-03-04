package domain;

/**
 * Represents the state of a booking request within a two-step approval workflow.
 * <p>
 * The workflow is:
 * </p>
 * <ol>
 *   <li>Category admin approves or rejects the request.</li>
 *   <li>If approved, the big admin approves or rejects the request.</li>
 *   <li>If the big admin approves, the request becomes confirmed and an appointment is created.</li>
 * </ol>
 */
public enum BookingRequestStatus {

    /**
     * Waiting for the category admin decision.
     */
    PENDING_CATEGORY_ADMIN,

    /**
     * Rejected by the category admin.
     */
    REJECTED_CATEGORY_ADMIN,

    /**
     * Waiting for the big admin final decision.
     */
    PENDING_BIG_ADMIN,

    /**
     * Rejected by the big admin.
     */
    REJECTED_BIG_ADMIN,

    /**
     * Approved by the big admin and converted into a confirmed appointment.
     */
    APPROVED_AND_CONFIRMED
}