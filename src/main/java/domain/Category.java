package domain;

/**
 * Represents a booking category (for example, "Doctor Appointment", "Conference Hall", etc.).
 *
 * <p>A category is used to group {@link TimeSlot} entries and bookings under a specific service type.</p>
 *
 * @author s12219530-cpu (remaa)
 * @version 1.0
 */
public class Category {

    /**
     * Category name.
     */
    private final String name;

    /**
     * Optional parent or related category reference (may be {@code null}).
     */
    private Category parentCategory;
    /**
     * Creates a new category with the given name.
     *
     * @param name the category name
     */
    public Category(String name) {
        this.name = name;
        this. parentCategory = null;
    }

    /**
     * Returns the parent or related category reference.
     *
     * @return related category (may be {@code null})
     */
    public Category getCategory() {
        return parentCategory;
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
     * Returns the category name as a string.
     *
     * @return category name
     */
    @Override
    public String toString() {
        return name;
    }
}