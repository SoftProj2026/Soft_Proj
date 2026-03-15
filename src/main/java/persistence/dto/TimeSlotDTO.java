package persistence.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing a persisted snapshot of a {@code TimeSlot}.
 *
 * <p>This DTO stores the slot timing and category association, along with its availability state.
 * It is designed for serialization (e.g., JSON) and later restoration into the domain model.</p>
 *
 * <p>The slot state is represented using:
 * <ul>
 *   <li>{@link #booked} to indicate the slot is not available due to a confirmed booking</li>
 *   <li>{@link #held} to indicate the slot is temporarily held (reserved) for a booking request</li>
 *   <li>{@link #heldRequestId} to identify the request holding the slot (when {@code held == true})</li>
 * </ul>
 * </p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class TimeSlotDTO {

    /**
     * Slot start date-time.
     */
    public LocalDateTime start;

    /**
     * Slot duration in minutes.
     */
    public int durationMinutes;

    /**
     * Name of the category associated with this slot (may be {@code null}).
     */
    public String categoryName;

    /**
     * Whether the slot is booked (not available).
     */
    public boolean booked;

    /**
     * Whether the slot is currently held for a booking request.
     */
    public boolean held;

    /**
     * Identifier of the booking request holding the slot, if any.
     */
    public Integer heldRequestId;
}