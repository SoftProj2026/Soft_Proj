package domain;

/**
 * Represents a service provider account (company or property owner).
 *
 * <p>A {@code Provider} can log in to the system (because it extends {@link User}) and can receive
 * messages from customers via {@link ContactRequest}.</p>
 *
 * <p>Provider accounts store additional contact information that can be displayed to customers
 * and administrators.</p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class Provider extends User {

    /**
     * Display name shown to customers (for example, company or property name).
     */
    private final String displayName;

    /**
     * Provider phone number (optional).
     */
    private final String phone;

    /**
     * Provider email address (optional).
     */
    private final String email;

    /**
     * Provider address or location (optional).
     */
    private final String address;

    /**
     * Creates a new provider account.
     *
     * @param username    provider login username
     * @param password    provider login password
     * @param displayName name shown in the UI (company or property name)
     * @param phone       contact phone number (optional)
     * @param email       contact email address (optional)
     * @param address     address or location (optional)
     */
    public Provider(String username,
                    String password,
                    String displayName,
                    String phone,
                    String email,
                    String address) {

        super(username, password);
        this.displayName = displayName != null ? displayName.trim() : "";
        this.phone = phone != null ? phone.trim() : "";
        this.email = email != null ? email.trim() : "";
        this.address = address != null ? address.trim() : "";
    }

    /**
     * Returns the provider display name.
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the provider phone number.
     *
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns the provider email address.
     *
     * @return email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns the provider address or location.
     *
     * @return address/location
     */
    public String getAddress() {
        return address;
    }
}