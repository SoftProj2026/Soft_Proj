package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Booking rule that prevents scheduling a new appointment whose time window
 * overlaps with any already-confirmed appointment.
 *
 * <p>An overlap is detected when the new appointment's start time is before
 * an existing appointment's end time <em>and</em> the new appointment's end
 * time is after the existing appointment's start time.</p>
 */
public class OverlapRule implements BookingRuleStrategy {

    private DataRepository repo;

    /**
     * Constructs an {@code OverlapRule} backed by the given data repository.
     *
     * @param repo the {@link DataRepository} used to retrieve confirmed appointments
     */
    public OverlapRule(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns {@code true} if the appointment does not overlap with any
     * confirmed appointment in the repository.
     *
     * @param appointment the appointment to validate
     * @return {@code true} if no time overlap is detected
     */
    @Override
    public boolean isValid(Appointment appointment) {

        List<Appointment> existing = repo.getAppointments();

        LocalDateTime newStart =
                appointment.getSlot().getStartDateTime();
        LocalDateTime newEnd =
                appointment.getSlot().getEndDateTime();

        for (Appointment a : existing) {

            if (!a.getStatus().name().equals("CONFIRMED"))
                continue;

            LocalDateTime existingStart =
                    a.getSlot().getStartDateTime();
            LocalDateTime existingEnd =
                    a.getSlot().getEndDateTime();

            if (newStart.isBefore(existingEnd)
                    && newEnd.isAfter(existingStart)) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getErrorMessage() {
        return "This time slot overlaps with an existing appointment.";
    }
}