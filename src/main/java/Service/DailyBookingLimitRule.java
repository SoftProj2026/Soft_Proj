package Service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDate;

public class DailyBookingLimitRule implements BookingRuleStrategy {

    private DataRepository repo;
    private int maxPerDay = 3;

    public DailyBookingLimitRule(DataRepository repo) {
        this.repo = repo;
    }

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

    @Override
    public String getErrorMessage() {
        return "You cannot book more than 3 appointments per day.";
    }
}