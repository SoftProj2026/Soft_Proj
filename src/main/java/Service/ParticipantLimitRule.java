package Service;

import domain.Appointment;

public class ParticipantLimitRule implements BookingRuleStrategy {

    private int maxParticipants;

    public ParticipantLimitRule(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }

    @Override
    public String getErrorMessage() {
        return "Participant limit exceeded.";
    }
}
