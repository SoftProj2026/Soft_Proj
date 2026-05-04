package service;

import domain.Appointment;

import java.time.LocalDateTime;

/**
 * Prevents booking appointments in the past (any time before "now").
 * @author Qussai
 * @version 1.0
 */
public class NotInPastRule implements BookingRuleStrategy {

    /**
     * Validates that the appointment start date/time is not before the current time.
     *
     * @param appointment appointment to validate
     * @return {@code true} if appointment is not in the past; {@code false} otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null
                || appointment.getSlot() == null
                || appointment.getSlot().getStartDateTime() == null) {
            return false;
        }

        LocalDateTime start = appointment.getSlot().getStartDateTime();
        return !start.isBefore(LocalDateTime.now());
    }

    /**
     * Returns the error message when the appointment is in the past.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Cannot book an appointment in the past.";
    }
}