package Service;

import domain.Appointment;

/**
 * Booking rule that rejects appointments whose time slot is already booked.
 *
 * <p>Delegates the availability check to {@link domain.TimeSlot#isAvailable()}.</p>
 */
public class SlotAvailabilityRule implements BookingRuleStrategy {

    /**
     * Returns {@code true} if the appointment's time slot is currently available.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the slot is not yet booked
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getSlot().isAvailable();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "Selected time slot is already booked.";
    }
}
