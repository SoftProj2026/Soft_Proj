package Service;

import domain.BookingRequest;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.LocalDateTime;

/**
 * Service responsible for creating booking requests and enforcing request-time business rules.
 *
 * <p>This service supports the request-based booking workflow:</p>
 * <ul>
 *   <li>A user selects a mutual available slot.</li>
 *   <li>The system creates a {@link BookingRequest}.</li>
 *   <li>The selected {@link TimeSlot} is held while the request is pending approvals.</li>
 * </ul>
 *
 * <p>Key rules enforced during request submission:</p>
 * <ul>
 *   <li>Requests cannot be submitted for past time slots.</li>
 *   <li>Only available (not booked / not held) slots can be requested.</li>
 *   <li>Duration and participants must be positive values.</li>
 *   <li>MAIN + EMERGENCY rule: a user cannot exceed two active items (confirmed + pending) per category.</li>
 * </ul>
 */
public class BookingRequestService {

    private final DataRepository repo;

    /**
     * Creates a booking request service.
     *
     * @param repo repository used to store requests and query active bookings
     */
    public BookingRequestService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Generates a category-admin key based on the category name.
     *
     * <p>The key is created as:</p>
     * <ul>
     *   <li>Take the acronym from the category name words (letters only)</li>
     *   <li>Append {@code "123"}</li>
     * </ul>
     *
     * <p>If category name is missing/empty, a default {@code "CA123"} is returned.</p>
     *
     * @param category category
     * @return category admin key
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
     * Derives the category-admin username from {@link #categoryAdminKey(Category)} by lower-casing the key.
     *
     * @param category category
     * @return username used for the category admin account
     */
    public static String categoryAdminUsername(Category category) {
        return categoryAdminKey(category).toLowerCase();
    }

    /**
     * Submits a booking request for a slot and holds the slot during the approval process.
     *
     * <p>Returns a {@link BookingResult} describing whether the request was accepted and the reason/result message.</p>
     *
     * @param requester        requesting user
     * @param slot             requested slot
     * @param durationInMinutes requested duration in minutes
     * @param participants     participants count
     * @return booking result
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

        if (durationInMinutes <= 0) return new BookingResult(false, "Invalid duration.");
        if (participants <= 0) return new BookingResult(false, "Invalid participants.");

        String username = requester.getUsername();
        String categoryName = slot.getCategory().getName();

        long confirmed = repo.countConfirmedForUserCategory(username, categoryName);
        long pending = repo.countPendingRequestsForUserCategory(username, categoryName);

        if (confirmed + pending >= 2) {
            return new BookingResult(
                    false,
                    "Not allowed: you already have MAIN + EMERGENCY (confirmed or pending) in this category."
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