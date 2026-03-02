package domain;

import java.time.LocalDate;

/**
 * Represents a system user who can login and perform actions (booking, messaging).
 * <p>
 * This is the base account type in the application.
 * Other roles can extend it, such as:
 * <ul>
 *   <li>{@link Administrator}</li>
 *   <li>{@link Provider}</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class stores:
 * <ul>
 *   <li>Login credentials (username, password)</li>
 *   <li>Optional profile info (first name, last name, date of birth)</li>
 * </ul>
 * </p>
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
     * Convenience constructor for accounts that only need username/password.
     * <p>
     * The remaining profile fields will be empty or null.
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
     * Returns the first name (may be empty).
     *
     * @return first name
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the last name (may be empty).
     *
     * @return last name
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns date of birth (may be null).
     *
     * @return date of birth
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
}