package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the booking workflow by applying booking rules and saving appointments.
 */
public class BookingService {

    private final DataRepository repo;
    private final List<BookingRuleStrategy> rules = new ArrayList<>();

    /**
     * Creates a BookingService and registers default booking rules.
     *
     * @param repo the repository used to store appointments
     */
    public BookingService(DataRepository repo) {
        this.repo = repo;

        rules.add(new SlotAvailabilityRule());

        rules.add(new NotInPastRule());
        rules.add(new MinimumNoticeRule());

        rules.add(new BlockedSlotsRule());
        rules.add(new DurationRule(60));
        rules.add(new ParticipantLimitRule(5));
        rules.add(new OverlapRule(repo));
    }

    /**
     * Attempts to book an appointment by validating it against all rules.
     * <p>
     * If validation passes, the appointment is confirmed and stored in the repository.
     * </p>
     *
     * @param appointment the appointment to book
     * @return booking result (success/failure + message)
     */
    public BookingResult book(Appointment appointment) {

        for (BookingRuleStrategy rule : rules) {
            if (!rule.isValid(appointment)) {
                return new BookingResult(false, rule.getErrorMessage());
            }
        }

        appointment.confirm();
        repo.addAppointment(appointment);

        return new BookingResult(true, "Appointment booked successfully.");
    }
}