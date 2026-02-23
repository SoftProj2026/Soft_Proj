package persistence;

import domain.User;
import domain.Appointment;

import java.util.LinkedList;
import java.util.List;

/**
 * In-memory repository that stores {@link User} and {@link Appointment} data
 * for the duration of the application session.
 *
 * <p>Acts as the single source of truth for users and appointments,
 * providing basic CRUD-like operations used by the service layer.</p>
 */
public class DataRepository {

    private List<User> users = new LinkedList<>();
    private List<Appointment> appointments = new LinkedList<>();

    /**
     * Adds a new user to the repository.
     *
     * @param user the {@link User} to add
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Returns all users currently stored in the repository.
     *
     * @return a {@link List} of all {@link User} objects
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Adds a new appointment to the repository.
     *
     * @param appointment the {@link Appointment} to add
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    /**
     * Returns all appointments currently stored in the repository.
     *
     * @return a {@link List} of all {@link Appointment} objects
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }
}