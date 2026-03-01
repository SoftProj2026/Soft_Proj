package Service;

import domain.Appointment;

import java.time.LocalDateTime;

/**
 * Prevents booking appointments in the past (any time before "now").
 */
public class NotInPastRule implements BookingRuleStrategy {

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

    @Override
    public String getErrorMessage() {
        return "Cannot book an appointment in the past.";
    }
}