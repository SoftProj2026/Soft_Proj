package service;

import domain.BookingRequest;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Service responsible for creating booking requests and enforcing booking rules.
 *
 * <p>Enforces the MAIN + EMERGENCY limit per user+category:
 * a user can have at most 2 active items (CONFIRMED + PENDING) in the same category.</p>
 *
 * @author Qussaialaw
 * @version 1.0
 */
public class BookingRequestService {

    /**
     * The repository handling data storage.
     */
    private final DataRepository repo;

    /**
     * Constructs a BookingRequestService.
     * @param repo the repository instance
     */
    public BookingRequestService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Generates a key for a category admin username, e.g. "Math" -> "M123".
     * @param category the category
     * @return the generated key, e.g. "CA123" for empty names
     */
    public static String categoryAdminKey(Category category) {
        String raw = (category != null && category.getName() != null) ? category.getName() : "";
        String cleaned = raw
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) return "CA123";

        String[] words = cleaned.split(" ");
        StringBuilder acronym = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) acronym.append(Character.toUpperCase(w.charAt(0)));
        }

        if (acronym.length() == 0) acronym.append("CA");
        return acronym + "123";
    }

    /**
     * Generates a category admin username in lowercase (e.g. "Math" -> "m123").
     * @param category the category
     * @return the username for admin
     */
    public static String categoryAdminUsername(Category category) {
        return categoryAdminKey(category).toLowerCase();
    }

    /**
     * Submits a booking request for the specified slot. The slot will be held during approval.
     * Enforces:
     * - No past slots.
     * - Must be available.
     * - Duration in range.
     * - Participants in range.
     * - MAIN + EMERGENCY limit: maximum 2 active items (confirmed + pending) per user+category.
     *
     * @param requester the user making the request
     * @param slot the time slot to book
     * @param durationInMinutes the appointment duration
     * @param participants number of participants (1-5)
     * @return BookingResult describing success/failure
     */
    public BookingResult submitRequest(User requester,
                                       TimeSlot slot,
                                       int durationInMinutes,
                                       int participants) {

        if (requester == null || slot == null || slot.getCategory() == null) {
            return new BookingResult(false, "Invalid request (missing user/slot/category).");
        }

        if (slot.getStartDateTime() != null && slot.getStartDateTime().isBefore(LocalDateTime.now())) {
            return new BookingResult(false, "You cannot book a past time slot.");
        }

        if (!slot.isAvailable()) {
            return new BookingResult(false, "This slot is not available (already booked or pending approval).");
        }

        if (participants < 1 || participants > 5) {
            return new BookingResult(false, "Participants must be between 1 and 5.");
        }

        if (durationInMinutes <= 0) {
            return new BookingResult(false, "Invalid duration.");
        }

        if (slot.getStartDateTime() == null || slot.getEndDateTime() == null) {
            return new BookingResult(false, "Invalid slot time.");
        }

        int slotMinutes = (int) Duration.between(slot.getStartDateTime(), slot.getEndDateTime()).toMinutes();
        if (slotMinutes <= 0) slotMinutes = 60;

        if (durationInMinutes > slotMinutes) {
            return new BookingResult(false, "Invalid duration. Max allowed for this slot is " + slotMinutes + " minutes.");
        }

        String username = requester.getUsername();
        String categoryName = slot.getCategory().getName();

        long confirmed = repo.countConfirmedForUserCategory(username, categoryName);
        long pending = repo.countPendingRequestsForUserCategory(username, categoryName);

        if (confirmed + pending >= 2) {
            return new BookingResult(
                    false,
                    "Not allowed: you already reached the limit (MAIN + EMERGENCY) for this category.\n" +
                            "Max allowed active items (confirmed + pending) per category is 2."
            );
        }

        String catAdmin = categoryAdminUsername(slot.getCategory());

        BookingRequest r = new BookingRequest(
                requester,
                slot,
                durationInMinutes,
                participants,
                catAdmin
        );

        slot.hold(r.getId());
        repo.addBookingRequest(r);

        return new BookingResult(true, "Request submitted. Status: PENDING_CATEGORY_ADMIN");
    }
}