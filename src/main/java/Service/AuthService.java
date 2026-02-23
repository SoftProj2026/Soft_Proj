package Service;

import domain.User;
import persistence.DataRepository;

/**
 * Service responsible for authentication operations including login, logout,
 * user registration, and password strength validation.
 *
 * <p>Sprint 1 – Authentication Service.<br>
 * Handles Login, Logout, Registration and Password Validation.</p>
 */
public class AuthService {

    private DataRepository repo;
    private User currentUser;

    /**
     * Constructs an {@code AuthService} backed by the given data repository.
     *
     * @param repo the {@link DataRepository} used to look up and store users
     */
    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Attempts to log in a user with the provided credentials.
     * US1.1 – User can log in with a valid username and password.
     *
     * @param username the username to authenticate
     * @param password the password to authenticate
     * @return {@code true} if the credentials match a registered user,
     *         {@code false} otherwise
     */
    public boolean login(String username, String password) {
        for (User u : repo.getUsers()) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(password)) {

                currentUser = u;
                return true;
            }
        }
        return false;
    }

    /**
     * Logs out the currently authenticated user by clearing the session.
     * US1.2 – User can log out and the session is cleared.
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * Returns {@code true} if a user is currently logged in.
     *
     * @return {@code true} if a session is active, {@code false} otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Returns the currently logged-in user, or {@code null} if no session is active.
     *
     * @return the current {@link User}, or {@code null}
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Registers a new user if the username is not already taken and the
     * password meets the strength requirements.
     * US1.3 – User can register if the username does not already exist.
     *
     * @param username the desired username
     * @param password the desired password
     * @return {@code true} if registration succeeded, {@code false} if the
     *         username is already taken or the password is too weak
     */
    public boolean register(String username, String password) {

        if (!isPasswordStrong(password)) {
            return false;
        }

        for (User u : repo.getUsers()) {
            if (u.getUsername().equals(username)) {
                return false;
            }
        }

        repo.addUser(new User(username, password));
        return true;
    }

    /**
     * Checks whether a password meets the strong password policy.
     * US1.4 – The system enforces a strong password policy:
     * minimum 8 characters, at least one digit, and at least one special character
     * ({@code !@#$%^&*}).
     *
     * @param pass the password string to evaluate
     * @return {@code true} if the password is considered strong, {@code false} otherwise
     */
    public boolean isPasswordStrong(String pass) {
        return pass.length() >= 8
                && pass.matches(".*[0-9].*")
                && pass.matches(".*[!@#$%^&*].*");
    }
}