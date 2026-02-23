package domain;

/**
 * Represents a system administrator user with elevated privileges.
 * Extends {@link User} and inherits standard credential fields.
 */
public class Administrator extends User {

    /**
     * Constructs a new Administrator with the given username and password.
     *
     * @param username the administrator's unique username
     * @param password the administrator's password
     */
    public Administrator(String username, String password) {
        super(username, password);
    }
}
