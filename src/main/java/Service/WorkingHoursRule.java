package Service;

import domain.Appointment;

import java.time.LocalTime;

public class WorkingHoursRule implements BookingRuleStrategy {

    private LocalTime start = LocalTime.of(9, 0);
    private LocalTime end = LocalTime.of(17, 0);

    @Override
    public boolean isValid(Appointment appointment) {

        LocalTime appointmentTime =
                appointment.getSlot()
                        .getStartDateTime()
                        .toLocalTime();

        return !appointmentTime.isBefore(start)
                && !appointmentTime.isAfter(end);
    }

    @Override
    public String getErrorMessage() {
        return "Appointment must be between 9:00 AM and 5:00 PM.";
    }
}