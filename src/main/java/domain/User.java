package domain;

import java.time.LocalDate;

/**
 * Represents an application user account.
 *
 * <p>A {@code User} stores authentication credentials and optional profile information
 * used across the system, such as name, date of birth, and email address.</p>
 *
 * <p>The email field is mutable to allow collecting/updating a user's email after account creation
 * (e.g., for reminder notifications).</p>
 */
public class User {

    private final String username;
    private final String password;

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;

    private String email;

    /**
     * Creates a fully-defined user with profile information and email.
     *
     * @param firstName   user's first name (may be empty)
     * @param lastName    user's last name (may be empty)
     * @param username    unique username used for login
     * @param password    password used for login
     * @param dateOfBirth date of birth (may be {@code null} for system accounts)
     * @param email       email address used for notifications (may be {@code null} or empty)
     */
    public User(String firstName,
                String lastName,
                String username,
                String password,
                LocalDate dateOfBirth,
                String email) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.email = email != null ? email.trim() : "";
    }

    /**
     * Creates a user with profile information but without an email.
     *
     * @param firstName   user's first name (may be empty)
     * @param lastName    user's last name (may be empty)
     * @param username    unique username used for login
     * @param password    password used for login
     * @param dateOfBirth date of birth (may be {@code null} for system accounts)
     */
    public User(String firstName,
                String lastName,
                String username,
                String password,
                LocalDate dateOfBirth) {
        this(firstName, lastName, username, password, dateOfBirth, "");
    }

    /**
     * Creates a basic user account with only username and password.
     *
     * @param username unique username used for login
     * @param password password used for login
     */
    public User(String username, String password) {
        this("", "", username, password, null, "");
    }

    /**
     * Returns the user's username.
     *
     * @return username string
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the user's password.
     *
     * @return password string
     */
    public String getPassword() {
        return password;
    }

    /**
     * Returns the user's first name.
     *
     * @return first name (may be empty)
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Returns the user's last name.
     *
     * @return last name (may be empty)
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Returns the user's date of birth.
     *
     * @return date of birth (may be {@code null})
     */
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    /**
     * Returns the user's email address.
     *
     * @return email address (never {@code null}; may be empty)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the user's email address.
     *
     * @param email new email address (may be {@code null} or empty)
     */
    public void setEmail(String email) {
        this.email = email != null ? email.trim() : "";
    }
}