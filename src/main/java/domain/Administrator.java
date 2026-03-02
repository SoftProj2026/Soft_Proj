package domain;
/**
 * Represents an administrator user in the system.
 * <p>
 * This class extends {@link User} and can be used to distinguish
 * admin accounts from regular users.
 * </p>
 */
public class Administrator extends User {
    /**
     * Creates a new Administrator account.
     *
     * @param username the administrator username
     * @param password the administrator password
     */
    public Administrator(String username, String password) {
        super(username, password);
    }
}



