package Service;

import domain.Appointment;

import java.time.LocalTime;

/**
 * Booking rule that enforces appointments to fall within business working hours.
 *
 * <p>Appointments are only accepted when their start time is between
 * 09:00 (inclusive) and 17:00 (inclusive).</p>
 */
public class WorkingHoursRule implements BookingRuleStrategy {

    private LocalTime start = LocalTime.of(9, 0);
    private LocalTime end = LocalTime.of(17, 0);

    /**
     * Returns {@code true} if the appointment's start time is within working hours
     * (09:00–17:00 inclusive).
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the start time is within the allowed range
     */
    @Override
    public boolean isValid(Appointment appointment) {

        LocalTime appointmentTime =
                appointment.getSlot()
                        .getStartDateTime()
                        .toLocalTime();

        return !appointmentTime.isBefore(start)
                && !appointmentTime.isAfter(end);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "Appointment must be between 9:00 AM and 5:00 PM.";
    }
}