package service;

import domain.Appointment;

/**
 * Validates that appointment duration does not exceed the allowed maximum.
 * <p>
 * (US2.2) Prevent booking if duration exceeds allowed limit.
 * </p>
 * @author remaa
 * @version 1.0
 
 */

public class DurationRule implements BookingRuleStrategy {

    private int maxDuration;

    /**
     * Creates a DurationRule with a max allowed duration.
     *
     * @param maxDuration maximum duration in minutes
     */
    public DurationRule(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    /**
     * Checks if the appointment duration is within the allowed limit.
     *
     * @param appointment appointment to validate
     * @return true if duration is acceptable; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getDurationInMinutes() <= maxDuration;
    }

    /**
     * Error message when duration is too long.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Duration exceeds maximum allowed time.";
    }
}