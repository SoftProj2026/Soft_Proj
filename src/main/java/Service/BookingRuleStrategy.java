package Service;

import domain.Appointment;

/**
 * Strategy interface for booking validation rules.
 *
 * <p>Each implementation encapsulates a single business rule that determines
 * whether an {@link Appointment} may be booked. The {@link BookingService}
 * iterates through all registered strategies and rejects the booking as soon
 * as any rule returns {@code false}.</p>
 */
public interface BookingRuleStrategy {

    /**
     * Checks whether the given appointment satisfies this rule.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the rule is satisfied, {@code false} otherwise
     */
    boolean isValid(Appointment appointment);

    /**
     * Returns a human-readable error message describing why the rule was violated.
     *
     * @return the error message
     */
    String getErrorMessage();
}