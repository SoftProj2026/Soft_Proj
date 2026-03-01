package domain;

import java.time.LocalDate;

/**
 * Represents a system user who can login and book appointments.
 */
public class User {

    private final String username;
    private final String password;

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;

    /**
     * Creates a user with full profile information.
     *
     * @param firstName   user's first name
     * @param lastName    user's last name
     * @param username    login username
     * @param password    login password
     * @param dateOfBirth user's date of birth (used for age validation)
     */
    public User(String firstName,
                String lastName,
                String username,
                String password,
                LocalDate dateOfBirth) {

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
     * Returns the username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the password.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the first name.
     *
     * @return first name (may be empty)
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name.
     *
     * @return last name (may be empty)
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the user's date of birth.
     *
     * @return date of birth (may be null)
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}