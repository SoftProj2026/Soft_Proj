package domain;

import java.time.LocalDateTime;

/**
 * Represents a booking appointment made by a {@link User} for a specific {@link TimeSlot}.
 *
 * <p>An {@code Appointment} is created in {@link AppointmentStatus#PENDING} state and can later be
 * {@link #confirm() confirmed}, {@link #cancel() cancelled}, or {@link #complete() completed}.</p>
 *
 * <p>Appointments may optionally store an {@link AppointmentType} and additional fields depending on the type
 * (e.g., group size for group appointments).</p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class Appointment {

    /**
     * Static counter used to generate unique appointment identifiers.
     */
    private static int counter = 1;

    /**
     * Unique appointment identifier.
     */
    private final int id;

    /**
     * User who owns this appointment.
     */
    private final User user;

    /**
     * Time slot assigned to this appointment.
     */
    private final TimeSlot slot;

    /**
     * Appointment duration in minutes.
     */
    private final int durationInMinutes;

    /**
     * Number of participants for the appointment.
     */
    private final int participants;

    /**
     * Current appointment status.
     */
    private AppointmentStatus status;

    /**
     * Timestamp when the appointment object was created.
     */
    private final LocalDateTime createdAt;

    /**
     * Timestamp when the appointment was confirmed.
     */
    private LocalDateTime confirmedAt;

    /**
     * Timestamp when the appointment was cancelled.
     */
    private LocalDateTime cancelledAt;

    /**
     * Selected appointment type (may be {@code null} if not selected yet).
     */
    private AppointmentType appointmentType;

    /**
     * Group size (only applicable when {@link #appointmentType} is {@link AppointmentType#GROUP}).
     */
    private Integer groupSize;

    /**
     * Target slot start time used for review appointments (only applicable when type is {@link AppointmentType#REVIEW}).
     */
    private LocalDateTime reviewTargetSlotStart;

    /**
     * Preferred slot start time used for emergency appointments (only applicable when type is {@link AppointmentType#EMERGENCY}).
     */
    private LocalDateTime emergencyPreferredSlotStart;

    /**
     * Creates a new appointment in {@link AppointmentStatus#PENDING} state.
     *
     * @param user              the user who is making the appointment
     * @param slot              the time slot to book
     * @param durationInMinutes the appointment duration in minutes
     * @param participants      the number of participants
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

        this.appointmentType = null;
        this.groupSize = null;

        this.reviewTargetSlotStart = null;
        this.emergencyPreferredSlotStart = null;
    }

    /**
     * Returns the appointment identifier.
     *
     * @return appointment id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the appointment owner.
     *
     * @return user who booked the appointment
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns the appointment time slot.
     *
     * @return time slot
     */
    public TimeSlot getSlot() {
        return slot;
    }

    /**
     * Returns the appointment duration in minutes.
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
     * Returns the current status of the appointment.
     *
     * @return appointment status
     */
    public AppointmentStatus getStatus() {
        return status;
    }

    /**
     * Returns the timestamp when the appointment was created.
     *
     * @return creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Returns the timestamp when the appointment was confirmed.
     *
     * @return confirmation timestamp, or {@code null} if not confirmed
     */
    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    /**
     * Returns the timestamp when the appointment was cancelled.
     *
     * @return cancellation timestamp, or {@code null} if not cancelled
     */
    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    /**
     * Returns the selected appointment type.
     *
     * @return appointment type, or {@code null} if not selected
     */
    public AppointmentType getAppointmentType() {
        return appointmentType;
    }

    /**
     * Returns the group size.
     *
     * @return group size, or {@code null} if not applicable
     */
    public Integer getGroupSize() {
        return groupSize;
    }

    /**
     * Returns the review target slot start time.
     *
     * @return review target slot start time, or {@code null} if not applicable
     */
    public LocalDateTime getReviewTargetSlotStart() {
        return reviewTargetSlotStart;
    }

    /**
     * Returns the emergency preferred slot start time.
     *
     * @return emergency preferred slot start time, or {@code null} if not applicable
     */
    public LocalDateTime getEmergencyPreferredSlotStart() {
        return emergencyPreferredSlotStart;
    }

    /**
     * Sets the appointment type and clears any type-specific fields that no longer apply.
     *
     * @param appointmentType the appointment type to set
     */
    public void setAppointmentType(AppointmentType appointmentType) {
        this.appointmentType = appointmentType;

        if (appointmentType != AppointmentType.GROUP) 
        {
            this.groupSize = null;
        }
        if (appointmentType != AppointmentType.REVIEW) 
        {
            this.reviewTargetSlotStart = null;
        }
        if (appointmentType != AppointmentType.EMERGENCY) 
        {
            this.emergencyPreferredSlotStart = null;
        }
    }

    /**
     * Sets the group size (only meaningful when type is {@link AppointmentType#GROUP}).
     *
     * @param groupSize group size value, may be {@code null}
     */
    public void setGroupSize(Integer groupSize) {
        this.groupSize = groupSize;
    }

    /**
     * Sets the review target slot start time (only meaningful when type is {@link AppointmentType#REVIEW}).
     *
     * @param reviewTargetSlotStart target slot start time, may be {@code null}
     */
    public void setReviewTargetSlotStart(LocalDateTime reviewTargetSlotStart) {
        this.reviewTargetSlotStart = reviewTargetSlotStart;
    }

    /**
     * Sets the emergency preferred slot start time (only meaningful when type is {@link AppointmentType#EMERGENCY}).
     *
     * @param emergencyPreferredSlotStart preferred slot start time, may be {@code null}
     */
    public void setEmergencyPreferredSlotStart(LocalDateTime emergencyPreferredSlotStart) {
        this.emergencyPreferredSlotStart = emergencyPreferredSlotStart;
    }

    /**
     * Confirms the appointment, sets the confirmation timestamp, and books the underlying slot.
     */
    public void confirm() {
        this.status = AppointmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        slot.book();
    }

    /**
     * Cancels the appointment, sets the cancellation timestamp, and releases the underlying slot.
     */
    public void cancel() {
        this.status = AppointmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        slot.cancel();
    }

    /**
     * Marks the appointment as completed if it is currently confirmed.
     */
    public void complete() {
        if (this.status == AppointmentStatus.CONFIRMED) {
            this.status = AppointmentStatus.COMPLETED;
        }
    }
}