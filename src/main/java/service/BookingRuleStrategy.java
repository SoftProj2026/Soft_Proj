package service;

import domain.Appointment;

/**
 * Strategy interface for booking validation rules.
 * <p>
 * Each rule validates an {@link Appointment} and provides an error message
 * if validation fails.
 * </p>
 * @author Qussaialaw
 * @version 1.0
 */
public interface BookingRuleStrategy {

    /**
     * Validates the given appointment.
     *
     * @param appointment appointment to validate
     * @return {@code true} if valid; {@code false} otherwise
     */
    boolean isValid(Appointment appointment);

    /**
     * Returns an error message describing why the last validation failed.
     *
     * @return error message
     */
    String getErrorMessage();
}