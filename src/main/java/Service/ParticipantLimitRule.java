package Service;

import domain.Appointment;

/**
 * Booking rule that enforces a maximum number of participants per appointment.
 *
 * <p>US2.3 – Prevent booking if participants exceed the allowed limit.</p>
 */
public class ParticipantLimitRule implements BookingRuleStrategy {

    private int maxParticipants;

    /**
     * Constructs a {@code ParticipantLimitRule} with the specified maximum.
     *
     * @param maxParticipants the maximum number of participants allowed
     */
    public ParticipantLimitRule(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    /**
     * Returns {@code true} if the appointment's participant count does not
     * exceed the configured maximum.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the participant count is within the limit
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getParticipants() <= maxParticipants;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "Participant limit exceeded.";
    }
}