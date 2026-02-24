package Service;

import domain.Appointment;
import persistence.DataRepository;

public class OneBookingPerCategoryRule implements BookingRuleStrategy {

    private final DataRepository repo;
    private String errorMessage = "You already have a booking in this category.";

    public OneBookingPerCategoryRule(DataRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean isValid(Appointment appointment) {
        String user = appointment.getUser().getUsername();
        String category = appointment.getSlot().getCategory().getName();

        boolean alreadyBooked = repo.getAppointments().stream()
                .filter(a -> a.getStatus().name().equals("CONFIRMED"))
                .anyMatch(a ->
                        a.getUser().getUsername().equalsIgnoreCase(user) &&
                        a.getSlot().getCategory().getName().equalsIgnoreCase(category)
                );

        if (alreadyBooked) {
            errorMessage = "You already booked a slot for \"" + category + "\". You cannot book another one.";
            return false;
        }
        return true;
    }

    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}