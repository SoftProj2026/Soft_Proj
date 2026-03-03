package Service;

import domain.User;
import persistence.DataRepository;

import java.time.LocalDate;
import java.time.Period;

/**
 * Provides authentication and registration operations.
 * <p>
 * This service validates login credentials against users stored in the
 * {@link DataRepository} and tracks the currently logged-in user.
 * </p>
 */
public class AuthService {

    /**
     * Registration outcomes.
     */
    public enum RegisterResult {
        /** Registration succeeded. */
        SUCCESS,
        /** Username is already taken. */
        USERNAME_TAKEN,
        /** User is under 18 years old. */
        UNDER_18,
        /** Input data is missing/invalid. */
        INVALID_INPUT
    }

    private final DataRepository repo;
    private User currentUser;

    /**
     * Creates a new {@code AuthService}.
     *
     * @param repo the repository used to store and retrieve users
     */
    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Attempts to login using username and password.
     * <p>
     * A login succeeds if a user exists in the repository whose username matches
     * (case-insensitive) and password matches exactly.
     * </p>
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
     * NEW: Logs in as admin without asking for username/password.
     * <p>
     * This is intended to be used ONLY after validating the Admin Key in the UI.
     * It simply finds the "admin" user in the repository and sets it as currentUser.
     * </p>
     *
     * @return true if admin user exists and was logged in; false otherwise
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
     * Registers a new user after validating input, ensuring unique username,
     * and verifying age (18+).
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

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public void logout() {
        currentUser = null;
    }
}