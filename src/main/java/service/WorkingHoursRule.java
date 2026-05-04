package service;

import domain.Appointment;

import java.time.LocalTime;

/**
 * Restricts bookings to working hours (default: 9:00 AM to 5:00 PM).
 * @author Qussai 
 * @version 1.0
 */
public class WorkingHoursRule implements BookingRuleStrategy {

    private LocalTime start = LocalTime.of(9, 0);
    private LocalTime end = LocalTime.of(17, 0);

    /**
     * Validates that the appointment time is within allowed working hours.
     *
     * @param appointment appointment to validate
     * @return true if within working hours; false otherwise
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
     * Error message when appointment is outside working hours.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Appointment must be between 9:00 AM and 5:00 PM.";
    }
}