package Service;

import domain.User;
import domain.Administrator;
import persistence.DataRepository;


public class AuthService {

    private DataRepository repo;
    private User currentUser;

    public AuthService(DataRepository repo) {
        this.repo = repo;
     
    }

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

    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Optional 
    public boolean register(String username, String password) {
        for (User u : repo.getUsers()) {
            if (u.getUsername().equals(username)) {
                return false;
            }
        }
        repo.addUser(new User(username, password));
        return true;
    }

    public boolean isPasswordStrong(String pass) {
        return pass.length() >= 8
                && pass.matches(".*[0-9].*")
                && pass.matches(".*[!@#$%^&*].*");
    }
}
