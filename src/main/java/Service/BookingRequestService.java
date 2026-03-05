package Service;

import domain.BookingRequest;
import domain.Category;
import domain.TimeSlot;
import domain.User;
import persistence.DataRepository;

import java.time.LocalDateTime;

public class BookingRequestService {

    private final DataRepository repo;

    public BookingRequestService(DataRepository repo) {
        this.repo = repo;
    }

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

    public static String categoryAdminUsername(Category category) {
        return categoryAdminKey(category).toLowerCase();
    }

    /**
     * IMPORTANT: return type is Service.BookingResult (your project class),
     * not an inner BookingResult.
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
            return new BookingResult(false,
                    "Not allowed: you already have MAIN + EMERGENCY (confirmed or pending) in this category.");
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