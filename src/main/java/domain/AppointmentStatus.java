package domain;

/**
 * Represents the status of an {@link Appointment}.
 */
public enum AppointmentStatus {

    /**
     * Appointment has been created but not yet confirmed.
     */
    PENDING,

    /**
     * Appointment has been confirmed and the slot is booked.
     */
    CONFIRMED,

    /**
     * Appointment has been cancelled and the slot is released.
     */
    CANCELLED
}