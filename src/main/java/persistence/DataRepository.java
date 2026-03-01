package persistence;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.Category;
import domain.TimeSlot;
import domain.User;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * In-memory repository that stores users, time slots, appointments, and categories.
 * <p>
 * This class acts as a simple data store (no database). It is used by services and UI
 * to retrieve and persist the application's runtime state.
 * </p>
 */
public class DataRepository {

    private final List<User> users = new LinkedList<>();
    private final List<TimeSlot> slots = new LinkedList<>();
    private final List<Appointment> appointments = new LinkedList<>();
    private final List<Category> categories = new LinkedList<>();

    /**
     * Tracks whether a user has already used their single allowed cancellation
     * for a given category.
     * <p>
     * Key format: {@code username|categoryName} (both normalized to lower-case/trimmed).
     * Value: {@code true} if cancellation has been used.
     * </p>
     */
    private final Map<String, Boolean> cancelUsedByUserCategory = new HashMap<>();

    /**
     * Creates a normalized map key used for tracking cancellations by user+category.
     *
     * @param username     username (may be null)
     * @param categoryName category name (may be null)
     * @return normalized key in the form {@code username|categoryName}
     */
    private String cancelKey(String username, String categoryName) {
        return (username == null ? "" : username.toLowerCase().trim())
                + "|"
                + (categoryName == null ? "" : categoryName.toLowerCase().trim());
    }

    /**
     * Adds a new user to the repository.
     *
     * @param user the user to add
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Returns all users stored in the repository.
     * <p>
     * Note: This returns the underlying list reference.
     * </p>
     *
     * @return list of users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Returns all time slots stored in the repository.
     * <p>
     * Note: This returns the underlying list reference.
     * </p>
     *
     * @return list of slots
     */
    public List<TimeSlot> getSlots() {
        return slots;
    }

    /**
     * Adds a new time slot to the repository.
     *
     * @param slot the slot to add
     */
    public void addSlot(TimeSlot slot) {
        slots.add(slot);
    }

    /**
     * Adds a new appointment to the repository.
     *
     * @param appointment the appointment to add
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    /**
     * Returns all appointments stored in the repository.
     * <p>
     * Note: This returns the underlying list reference.
     * </p>
     *
     * @return list of appointments
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Adds a new booking category to the repository.
     *
     * @param c the category to add
     */
    public void addCategory(Category c) {
        categories.add(c);
    }

    /**
     * Returns all booking categories stored in the repository.
     * <p>
     * Note: This returns the underlying list reference.
     * </p>
     *
     * @return list of categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * Cancels a confirmed appointment and marks that the user has used their
     * single allowed cancellation for this appointment's category.
     * <p>
     * Rules enforced:
     * <ul>
     *   <li>Appointment must be non-null and exist in the repository.</li>
     *   <li>Only {@link AppointmentStatus#CONFIRMED} appointments can be cancelled.</li>
     *   <li>A user can cancel only ONE confirmed booking per category.</li>
     * </ul>
     * </p>
     *
     * @param appointment the appointment to cancel
     * @return a user-friendly message describing the result
     */
    public String cancelAppointment(Appointment appointment) {
        if (appointment == null) return "Invalid booking.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return "Only CONFIRMED bookings can be cancelled.";
        }

        String username = appointment.getUser() != null ? appointment.getUser().getUsername() : null;

        String categoryName = null;
        if (appointment.getSlot() != null && appointment.getSlot().getCategory() != null) {
            categoryName = appointment.getSlot().getCategory().getName();
        }

        if (username == null || categoryName == null) {
            return "Cannot cancel booking (missing user or category).";
        }

        String key = cancelKey(username, categoryName);

        boolean alreadyUsed = cancelUsedByUserCategory.getOrDefault(key, false);
        if (alreadyUsed) {
            return "Cancellation not allowed. You can only cancel ONE booking in category \"" + categoryName + "\".";
        }

        cancelUsedByUserCategory.put(key, true);
        appointment.cancel();
        return "Booking cancelled successfully (one cancellation used for category \"" + categoryName + "\").";
    }
}