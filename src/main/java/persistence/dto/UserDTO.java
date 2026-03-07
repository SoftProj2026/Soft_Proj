package persistence.dto;

import java.time.LocalDate;

/**
 * Data Transfer Object (DTO) representing a serializable snapshot of a user.
 *
 * <p>This DTO is used by the persistence layer (repository snapshots) to store user data in a JSON-friendly
 * structure and later restore it back into domain objects.</p>
 *
 * <p>The {@link #type} field indicates which domain class should be reconstructed:</p>
 * <ul>
 *   <li>{@code "User"} for a normal user</li>
 *   <li>{@code "Administrator"} for an admin user</li>
 *   <li>{@code "Provider"} for a provider account</li>
 * </ul>
 *
 * <p>Some fields are only applicable depending on the user type. For example, provider-related fields
 * such as {@link #displayName}, {@link #phone}, and {@link #address} are relevant when {@code type="Provider"}.</p>
 */
public class UserDTO {

    /**
     * User type label used to restore the correct domain subclass.
     */
    public String type;

    /**
     * Username used for login and identification.
     */
    public String username;

    /**
     * Password used for login.
     */
    public String password;

    /**
     * First name (optional).
     */
    public String firstName;

    /**
     * Last name (optional).
     */
    public String lastName;

    /**
     * Date of birth (may be {@code null} for system accounts).
     */
    public LocalDate dateOfBirth;

    /**
     * Email address used for notifications/reminders (may be empty).
     */
    public String email;

    /**
     * Provider display name (only for provider accounts).
     */
    public String displayName;

    /**
     * Provider phone number (only for provider accounts).
     */
    public String phone;

    /**
     * Provider address/location (only for provider accounts).
     */
    public String address;
}