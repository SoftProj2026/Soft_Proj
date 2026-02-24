package Service;

import domain.User;
import persistence.DataRepository;

import java.time.LocalDate;
import java.util.regex.Pattern;

public class AuthService {

    private DataRepository repo;
    private User currentUser;

    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

    public boolean login(String username, String password) {

        for (User user : repo.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(username)
                    && user.getPassword().equals(password)) {

                currentUser = user;
                return true;
            }
        }
        return false;
    }

    public boolean register(String firstName,
                            String lastName,
                            LocalDate dateOfBirth,
                            String residence,
                            String password) {

        if (isBlank(firstName) || isBlank(lastName) || dateOfBirth == null || isBlank(residence) || isBlank(password)) {
            return false;
        }

        if (!isAdult(dateOfBirth)) {
            return false;
        }

        if (!isStrongPassword(password)) {
            return false;
        }

        String usernameBase = buildUsernameWithSpace(firstName, lastName);
        String username = usernameBase;

        int suffix = 1;
        while (usernameExists(username)) {
            suffix++;
            username = usernameBase + " " + suffix; // مثال: "Ahmad Ali 2"
        }

        repo.addUser(new User(username, password, normalizeName(firstName), normalizeName(lastName), dateOfBirth, residence));
        return true;
    }

    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            return false;
        }

        for (User user : repo.getUsers()) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return false;
            }
        }

        repo.addUser(new User(username, password));
        return true;
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


    private boolean usernameExists(String username) {
        for (User u : repo.getUsers()) {
            if (u.getUsername().equalsIgnoreCase(username)) return true;
        }
        return false;
    }

    private String buildUsernameWithSpace(String firstName, String lastName) {
        String f = normalizeName(firstName);
        String l = normalizeName(lastName);
        return f + " " + l;
    }

    private String normalizeName(String s) {
        if (s == null) return "";
        return s.trim().replaceAll("\\s+", " ");
    }

    private boolean isAdult(LocalDate dob) {
        LocalDate today = LocalDate.now();
        return !today.isBefore(dob.plusYears(18));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private boolean isStrongPassword(String password) {
        if (password == null) return false;
        if (password.length() < 8) return false;

        boolean hasLetter = Pattern.compile("[A-Za-z]").matcher(password).find();
        boolean hasDigit = Pattern.compile("[0-9]").matcher(password).find();
        boolean hasSpecial = Pattern.compile("[^A-Za-z0-9]").matcher(password).find();

        return hasLetter && hasDigit && hasSpecial;
    }
}