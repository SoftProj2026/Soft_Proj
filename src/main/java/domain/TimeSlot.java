package domain;

import java.time.LocalDateTime;

/**
 * Represents a time interval that can be booked within a {@link Category}.
 * <p>
 * A slot may be:
 * </p>
 * <ul>
 *   <li><b>Available</b>: not booked and not held</li>
 *   <li><b>Held</b>: temporarily reserved for a pending approval request</li>
 *   <li><b>Booked</b>: confirmed and no longer available</li>
 * </ul>
 */
public class TimeSlot {

    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;

    private boolean booked;

    private boolean held;
    private Integer heldRequestId;

    private final Category category;

    /**
     * Creates a time slot for a specific category.
     *
     * @param startDateTime      slot start date/time
     * @param durationInMinutes  slot duration in minutes
     * @param category           slot category (may be null)
     */
    public TimeSlot(LocalDateTime startDateTime, int durationInMinutes, Category category) {
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusMinutes(durationInMinutes);
        this.booked = false;
        this.held = false;
        this.heldRequestId = null;
        this.category = category;
    }

    /**
     * Creates a time slot without a category.
     *
     * @param startDateTime     slot start date/time
     * @param durationInMinutes slot duration in minutes
     */
    public TimeSlot(LocalDateTime startDateTime, int durationInMinutes) {
        this(startDateTime, durationInMinutes, null);
    }

    /**
     * Returns the category assigned to the slot.
     *
     * @return category (may be null)
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Indicates whether the slot is available for requesting/booking.
     *
     * @return true if not booked and not held; otherwise false
     */
    public boolean isAvailable() {
        return !booked && !held;
    }

    /**
     * Indicates whether the slot is currently held for a pending approval request.
     *
     * @return true if held; otherwise false
     */
    public boolean isHeld() {
        return held;
    }

    /**
     * Returns the request id currently holding the slot.
     *
     * @return held request id, or null if not held
     */
    public Integer getHeldRequestId() {
        return heldRequestId;
    }

    /**
     * Places a temporary hold on this slot.
     * <p>
     * If the slot is already booked, the hold is not applied.
     * </p>
     *
     * @param requestId booking request id that holds the slot
     */
    public void hold(int requestId) {
        if (booked) return;
        this.held = true;
        this.heldRequestId = requestId;
    }

    /**
     * Releases a temporary hold, making the slot available again if it is not booked.
     */
    public void releaseHold() {
        this.held = false;
        this.heldRequestId = null;
    }

    /**
     * Marks the slot as booked and clears any hold information.
     */
    public void book() {
        this.booked = true;
        this.held = false;
        this.heldRequestId = null;
    }

    /**
     * Cancels the booking and clears any hold information.
     */
    public void cancel() {
        this.booked = false;
        this.held = false;
        this.heldRequestId = null;
    }

    /**
     * Returns the slot start date/time.
     *
     * @return start date/time
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Returns the slot end date/time.
     *
     * @return end date/time
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}