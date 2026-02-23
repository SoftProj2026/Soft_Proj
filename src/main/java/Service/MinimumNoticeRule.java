package Service;

import domain.Appointment;
import java.time.LocalDateTime;

/**
 * Booking rule that requires appointments to be made at least 1 hour in advance.
 *
 * <p>This prevents last-minute bookings by ensuring the appointment's start
 * time is more than one hour from the current time.</p>
 */
public class MinimumNoticeRule implements BookingRuleStrategy {

    /**
     * Returns {@code true} if the appointment starts more than 1 hour from now.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the minimum notice period is satisfied
     */
    @Override
    public boolean isValid(Appointment appointment) {

        return appointment.getSlot()
                .getStartDateTime()
                .isAfter(LocalDateTime.now().plusHours(1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "Booking must be made at least 1 hour in advance.";
    }
}