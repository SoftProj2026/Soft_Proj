
package Service;

import domain.User;
import persistence.DataRepository;


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
    }    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }
}