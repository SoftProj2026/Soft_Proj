package domain;

/**
 * Represents the possible states of an {@link Appointment}.
 *
 * <ul>
 *   <li>{@link #PENDING}   – the appointment has been created but not yet confirmed.</li>
 *   <li>{@link #CONFIRMED} – the appointment has been confirmed and the time slot is booked.</li>
 *   <li>{@link #CANCELLED} – the appointment has been cancelled and the time slot is released.</li>
 * </ul>
 */
public enum AppointmentStatus {
    /** The appointment has been created but not yet confirmed. */
    PENDING,
    /** The appointment has been confirmed and the time slot is booked. */
    CONFIRMED,
    /** The appointment has been cancelled and the time slot is released. */
    CANCELLED
}
