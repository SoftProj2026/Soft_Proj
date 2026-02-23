package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDate;

/**
 * Booking rule that limits the number of confirmed appointments a single user
 * can have on any given calendar day.
 *
 * <p>By default the limit is 3 appointments per user per day.</p>
 */
public class DailyBookingLimitRule implements BookingRuleStrategy {

    private DataRepository repo;
    private int maxPerDay = 3;

    /**
     * Constructs a {@code DailyBookingLimitRule} backed by the given data repository.
     *
     * @param repo the {@link DataRepository} used to count existing appointments
     */
    public DailyBookingLimitRule(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns {@code true} if the user has fewer than {@code maxPerDay} confirmed
     * appointments on the same day as the new appointment.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if the daily limit has not been reached
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
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "You cannot book more than 3 appointments per day.";
    }
}