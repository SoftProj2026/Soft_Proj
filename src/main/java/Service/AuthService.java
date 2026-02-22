package Service;

import domain.User;
import persistence.DataRepository;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

//Sprint 1 - Authentication Service
 // Handles Login, Logout, Registration and Password Validation
 
public class AuthService {

    private DataRepository repo;
    private User currentUser;

    public AuthService(DataRepository repo) {
        this.repo = repo;
    }

    // Hash password using PBKDF2WithHmacSHA256 with the username as salt.
    // Using the username as a per-user salt prevents rainbow table attacks
    // without needing to store a separate salt value.
    private static String hashPassword(String password, String salt) {
        try {
            byte[] saltBytes = salt.getBytes(StandardCharsets.UTF_8);
            KeySpec spec = new PBEKeySpec(password.toCharArray(), saltBytes, 65536, 256);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hash = factory.generateSecret(spec).getEncoded();
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

   
     //US1.1 - User can login with valid username and password
    
    public boolean login(String username, String password) {
        String hashed = hashPassword(password, username);
        for (User u : repo.getUsers()) {
            if (u.getUsername().equals(username)
                    && u.getPassword().equals(hashed)) {

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

        repo.addUser(new User(username, hashPassword(password, username)));
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