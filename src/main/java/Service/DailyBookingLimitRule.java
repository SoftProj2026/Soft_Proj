package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDate;

/**
 * Restricts the number of confirmed appointments a user can book per day.
 * @auther remaa
 * @version 1.0
 */
public class DailyBookingLimitRule implements BookingRuleStrategy {

    private DataRepository repo;
    private int maxPerDay = 3;

    /**
     * Creates a daily limit rule.
     *
     * @param repo repository to count existing appointments
     */
    public DailyBookingLimitRule(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Validates that the user has not exceeded the daily booking limit.
     *
     * @param appointment appointment to validate
     * @return true if under the limit; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {

        LocalDate newDate =
                appointment.getSlot()
                        .getStartDateTime()
                        .toLocalDate();

        long count = repo.getAppointments().stream()
                .filter(a ->
                        a.getUser().getUsername()
                                .equals(appointment.getUser().getUsername())
                                &&
                        a.getSlot().getStartDateTime()
                                .toLocalDate()
                                .equals(newDate)
                                &&
                        a.getStatus().name().equals("CONFIRMED")
                )
                .count();

        return count < maxPerDay;
    }

    /**
     * Error message when daily limit is exceeded.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return "You cannot book more than 3 appointments per day.";
    }
}