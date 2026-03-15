package persistence.dto;

/**
 * Data Transfer Object (DTO) representing a {@code Category} in a serializable form.
 *
 * <p>This DTO is used inside repository snapshots (e.g., {@code RepoSnapshot}) to persist the list
 * of available categories without carrying full domain behavior.</p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class CategoryDTO {

    /**
     * The category name.
     */
    public String name;
}