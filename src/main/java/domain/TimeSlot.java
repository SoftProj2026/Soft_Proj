package domain;

import java.time.LocalDateTime;

/**
 * Represents a time interval available for booking.
 * <p>
 * A slot has a start time, end time (computed from duration), a booked flag,
 * and an optional {@link Category}.
 * </p>
 */
public class TimeSlot {
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isBooked;

    private Category category;

    /**
     * Creates a new time slot with the given start time, duration, and category.
     *
     * @param startDateTime     the slot start date/time
     * @param durationInMinutes duration in minutes (used to compute end time)
     * @param category          the slot category (may be null)
     */
    public TimeSlot(LocalDateTime startDateTime, int durationInMinutes, Category category) {
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusMinutes(durationInMinutes);
        this.isBooked = false;
        this.category = category;
    }

    /**
     * Creates a new time slot without a category.
     *
     * @param startDateTime     the slot start date/time
     * @param durationInMinutes duration in minutes (used to compute end time)
     */
    public TimeSlot(LocalDateTime startDateTime, int durationInMinutes) {
        this(startDateTime, durationInMinutes, null);
    }

    /**
     * Gets the slot category.
     *
     * @return the category (may be null)
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Checks if the slot is available for booking.
     *
     * @return true if not booked; false otherwise
     */
    public boolean isAvailable() {
        return !isBooked;
    }

    /**
     * Marks the slot as booked (unavailable).
     */
    public void book() {
        this.isBooked = true;
    }

    /**
     * Cancels the booking and makes the slot available again.
     */
    public void cancel() {
        this.isBooked = false;
    }

    /**
     * Gets the start date/time of this slot.
     *
     * @return start date/time
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Gets the end date/time of this slot.
     *
     * @return end date/time
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}