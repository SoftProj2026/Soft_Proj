package service;

/**
 * Represents the outcome of a booking attempt.
 * @author remaa
 * @version 1.0
 */
public class BookingResult {

    private boolean success;
    private String message;

    /**
     * Creates a booking result.
     *
     * @param success true if booking succeeded; false otherwise
     * @param message user-friendly message describing the outcome
     */
    public BookingResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Indicates whether the operation succeeded.
     *
     * @return true if successful; false otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the result message.
     *
     * @return result message
     */
    public String getMessage() {
        return message;
    }
}