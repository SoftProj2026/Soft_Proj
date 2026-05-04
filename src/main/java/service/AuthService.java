package service;

import domain.User;
import persistence.DataRepository;

import java.time.LocalDate;
import java.time.Period;

/**
 * Provides authentication and registration operations for the application.
 *
 * <p>This service authenticates users against the in-memory {@link DataRepository} and stores the
 * currently authenticated user in the {@code currentUser} field.</p>
 *
 * <p>Registration enforces:</p>
 * <ul>
 *   <li>Non-empty first name, last name, username, and password</li>
 *   <li>Non-null date of birth</li>
 *   <li>Email is required and must be in a valid basic format</li>
 *   <li>Unique username</li>
 *   <li>Minimum age of 18 years</li>
 * </ul>
 * @author Qussai @ remaa
 * @version 1.0
 */
public class AuthService {

    /**
     * Result values returned by registration operations.
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
     * Creates an authentication service backed by the given repository.
     *
     * @param repo repository containing users
     */
    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Logs in by locating an existing user by username only (no password check).
     *
     * <p>This is intended for controlled internal flows such as category-admin quick login, where the
     * system has already validated access by a separate key.</p>
     *
     * @param username target username
     * @return {@code true} if the user was found and the session was created; otherwise {@code false}
     */
    public boolean loginAsUser(String username) {
        if (username == null) return false;
        String u = username.trim();
        if (u.isEmpty()) return false;

        for (User user : repo.getUsers()) {
            if (user != null && user.getUsername() != null && user.getUsername().equalsIgnoreCase(u)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs in using username and password.
     *
     * @param username username
     * @param password password
     * @return {@code true} if credentials are valid; otherwise {@code false}
     */
    public boolean login(String username, String password) {
        if (username == null || password == null) return false;

        String u = username.trim();
        if (u.isEmpty()) return false;

        for (User user : repo.getUsers()) {
            if (user == null || user.getUsername() == null || user.getPassword() == null) continue;

            if (user.getUsername().equalsIgnoreCase(u)
                    && user.getPassword().equals(password)) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs in as the single big-admin account ("admin") if it exists in the repository.
     *
     * @return {@code true} if the admin account exists and login succeeded; otherwise {@code false}
     */
    public boolean loginAsAdmin() {
        for (User user : repo.getUsers()) {
            if (user != null
                    && user.getUsername() != null
                    && user.getUsername().equalsIgnoreCase("admin")) {
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Legacy registration overload (without email).
     *
     * <p>Email registration is required by the application. This method exists only to avoid breaking
     * older code paths and always returns {@link RegisterResult#INVALID_INPUT}.</p>
     *
     * @param firstName   first name
     * @param lastName    last name
     * @param username    username
     * @param password    password
     * @param dateOfBirth date of birth
     * @return {@link RegisterResult#INVALID_INPUT} always
     */
    public RegisterResult register(String firstName,
                                   String lastName,
                                   String username,
                                   String password,
                                   LocalDate dateOfBirth) {
        return RegisterResult.INVALID_INPUT;
    }

    /**
     * Registers a new user account and stores it in the repository.
     *
     * @param firstName   first name (required)
     * @param lastName    last name (required)
     * @param username    username (required, must be unique)
     * @param password    password (required)
     * @param dateOfBirth date of birth (required, must indicate age >= 18)
     * @param email       email (required)
     * @return result indicating success or the reason for failure
     */
    public RegisterResult register(String firstName,
                                   String lastName,
                                   String username,
                                   String password,
                                   LocalDate dateOfBirth,
                                   String email) {

        if (firstName == null || firstName.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (lastName == null || lastName.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (username == null || username.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (password == null || password.trim().isEmpty()) return RegisterResult.INVALID_INPUT;
        if (dateOfBirth == null) return RegisterResult.INVALID_INPUT;

        if (email == null || email.trim().isEmpty()) return RegisterResult.INVALID_INPUT;

        String em = email.trim();
        if (!em.contains("@") || !em.contains(".")) return RegisterResult.INVALID_INPUT;

        String u = username.trim();

        for (User user : repo.getUsers()) {
            if (user != null && user.getUsername() != null && user.getUsername().equalsIgnoreCase(u)) {
                return RegisterResult.USERNAME_TAKEN;
            }
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18) return RegisterResult.UNDER_18;

        repo.addUser(new User(firstName.trim(), lastName.trim(), u, password, dateOfBirth, em));
        return RegisterResult.SUCCESS;
    }

    /**
     * Returns the current logged-in user, or {@code null} if no session is active.
     *
     * @return current user or null
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Indicates whether a session is currently active.
     *
     * @return {@code true} if a user is logged in; otherwise {@code false}
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Logs out the current user by clearing the session.
     */
    public void logout() {
        currentUser = null;
    }
}