package Service;

import domain.Appointment;
import domain.TimeSlot;
import persistence.DataRepository;

/**
 * Prevents overlapping bookings with existing appointments.
 * <p>
 * (US3.1) Prevent overlapping bookings.
 * </p>
 * @author remaa
 * @version 1.0
 */
public class OverlapRule implements BookingRuleStrategy {

    private DataRepository repo;

    /**
     * Creates a new OverlapRule.
     *
     * @param repo repository to check existing appointments
     */
    public OverlapRule(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Validates that the new appointment does not overlap with any existing appointment.
     *
     * @param newAppointment the new appointment to validate
     * @return true if no overlap is found; false otherwise
     */
    @Override
    public boolean isValid(Appointment newAppointment) {

        for (Appointment existing : repo.getAppointments()) {

            TimeSlot newSlot = newAppointment.getSlot();
            TimeSlot existingSlot = existing.getSlot();

            boolean overlap =
                    newSlot.getStartDateTime().isBefore(existingSlot.getEndDateTime()) &&
                    newSlot.getEndDateTime().isAfter(existingSlot.getStartDateTime());

            if (overlap) {
                return false;
            }
        }

        return true;
    }

    /**
     * Error message when an overlap is detected.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Time slot overlaps with an existing booking.";
    }
}