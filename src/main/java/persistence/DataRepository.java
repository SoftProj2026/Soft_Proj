package persistence;

import domain.Appointment;
import domain.Category;
import domain.TimeSlot;
import domain.User;

import java.util.LinkedList;
import java.util.List;

/**
 * In-memory repository that stores users, slots, appointments, and categories.
 * <p>
 * This class acts as a simple data store (no database).
 * </p>
 */
public class DataRepository {

    private List<User> users = new LinkedList<>();
    private List<TimeSlot> slots = new LinkedList<>();
    private List<Appointment> appointments = new LinkedList<>();
    private List<Category> categories = new LinkedList<>();

    /**
     * Adds a new user to the repository.
     *
     * @param user the user to add
     */
    public void addUser(User user) { users.add(user); }

    /**
     * Returns all users in the repository.
     *
     * @return list of users
     */
    public List<User> getUsers() { return users; }

    /**
     * Returns all time slots in the repository.
     *
     * @return list of slots
     */
    public List<TimeSlot> getSlots() { return slots; }

    /**
     * Adds a new time slot to the repository.
     *
     * @param slot the slot to add
     */
    public void addSlot(TimeSlot slot) { slots.add(slot); }

    /**
     * Adds a new appointment to the repository.
     *
     * @param appointment the appointment to add
     */
    public void addAppointment(Appointment appointment) { appointments.add(appointment); }

    /**
     * Returns all appointments in the repository.
     *
     * @return list of appointments
     */
    public List<Appointment> getAppointments() { return appointments; }

    /**
     * Adds a new category to the repository.
     *
     * @param c the category to add
     */
    public void addCategory(Category c) { categories.add(c); }

    /**
     * Returns all categories in the repository.
     *
     * @return list of categories
     */
    public List<Category> getCategories() { return categories; }

    /**
     * Cancels an appointment if it exists in the repository.
     *
     * @param appointment the appointment to cancel
     * @return a user-friendly message describing the result
     */
    public String cancelAppointment(Appointment appointment) {
        if (appointment == null) return "Invalid booking.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        appointment.cancel();
        return "Booking cancelled successfully.";
    }
}