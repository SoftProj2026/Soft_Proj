package Service;

import domain.Appointment;

/**
 * Strategy interface for booking validation rules.
 * <p>
 * Each rule validates an {@link Appointment} and provides an error message
 * if validation fails.
 * </p>
 */
public interface BookingRuleStrategy {

    /**
     * Validates the given appointment.
     *
     * @param appointment the appointment to validate
     * @return true if valid; false otherwise
     */
    boolean isValid(Appointment appointment);

    /**
     * Provides the error message for the last validation failure.
     *
     * @return error message
     */
    String getErrorMessage();
}