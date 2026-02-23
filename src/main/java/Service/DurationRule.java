package Service;

import domain.Appointment;

/**
 * Booking rule that enforces a maximum appointment duration.
 *
 * <p>US2.2 – Prevent booking if the duration exceeds the allowed limit.</p>
 */
public class DurationRule implements BookingRuleStrategy {

    private int maxDuration;

    /**
     * Constructs a {@code DurationRule} with the specified maximum duration.
     *
     * @param maxDuration the maximum allowed appointment duration in minutes
     */
    public DurationRule(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    /**
     * Returns {@code true} if the appointment's duration does not exceed
     * the configured maximum.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the duration is within the allowed limit
     */
    @Override
    public boolean isValid(Appointment appointment) {
        return appointment.getDurationInMinutes() <= maxDuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "Duration exceeds maximum allowed time.";
    }
}