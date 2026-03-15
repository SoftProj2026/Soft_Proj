package domain;

/**
 * Represents the state of a {@link BookingRequest} within a two-step approval workflow.
 *
 * <p>The workflow is:</p>
 * <ol>
 *   <li>Category administrator approves or rejects the request.</li>
 *   <li>If approved, the big administrator approves or rejects the request.</li>
 *   <li>If approved by the big administrator, the request becomes confirmed and an appointment is created.</li>
 * </ol>
 *
 * @author s12219530-cpu (remaa)
 * @version 1.0
 */
public enum BookingRequestStatus {

    /**
     * Waiting for the category administrator decision.
     */
    PENDING_CATEGORY_ADMIN,

    /**
     * Rejected by the category administrator.
     */
    REJECTED_CATEGORY_ADMIN,

    /**
     * Waiting for the big administrator final decision.
     */
    PENDING_BIG_ADMIN,

    /**
     * Rejected by the big administrator.
     */
    REJECTED_BIG_ADMIN,

    /**
     * Approved by the big administrator and converted into a confirmed appointment.
     */
    APPROVED_AND_CONFIRMED
}