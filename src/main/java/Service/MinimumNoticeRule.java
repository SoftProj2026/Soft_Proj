package Service;

import domain.Appointment;
import java.time.LocalDateTime;

/**
 * Ensures bookings are made a minimum time in advance (e.g., 1 hour).
 */
public class MinimumNoticeRule implements BookingRuleStrategy {

    /**
     * Validates that the appointment starts at least 1 hour from now.
     * (>= 60 minutes is allowed)
     *
     * @param appointment appointment to validate
     * @return true if appointment is 60+ minutes in advance; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null
                || appointment.getSlot() == null
                || appointment.getSlot().getStartDateTime() == null) {
            return false;
        }

        LocalDateTime start = appointment.getSlot().getStartDateTime();
        LocalDateTime minAllowed = LocalDateTime.now().plusHours(1);

        return !start.isBefore(minAllowed);
    }

    /**
     * Error message when booking is too close to start time.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Booking must be made at least 1 hour in advance.";
    }
}