package Service;

import domain.Appointment;
import java.time.LocalDateTime;

/**
 * Ensures bookings are made a minimum time in advance (e.g., 1 hour).
 */
public class MinimumNoticeRule implements BookingRuleStrategy {

    /**
     * Validates that the appointment starts at least 1 hour from now.
     *
     * @param appointment appointment to validate
     * @return true if appointment is 1+ hour in advance; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {

        return appointment.getSlot()
                .getStartDateTime()
                .isAfter(LocalDateTime.now().plusHours(1));
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