package service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDateTime;

/**
 * Prevents two confirmed appointments from having the exact same start time.
 * @author remaa
 * @version 1.0
 */
public class UniqueStartTimeRule implements BookingRuleStrategy {

    private final DataRepository repo;
    private String errorMessage = "This time is not available.";

    /**
     * Creates a rule that checks unique start times.
     *
     * @param repo repository used to search for existing appointments
     */
    public UniqueStartTimeRule(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Validates that the start time is not already used by a confirmed appointment.
     *
     * @param appointment appointment to validate
     * @return true if start time is unique; false otherwise
     */
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

    /**
     * Error message when the start time is not unique.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}