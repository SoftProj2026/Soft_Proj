package domain;

/**
 * Represents a booking category (e.g., "Doctor Appointment", "Conference Hall", etc.).
 */
public class Category {

    private final String name;

    /**
     * Optional parent/related category reference.
     * <p>
     * Note: In the current implementation this field is never set through the constructor.
     * It remains {@code null} unless you add a setter or a constructor parameter.
     * </p>
     */
    private Category category;

    /**
     * Creates a new category with the given name.
     *
     * @param name the category name
     */
    public Category(String name) {
        this.name = name;
        this.category = null;
    }

    /**
     * Returns the parent/related category (if any).
     *
     * @return the category reference (may be null)
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Returns the category name.
     *
     * @return category name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the category name.
     *
     * @return category name as string
     */
    @Override
    public String toString() {
        return name;
    }
}