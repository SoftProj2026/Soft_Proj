package Service;

import domain.Appointment;


 // US2.3 - Prevent booking if participants exceed allowed limit
 
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