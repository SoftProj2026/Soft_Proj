package domain;

import java.time.LocalDateTime;

/**
 * Represents a bookable time slot defined by a start date/time and a duration.
 *
 * <p>A {@code TimeSlot} tracks whether it has been booked and provides
 * methods to reserve or release it.</p>
 */
public class TimeSlot {

    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isBooked;

    /**
     * Constructs a new TimeSlot starting at {@code startDateTime} with the
     * specified duration.
     *
     * @param startDateTime    the date and time when the slot begins
     * @param durationInMinutes the length of the slot in minutes
     */
    public TimeSlot(LocalDateTime startDateTime, int durationInMinutes) {
        this.startDateTime = startDateTime;
        this.endDateTime = startDateTime.plusMinutes(durationInMinutes);
        this.isBooked = false;
    }

    /**
     * Returns {@code true} if the slot has not yet been booked.
     *
     * @return {@code true} if available, {@code false} if already booked
     */
    public boolean isAvailable() {
        return !isBooked; 
    }

    /**
     * Marks this slot as booked (unavailable).
     */
    public void book() {
        this.isBooked = true;
    }

    /**
     * Releases this slot, making it available for booking again.
     */
    public void cancel() {
        this.isBooked = false;
    }

    /**
     * Returns the start date and time of this slot.
     *
     * @return the start {@link LocalDateTime}
     */
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    /**
     * Returns the end date and time of this slot (computed from start + duration).
     *
     * @return the end {@link LocalDateTime}
     */
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
}