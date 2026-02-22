package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDateTime;
import java.util.List;

public class OverlapRule implements BookingRuleStrategy {

    private DataRepository repo;

    public OverlapRule(DataRepository repo) {
        this.repo = repo;
    }

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

    @Override
    public String getErrorMessage() {
        return "This time slot overlaps with an existing appointment.";
    }
}