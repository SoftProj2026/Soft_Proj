package domain;

/**
 * Represents the user-selected type of an appointment after it is confirmed.
 *
 * <p>The selected type may affect validation rules and additional stored fields
 * (for example, group size for {@link #GROUP}).</p>
 *
 * @author s12219530-cpu(remaa)
 * @version 1.0
 */
public enum AppointmentType {

    /**
     * Standard new appointment.
     */
    NEW_APPOINTMENT,

    /**
     * Review appointment (follow-up).
     */
    REVIEW,

    /**
     * Emergency appointment.
     */
    EMERGENCY,

    /**
     * Individual appointment (single participant).
     */
    INDIVIDUAL,

    /**
     * Group appointment (multiple participants).
     */
    GROUP
}