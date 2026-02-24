package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDateTime;

public class UniqueStartTimeRule implements BookingRuleStrategy {

    private final DataRepository repo;
    private String errorMessage = "This time is not available.";

    public UniqueStartTimeRule(DataRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean isValid(Appointment appointment) {

        LocalDateTime newStart = appointment.getSlot().getStartDateTime();

        boolean timeAlreadyBooked = repo.getAppointments().stream()
                .filter(a -> a.getStatus().name().equals("CONFIRMED"))
                .anyMatch(a -> a.getSlot().getStartDateTime().equals(newStart));

        if (timeAlreadyBooked) {
            errorMessage = "This time slot is already booked by another user (same time). Please choose a different time.";
            return false;
        }

        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}