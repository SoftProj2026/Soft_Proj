package persistence.dto;

/**
 * Data Transfer Object (DTO) representing whether a specific user has used their cancellation
 * privilege/allowance for a specific category.
 *
 * <p>This DTO exists to persist the internal "cancel used" state of the repository where the key is
 * effectively {@code (username, categoryName)} and the value indicates whether it has been used.</p>
 *
 * <p>Values are typically stored normalized (trimmed/lower-cased) by the persistence layer, but this DTO
 * itself does not enforce normalization.</p>
 *
 * @author remaa
 * @version 1.0
 */
public class CancelUsedDTO {

    /**
     * The username this record applies to.
     */
    public String username;

    /**
     * The category name this record applies to.
     */
    public String categoryName;

    /**
     * Whether the cancellation privilege/allowance has already been used.
     */
    public boolean used;
}