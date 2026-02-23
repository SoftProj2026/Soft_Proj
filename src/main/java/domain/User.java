package domain;

/**
 * Represents a registered user in the appointment booking system.
 * Stores the user's credentials (username and password).
 */
public class User {
    private String username;
    private String password;

    /**
     * Constructs a new User with the given username and password.
     *
     * @param username the unique name used to identify the user
     * @param password the user's password for authentication
     */
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Returns the username of this user.
     *
     * @return the username
     */
    public String getUsername() {
    	return username;
    	}

    /**
     * Returns the password of this user.
     *
     * @return the password
     */
    public String getPassword() { 
    	return password; 
    	}
}
