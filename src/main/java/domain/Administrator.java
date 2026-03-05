package domain;


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