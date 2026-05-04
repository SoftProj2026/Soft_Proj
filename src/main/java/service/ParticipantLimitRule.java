package service;

import domain.Appointment;

/**
 * Validates that the number of participants does not exceed the allowed limit.
 * <p>
 * (US2.3) Prevent booking if participants exceed allowed limit.
 * </p>
 * @author Qussai
 * @version 1.0
 */

public class ParticipantLimitRule implements BookingRuleStrategy {

    private int maxParticipants;

    /**
     * Creates a ParticipantLimitRule with a max allowed participants count.
     *
     * @param maxParticipants maximum participants allowed
     */
    public ParticipantLimitRule(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    /**
     * Checks if participants count is within allowed limit.
     *
     * @param appointment appointment to validate
     * @return true if participants are acceptable; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }

    /**
     * Error message when participants exceed limit.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Participant limit exceeded.";
    }
}