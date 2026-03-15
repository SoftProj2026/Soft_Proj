package persistence.dto;

/**
 * Data Transfer Object (DTO) representing a persisted snapshot of a provider.
 *
 * <p>This DTO mirrors the essential fields of the domain {@code Provider} and can be used to
 * persist providers separately from the general users list.</p>
 *
 * <p>It is primarily used to reconstruct the repository's provider collection (e.g., {@code repo.getProviders()})
 * when restoring a repository snapshot.</p>
 *
 * @author remaa
 * @version 1.0
 */
public class ProviderDTO {

    /**
     * Provider username (unique identifier).
     */
    public String username;

    /**
     * Provider password (as stored by the application).
     */
    public String password;

    /**
     * Human-friendly display name shown to other users.
     */
    public String displayName;

    /**
     * Provider phone number.
     */
    public String phone;

    /**
     * Provider email address.
     */
    public String email;

    /**
     * Provider physical address.
     */
    public String address;
}