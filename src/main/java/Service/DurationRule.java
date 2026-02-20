package Service;

import domain.Appointment;

public class DurationRule implements BookingRuleStrategy {

    private int maxDuration;

    public DurationRule(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getDurationInMinutes() <= maxDuration;
    }

    @Override
    public String getErrorMessage() {
        return "Duration exceeds maximum allowed time.";
    }
}
