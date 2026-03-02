package domain;

/**
 * Represents a service provider account (company/property owner).
 * <p>
 * A provider can login to the system (because it extends {@link User}) and can
 * receive messages from customers via {@link ContactRequest}.
 * </p>
 *
 * <p>
 * Provider accounts store additional contact information that can be displayed
 * to customers/admins.
 * </p>
 */
public class Provider extends User {

    /** Display name shown to customers (e.g., company/property name). */
    private final String displayName;

    /** Provider phone number (optional). */
    private final String phone;

    /** Provider email address (optional). */
    private final String email;

    /** Provider address/location (optional). */
    private final String address;

    /**
     * Creates a new provider account.
     *
     * @param username    provider login username
     * @param password    provider login password
     * @param displayName name shown in the UI (company/property name)
     * @param phone       contact phone (optional)
     * @param email       contact email (optional)
     * @param address     address/location (optional)
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
     * Returns provider display name (company/property name).
     *
     * @return display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns provider phone.
     *
     * @return phone number
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Returns provider email.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Returns provider address/location.
     *
     * @return address
     */
    public String getAddress() {
        return address;
    }
}