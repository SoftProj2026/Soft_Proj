package domain;

/**
 * Represents the status of an {@link Appointment}.
 *
 * <p>The appointment status describes the lifecycle stage of an appointment from creation
 * until it is completed or cancelled.</p>
 *
 * @author s12219530-cpu(Remaa)
 * @version 1.0
 */
public enum AppointmentStatus {

    /**
     * Appointment has been created but not yet confirmed.
     */
    PENDING,

    /**
     * Appointment has been confirmed and the underlying slot is booked.
     */
    CONFIRMED,

    /**
     * Appointment has been cancelled and the underlying slot is released.
     */
    CANCELLED,

    /**
     * Appointment has ended (time passed) and is considered finished.
     */
    COMPLETED
}