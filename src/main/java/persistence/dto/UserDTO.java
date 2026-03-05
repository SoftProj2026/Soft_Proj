package persistence.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a persisted snapshot of a user.
 *
 * <p>This DTO is used to serialize/deserialize user data for repository snapshots.
 * The {@link #type} field indicates which domain subtype should be reconstructed:
 * typically {@code "User"}, {@code "Provider"}, or {@code "Administrator"}.</p>
 *
 * <p>Provider-specific fields (displayName/phone/email/address) may be {@code null}
 * when {@link #type} is not {@code "Provider"}.</p>
 */
public class UserDTO {

    /**
     * User type discriminator used during restoration (e.g., "User", "Provider", "Administrator").
     */
    public String type;

    /**
     * Unique username used to identify the user.
     */
    public String username;

    /**
     * User password (as stored by the application).
     */
    public String password;

    /**
     * User first name (primarily for regular users).
     */
    public String firstName;

    /**
     * User last name (primarily for regular users).
     */
    public String lastName;

    /**
     * User date of birth (primarily for regular users).
     */
    public LocalDate dateOfBirth;

    /**
     * Provider display name (only meaningful when {@link #type} indicates Provider).
     */
    public String displayName;

    /**
     * Provider phone number (only meaningful when {@link #type} indicates Provider).
     */
    public String phone;

    /**
     * Provider email address (only meaningful when {@link #type} indicates Provider).
     */
    public String email;

    /**
     * Provider physical address (only meaningful when {@link #type} indicates Provider).
     */
    public String address;
}