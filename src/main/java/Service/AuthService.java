package Service;

import domain.User;
import persistence.DataRepository;

import java.time.LocalDate;
import java.time.Period;

/**
 * Provides authentication and registration operations.
 */
public class AuthService {

    /**
     * Registration outcomes.
     */
    public enum RegisterResult {
        SUCCESS,
        USERNAME_TAKEN,
        UNDER_18,
        INVALID_INPUT
    }

    private final DataRepository repo;
    private User currentUser;

    /**
     * Creates a new AuthService.
     *
     * @param repo the repository used to store and retrieve users
     */
    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Attempts to login using username and password.
     *
     * @param username the entered username
     * @param password the entered password
     * @return true if credentials are valid; false otherwise
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;

        String u = username.trim();
        if (u.isEmpty()) return false;

        for (User user : repo.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(u)
                    && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Registers a new user after validating input, unique username, and age (18+).
     *
     * @param firstName   first name (required)
     * @param lastName    last name (required)
     * @param username    username (required, must be unique)
     * @param password    password (required)
     * @param dateOfBirth date of birth (required, must be 18+)
     * @return the registration result status
     */
    public RegisterResult register(String firstName,
                                   String lastName,
                                   String username,
                                   String password,
                                   LocalDate dateOfBirth) {

        if (firstName == null || firstName.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (lastName == null || lastName.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (username == null || username.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (password == null || password.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (dateOfBirth == null) return RegisterResult.INVALID_INPUT;

        String u = username.trim();

        for (User user : repo.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(u)) {
                return RegisterResult.USERNAME_TAKEN;
            }
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18) return RegisterResult.UNDER_18;

        repo.addUser(new User(firstName.trim(), lastName.trim(), u, password, dateOfBirth));
        return RegisterResult.SUCCESS;
    }

    /**
     * Gets the currently logged-in user.
     *
     * @return current user, or null if not logged in
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks whether a user is currently logged in.
     *
     * @return true if logged in; false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        currentUser = null;
    }
}