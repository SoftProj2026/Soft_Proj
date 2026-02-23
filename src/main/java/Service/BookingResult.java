package Service;

/**
 * Represents the outcome of an appointment booking attempt.
 *
 * <p>Encapsulates a success flag and a human-readable message that explains
 * the result (e.g., a confirmation message or the reason for failure).</p>
 */
public class BookingResult {

    private boolean success;
    private String message;

    /**
     * Constructs a new {@code BookingResult}.
     *
     * @param success {@code true} if the booking succeeded, {@code false} otherwise
     * @param message a descriptive message about the outcome
     */
    public BookingResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Returns whether the booking attempt was successful.
     *
     * @return {@code true} if the booking succeeded
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the message describing the outcome of the booking attempt.
     *
     * @return the result message
     */
    public String getMessage() {
        return message;
    }
}