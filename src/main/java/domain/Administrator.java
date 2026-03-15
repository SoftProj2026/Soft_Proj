package domain;

/**
 * Represents an administrator account in the system.
 *
 * <p>An {@code Administrator} is a system user with elevated privileges (e.g., managing requests,
 * reviewing activity, and managing reservations). This class extends {@link User} and reuses the
 * base authentication fields (username and password).</p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class Administrator extends User {

    /**
     * Creates a new administrator account.
     *
     * @param username the administrator username used for login
     * @param password the administrator password used for login
     */
    public Administrator(String username, String password) {
        super(username, password);
    }
}