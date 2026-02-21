package Service;
import domain.Appointment;

public class SlotAvailabilityRule implements BookingRuleStrategy {

    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getSlot().isAvailable();
    }

    @Override
    public String getErrorMessage() {
        return "Selected time slot is already booked.";
    }
}
