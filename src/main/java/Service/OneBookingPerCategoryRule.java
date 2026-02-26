package Service;

import domain.Appointment;
import persistence.DataRepository;

/**
 * Prevents a user from having more than one confirmed booking in the same category.
 */
public class OneBookingPerCategoryRule implements BookingRuleStrategy {

    private final DataRepository repo;
    private String errorMessage = "You already have a booking in this category.";

    /**
     * Creates a rule that checks existing bookings in the repository.
     *
     * @param repo repository used to search for existing appointments
     */
    public OneBookingPerCategoryRule(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Validates that the user does not already have a confirmed appointment
     * in the same category.
     *
     * @param appointment appointment to validate
     * @return true if no existing booking in category; false otherwise
     */
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

    /**
     * Returns the error message for category duplication.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}