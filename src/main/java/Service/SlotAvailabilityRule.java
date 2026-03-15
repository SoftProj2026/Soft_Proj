package Service;

import domain.Appointment;

/**
 * Validates that the selected slot is still available (not already booked).
 * @author remaa
 * @version 1.0
 *  */
public class SlotAvailabilityRule implements BookingRuleStrategy {

    /**
     * Checks whether the appointment's slot is available.
     *
     * @param appointment appointment to validate
     * @return true if slot is available; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getSlot().isAvailable();
    }

    /**
     * Returns the error message when the slot is already booked.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "Selected time slot is already booked.";
    }
}