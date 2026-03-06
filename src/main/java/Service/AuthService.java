package Service;

import domain.User;
import persistence.DataRepository;

import java.time.LocalDate;
import java.time.Period;

public class AuthService {

    public enum RegisterResult {
        SUCCESS,
        USERNAME_TAKEN,
        UNDER_18,
        INVALID_INPUT
    }

    private final DataRepository repo;
    private User currentUser;

    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

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
     * Force email-based registration (email is required).
     * Keep this method only to avoid breaking older code, but make it fail.
     */
    public RegisterResult register(String firstName,
                                   String lastName,
                                   String username,
                                   String password,
                                   LocalDate dateOfBirth) {
        return RegisterResult.INVALID_INPUT;
    }

    /**
     * NEW: Email is required.
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
            if (user.getUsername().equalsIgnoreCase(u)) {
                return RegisterResult.USERNAME_TAKEN;
            }
        }

        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 18) return RegisterResult.UNDER_18;

        repo.addUser(new User(firstName.trim(), lastName.trim(), u, password, dateOfBirth, em));
        return RegisterResult.SUCCESS;
    }

    public User getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    public void logout() { currentUser = null; }
}