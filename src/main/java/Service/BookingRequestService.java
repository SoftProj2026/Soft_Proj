package Service;

import domain.BookingRequest;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

/**
 * Service responsible for creating booking requests and computing category-admin usernames.
 */
public class BookingRequestService {

    private final DataRepository repo;

    /**
     * Creates the booking request service.
     *
     * @param repo repository used to store and query requests and appointments
     */
    public BookingRequestService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Generates a stable short username for a category admin.
     * <p>
     * Format:
     * </p>
     * <ul>
     *   <li>Base acronym: first letter of each word</li>
     *   <li>Stable suffix: 2-digit code derived from the normalized category name</li>
     *   <li>Final: {@code acronym + code + "123"}</li>
     * </ul>
     *
     * <p>
     * This approach avoids collisions without relying on runtime state or category order.
     * </p>
     *
     * @param category category
     * @return stable category admin username
     */
    public static String categoryAdminUsername(Category category) {
        String raw = (category != null && category.getName() != null) ? category.getName() : "";
        String cleaned = raw.toLowerCase()
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) return "cat00123";

        String[] words = cleaned.split(" ");
        StringBuilder acronym = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) acronym.append(w.charAt(0));
        }

        if (acronym.length() == 0) acronym.append("cat");

        String normNoSpaces = cleaned.replace(" ", "");
        int h = stableHash(normNoSpaces);
        int code = Math.abs(h) % 100;
        String code2 = String.format("%02d", code);

        return acronym + code2 + "123";
    }

    /**
     * Computes a stable hash value for a string (deterministic across runs).
     *
     * @param s input string
     * @return hash value
     */
    private static int stableHash(String s) {
        int hash = 0;
        for (int i = 0; i < s.length(); i++) {
            hash = (hash * 31) + s.charAt(i);
        }
        return hash;
    }

    /**
     * Submits a booking request and holds the slot if accepted.
     *
     * @param requester         user submitting the request
     * @param slot              requested time slot
     * @param durationInMinutes duration in minutes
     * @param participants      number of participants
     * @return booking result (success + message)
     */
    public BookingResult submitRequest(User requester,
                                       TimeSlot slot,
                                       int durationInMinutes,
                                       int participants) {

        if (requester == null || slot == null || slot.getCategory() == null) {
            return new BookingResult(false, "Invalid request (missing user/slot/category).");
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
            return new BookingResult(false, "Not allowed: you already have MAIN + EMERGENCY (confirmed or pending) in this category.");
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

        return new BookingResult(
                true,
                "Request submitted to category admin @" + catAdmin + ". Status: PENDING_CATEGORY_ADMIN"
        );
    }
}