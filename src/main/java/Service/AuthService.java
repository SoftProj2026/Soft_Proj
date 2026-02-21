package Service;

import domain.User;
import persistence.DataRepository;


//Sprint 1 - Authentication Service
 // Handles Login, Logout, Registration and Password Validation
 
public class AuthService {

    private DataRepository repo;
    private User currentUser;

    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

   
     //US1.1 - User can login with valid username and password
    
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

    
     //US1.2 - User can logout and session is cleared
     
    public void logout() {
        currentUser = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    
     //US1.3 - User can register if username does not already exist
     
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

    
     //US1.4 - System enforces strong password policy
      //Minimum 8 characters, at least 1 number and 1 special character
     
    public boolean isPasswordStrong(String pass) {
        return pass.length() >= 8
                && pass.matches(".*[0-9].*")
                && pass.matches(".*[!@#$%^&*].*");
    }
}