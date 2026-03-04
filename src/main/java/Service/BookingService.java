package Service;

import domain.Appointment;
import domain.AuditEvent;
import persistence.DataRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for validating and confirming appointments using a set of booking rules.
 * <p>
 * The booking process:
 * </p>
 * <ol>
 *   <li>Validate the appointment using configured {@link BookingRuleStrategy} rules.</li>
 *   <li>If any rule fails, return a failure {@link BookingResult} with the rule message.</li>
 *   <li>If valid, confirm the appointment and store it in {@link DataRepository}.</li>
 *   <li>Write an {@link AuditEvent} for admin activity tracking.</li>
 * </ol>
 */
public class BookingService {

    private final DataRepository repo;
    private final List<BookingRuleStrategy> rules = new ArrayList<>();

    /**
     * Creates a booking service and registers the default booking rules.
     *
     * @param repo repository used to store appointments and read existing ones
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
     * Attempts to book an appointment after validating it against all booking rules.
     * <p>
     * If the booking succeeds, the appointment is confirmed and stored in the repository,
     * and an audit event is written.
     * </p>
     *
     * @param appointment appointment to book
     * @return result object containing success flag and message
     */
    public BookingResult book(Appointment appointment) {
        for (BookingRuleStrategy rule : rules) {
            if (!rule.isValid(appointment)) {
                return new BookingResult(false, rule.getErrorMessage());
            }
        }

        appointment.confirm();
        repo.addAppointment(appointment);

        String user = (appointment.getUser() != null) ? appointment.getUser().getUsername() : "unknown";
        String category = (appointment.getSlot() != null && appointment.getSlot().getCategory() != null)
                ? appointment.getSlot().getCategory().getName()
                : "N/A";

        repo.addAuditEvent(new AuditEvent(
                AuditEvent.Type.APPOINTMENT_CONFIRMED,
                user,
                category,
                "Confirmed appointment #" + appointment.getId()
        ));

        return new BookingResult(true, "Appointment booked successfully.");
    }
}