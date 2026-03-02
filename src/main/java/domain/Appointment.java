package domain;

import java.time.LocalDateTime;

/**
 * Represents a booking appointment made by a {@link User} for a specific {@link TimeSlot}.
 * <p>
 * An appointment contains:
 * <ul>
 *   <li>A unique incremental ID</li>
 *   <li>The user who created the appointment</li>
 *   <li>The chosen {@link TimeSlot}</li>
 *   <li>Duration in minutes (may be less than the slot length)</li>
 *   <li>Number of participants</li>
 *   <li>A {@link AppointmentStatus} representing the current state</li>
 *   <li>Timestamps for creation/confirmation/cancellation</li>
 * </ul>
 * </p>
 *
 * <h2>Lifecycle</h2>
 * <ul>
 *   <li>New appointments begin in {@link AppointmentStatus#PENDING}.</li>
 *   <li>When booked, {@link #confirm()} sets status to {@link AppointmentStatus#CONFIRMED}
 *       and marks the slot as booked.</li>
 *   <li>When cancelled, {@link #cancel()} sets status to {@link AppointmentStatus#CANCELLED}
 *       and releases the slot.</li>
 * </ul>
 */
public class Appointment {

    /** Counter used to generate auto-increment appointment IDs. */
    private static int counter = 1;

    private final int id;
    private final User user;
    private final TimeSlot slot;
    private final int durationInMinutes;
    private final int participants;
    private AppointmentStatus status;

    /** Timestamp when the appointment object was created. */
    private final LocalDateTime createdAt;

    /** Timestamp when the appointment was confirmed (set by {@link #confirm()}). */
    private LocalDateTime confirmedAt;

    /** Timestamp when the appointment was cancelled (set by {@link #cancel()}). */
    private LocalDateTime cancelledAt;

    /**
     * Creates a new appointment in {@link AppointmentStatus#PENDING} state.
     *
     * @param user              the user who is making the appointment
     * @param slot              the selected slot
     * @param durationInMinutes appointment duration in minutes
     * @param participants      number of participants
     */
    public Appointment(User user, TimeSlot slot, int durationInMinutes, int participants) {
        this.id = counter++;
        this.user = user;
        this.slot = slot;
        this.durationInMinutes = durationInMinutes;
        this.participants = participants;
        this.status = AppointmentStatus.PENDING;

        this.createdAt = LocalDateTime.now();
        this.confirmedAt = null;
        this.cancelledAt = null;
    }

    /**
     * Returns the unique appointment ID.
     *
     * @return appointment id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the owner user of the appointment.
     *
     * @return appointment owner
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the slot associated with this appointment.
     *
     * @return slot
     */
    public TimeSlot getSlot() {
        return slot;
    }

    /**
     * Returns the duration in minutes for this appointment.
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
     * Returns current appointment status.
     *
     * @return status enum
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Returns the creation timestamp of the appointment object.
     *
     * @return createdAt timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the confirmation timestamp, or {@code null} if not confirmed.
     *
     * @return confirmedAt timestamp or null
     */
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    /**
     * Returns the cancellation timestamp, or {@code null} if not cancelled.
     *
     * @return cancelledAt timestamp or null
     */
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    /**
     * Confirms this appointment:
     * <ul>
     *   <li>Sets status to {@link AppointmentStatus#CONFIRMED}</li>
     *   <li>Sets {@link #confirmedAt} to now</li>
     *   <li>Marks the slot booked via {@link TimeSlot#book()}</li>
     * </ul>
     */
    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        slot.book();
    }

    /**
     * Cancels this appointment:
     * <ul>
     *   <li>Sets status to {@link AppointmentStatus#CANCELLED}</li>
     *   <li>Sets {@link #cancelledAt} to now</li>
     *   <li>Releases the slot via {@link TimeSlot#cancel()}</li>
     * </ul>
     */
    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        slot.cancel();
    }
}