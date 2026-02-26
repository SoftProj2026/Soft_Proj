package Service;

import domain.Appointment;
import domain.TimeSlot;

import java.time.LocalTime;

/**
 * Blocks booking during a defined break interval (e.g., 12:00 to 13:00).
 */
public class BlockedSlotsRule implements BookingRuleStrategy {

    private String errorMessage = "This time is not available for booking.";

    private static final LocalTime BREAK_START = LocalTime.of(12, 0);
    private static final LocalTime BREAK_END = LocalTime.of(13, 0);

    /**
     * Returns a custom block message if the slot start time is inside the break window.
     *
     * @param slot the slot to check
     * @return a message if blocked; otherwise null
     */
    public String getBlockMessageIfBlocked(TimeSlot slot) {
        if (slot == null) return null;

        LocalTime start = slot.getStartDateTime().toLocalTime();

        if (!start.isBefore(BREAK_START) && start.isBefore(BREAK_END)) {
            return "ممنوع الحجز من 12:00 إلى 13:00 (استراحة).";
        }

        return null;
    }

    /**
     * Validates that the appointment is not during the blocked interval.
     *
     * @param appointment appointment to validate
     * @return true if allowed; false otherwise
     */
    @Override
    public boolean isValid(Appointment appointment) {
        if (appointment == null) return false;

        String msg = getBlockMessageIfBlocked(appointment.getSlot());
        if (msg != null) {
            errorMessage = msg;
            return false;
        }
        return true;
    }

    /**
     * Returns the error message for blocked slots.
     *
     * @return error message
     */
    @Override
    public String getErrorMessage() {
        return errorMessage;
    }
}