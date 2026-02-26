package domain;

import java.time.LocalDate;

/**
 * Represents a system user who can login and book appointments.
 */
public class User {
    private String username;
    private String password;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    /**
     * Creates a user with full profile information.
     *
     * @param firstName    user's first name
     * @param lastName     user's last name
     * @param username     login username
     * @param password     login password
     * @param dateOfBirth  user's date of birth (used for age validation)
     */
    public User(String firstName, String lastName, String username, String password, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }

    /**
     * Creates a user with only username and password.
     * <p>
     * Other profile fields will be empty/null.
     * </p>
     *
     * @param username login username
     * @param password login password
     */
    public User(String username, String password) {
        this("", "", username, password, null);
    }

    /**
     * Gets the username.
     *
     * @return username
     */
    public String getUsername() {
    	return username; 
    	}

    /**
     * Gets the password.
     *
     * @return password
     */
    public String getPassword() {
    	return password;
    	}

    /**
     * Gets the first name.
     *
     * @return first name (may be empty)
     */
    public String getFirstName() {
    	return firstName;
    	}

    /**
     * Gets the last name.
     *
     * @return last name (may be empty)
     */
    public String getLastName() { 
    	return lastName; 
    	}

    /**
     * Gets the user's date of birth.
     *
     * @return date of birth (may be null)
     */
    public LocalDate getDateOfBirth() { 
    	return dateOfBirth; 
    	}
}