package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for processing appointment booking requests.
 *
 * <p>Uses the Strategy pattern: a list of {@link BookingRuleStrategy} rules
 * is evaluated in order for every booking attempt. If all rules pass, the
 * appointment is confirmed and persisted; otherwise an error message is
 * returned without persisting anything.</p>
 *
 * <p>Default rules applied (in order):</p>
 * <ol>
 *   <li>{@link DurationRule} – maximum 60 minutes</li>
 *   <li>{@link ParticipantLimitRule} – maximum 5 participants</li>
 *   <li>{@link OverlapRule} – no overlap with confirmed appointments</li>
 *   <li>{@link WorkingHoursRule} – slot must be within 09:00–17:00</li>
 *   <li>{@link MinimumNoticeRule} – must be booked at least 1 hour in advance</li>
 *   <li>{@link DailyBookingLimitRule} – maximum 3 bookings per user per day</li>
 * </ol>
 */
public class BookingService {

    private DataRepository repo;
    private List<BookingRuleStrategy> rules = new ArrayList<>();

    /**
     * Constructs a {@code BookingService} backed by the given data repository
     * and registers all default booking rules.
     *
     * @param repo the {@link DataRepository} used to read and persist appointments
     */
    public BookingService(DataRepository repo) {
        this.repo = repo;

        rules.add(new DurationRule(60));
        rules.add(new ParticipantLimitRule(5));
        rules.add(new OverlapRule(repo));
        rules.add(new WorkingHoursRule());
        rules.add(new MinimumNoticeRule());
        rules.add(new DailyBookingLimitRule(repo));
    }

    /**
     * Attempts to book the given appointment by evaluating all registered rules.
     *
     * <p>If every rule passes, the appointment is confirmed and saved to the
     * repository. The first failing rule stops evaluation and its error message
     * is returned.</p>
     *
     * @param appointment the {@link Appointment} to book
     * @return a {@link BookingResult} indicating success or the reason for failure
     */
    public BookingResult book(Appointment appointment) {

        for (BookingRuleStrategy rule : rules) {
            if (!rule.isValid(appointment)) {
                return new BookingResult(false,
                        rule.getErrorMessage());
            }
        }

        appointment.confirm();
        repo.addAppointment(appointment);

        return new BookingResult(true,
                "Appointment booked successfully.");
    }
}