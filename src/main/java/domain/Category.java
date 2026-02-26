package domain;

/**
 * Represents a booking category (e.g., "Doctor Appointment", "Conference Hall", etc.).
 */
public class Category {

    private String name;
    private Category category;

    /**
     * Creates a new category with the given name.
     *
     * @param name the category name
     */
    public Category(String name) {
        this.name = name;
        this.category = category;
    }
    /**
     * Gets the parent/related category (if any).
     *
     * @return the category reference (may be null)
     */
    public Category getCategory() {
        return category;
    }
    /**
     * Gets the category name.
     *
     * @return the category name
     */
    public String getName() {
        return name;
    }
    /**
     * Returns the category name as a string.
     *
     * @return category name
     */
    @Override
    public String toString() {
        return name;
    }
}