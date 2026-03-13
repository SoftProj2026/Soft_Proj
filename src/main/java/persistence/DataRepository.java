package persistence;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AuditEvent;
import domain.BookingRequest;
import domain.BookingRequestStatus;
import domain.Category;
import domain.ContactRequest;
import domain.Provider;
import domain.TimeSlot;
import domain.User;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * In-memory repository that stores the application's runtime data.
 *
 * <p>This repository replaces a database for learning/demo purposes. It stores:</p>
 * <ul>
 *   <li>Users and Providers (providers are also stored as users for login)</li>
 *   <li>Time slots</li>
 *   <li>Appointments</li>
 *   <li>Categories</li>
 *   <li>Contact requests (messages from customers to providers)</li>
 *   <li>Audit events (admin activity log)</li>
 *   <li>Booking requests (2-step approval workflow)</li>
 * </ul>
 *
 * <h2>Booking Policy</h2>
 * <p>For each user within the same category:</p>
 * <ul>
 *   <li>At most <b>two</b> confirmed bookings are allowed.</li>
 *   <li>The first confirmed booking is considered <b>MAIN</b>.</li>
 *   <li>The second confirmed booking is considered <b>Emergency (طوارئ)</b>.</li>
 * </ul>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>This repository is <b>not thread-safe</b>.</li>
 *   <li>Most getters return the underlying mutable lists (demo purposes).</li>
 * </ul>
 */
public class DataRepository {

    private final List<User> users = new LinkedList<>();
    private final List<Provider> providers = new LinkedList<>();

    private final List<TimeSlot> slots = new LinkedList<>();
    private final List<Appointment> appointments = new LinkedList<>();
    private final List<Category> categories = new LinkedList<>();

    private final List<ContactRequest> contactRequests = new LinkedList<>();
    private final List<AuditEvent> auditEvents = new LinkedList<>();
    private final List<BookingRequest> bookingRequests = new LinkedList<>();

    private final Map<String, Boolean> cancelUsedByUserCategory = new HashMap<>();

    /**
     * Creates a normalized key for cancellation tracking.
     *
     * @param username     username (may be null)
     * @param categoryName category name (may be null)
     * @return normalized key in the form "username|category"
     */
    private String cancelKey(String username, String categoryName) {
        return (username == null ? "" : username.toLowerCase().trim())
                + "|"
                + (categoryName == null ? "" : categoryName.toLowerCase().trim());
    }

    /**
     * Purges the given categories (by name) from the repository.
     *
     * <p>This method removes, for the categories being purged:</p>
     * <ul>
     *   <li>{@link Category} objects from {@link #getCategories()}</li>
     *   <li>{@link TimeSlot} objects whose category matches</li>
     *   <li>{@link Appointment} objects whose slot category matches</li>
     *   <li>{@link BookingRequest} objects whose slot category matches</li>
     *   <li>Cancellation-usage entries for the category (one-cancel rule)</li>
     *   <li>Seeded category-admin users derived from those categories</li>
     *   <li>{@link AuditEvent} entries that reference those categories (best-effort string match)</li>
     * </ul>
     *
     * <p>This operation is idempotent (safe to call multiple times).</p>
     *
     * <p><b>Important:</b> Category matching is case-insensitive and based on trimmed names.</p>
     *
     * @param categoryNamesToRemove category names to remove
     * @return number of {@link Category} entries removed from the categories list
     */
    public int purgeCategories(Set<String> categoryNamesToRemove) {
        if (categoryNamesToRemove == null || categoryNamesToRemove.isEmpty()) return 0;

        Set<String> norm = categoryNamesToRemove.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        if (norm.isEmpty()) return 0;

        Set<String> matchedNames = categories.stream()
                .filter(c -> c != null && c.getName() != null)
                .map(c -> c.getName().trim())
                .filter(n -> norm.contains(n.toLowerCase()))
                .collect(Collectors.toSet());

        int beforeCats = categories.size();
        categories.removeIf(c ->
                c != null
                        && c.getName() != null
                        && norm.contains(c.getName().trim().toLowerCase())
        );
        int removedCats = beforeCats - categories.size();

        slots.removeIf(s ->
                s != null
                        && s.getCategory() != null
                        && s.getCategory().getName() != null
                        && norm.contains(s.getCategory().getName().trim().toLowerCase())
        );

        appointments.removeIf(a ->
                a != null
                        && a.getSlot() != null
                        && a.getSlot().getCategory() != null
                        && a.getSlot().getCategory().getName() != null
                        && norm.contains(a.getSlot().getCategory().getName().trim().toLowerCase())
        );

        bookingRequests.removeIf(r ->
                r != null
                        && r.getSlot() != null
                        && r.getSlot().getCategory() != null
                        && r.getSlot().getCategory().getName() != null
                        && norm.contains(r.getSlot().getCategory().getName().trim().toLowerCase())
        );

        cancelUsedByUserCategory.keySet().removeIf(k -> {
            if (k == null) return false;
            String[] parts = k.split("\\|", 2);
            if (parts.length < 2) return false;
            String cat = parts[1] != null ? parts[1].trim().toLowerCase() : "";
            return norm.contains(cat);
        });

        Set<String> categoryAdminUsernames = matchedNames.stream()
                .map(DataRepository::deriveCategoryAdminUsername)
                .collect(Collectors.toSet());

        users.removeIf(u ->
                u != null
                        && u.getUsername() != null
                        && categoryAdminUsernames.contains(u.getUsername().trim().toLowerCase())
        );

        providers.removeIf(p ->
                p != null
                        && p.getUsername() != null
                        && categoryAdminUsernames.contains(p.getUsername().trim().toLowerCase())
        );

        auditEvents.removeIf(e -> {
            if (e == null) return false;

            String target = e.getTarget() != null ? e.getTarget().trim().toLowerCase() : "";
            String details = e.getDetails() != null ? e.getDetails().trim().toLowerCase() : "";

            for (String cat : norm) {
                if (cat.isEmpty()) continue;
                if (target.equals(cat)) return true;
                if (details.contains(cat)) return true;
            }
            return false;
        });

        return removedCats;
    }

    /**
     * Derives the seeded category-admin username for a category name using the same logic as
     * {@code BookingRequestService.categoryAdminUsername(Category)} but without depending on Service layer code.
     *
     * <p>The derived username is:</p>
     * <ul>
     *   <li>Acronym of the category words (letters/numbers only), uppercased</li>
     *   <li>Concatenated with {@code "123"}</li>
     *   <li>Lower-cased as a username</li>
     * </ul>
     *
     * @param categoryName category name
     * @return derived category admin username (never null)
     */
    private static String deriveCategoryAdminUsername(String categoryName) {
        String raw = categoryName != null ? categoryName : "";
        String cleaned = raw
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) return "ca123";

        String[] words = cleaned.split(" ");
        StringBuilder acronym = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) acronym.append(Character.toUpperCase(w.charAt(0)));
        }

        if (acronym.length() == 0) acronym.append("CA");
        return (acronym + "123").toLowerCase();
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }

    public void addProvider(Provider provider) {
        if (provider == null) return;
        providers.add(provider);
        users.add(provider);
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public List<TimeSlot> getSlots() {
        return slots;
    }

    public void addSlot(TimeSlot slot) {
        slots.add(slot);
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void addCategory(Category c) {
        categories.add(c);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void addContactRequest(ContactRequest req) {
        if (req == null) return;
        contactRequests.add(req);

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                req.getFromUsername(),
                req.getToProviderUsername(),
                req.getMessage()
        ));
    }

    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }

    public List<ContactRequest> getRequestsForProvider(String providerUsername) {
        String u = providerUsername != null ? providerUsername.trim() : "";
        return contactRequests.stream()
                .filter(r -> r.getToProviderUsername().equalsIgnoreCase(u))
                .collect(Collectors.toList());
    }

    public boolean markRequestRead(int requestId) {
        for (ContactRequest r : contactRequests) {
            if (r.getId() == requestId) {
                r.markRead();
                return true;
            }
        }
        return false;
    }

    public List<AuditEvent> getAuditEvents() {
        return auditEvents;
    }

    public void addAuditEvent(AuditEvent e) {
        if (e == null) return;
        auditEvents.add(e);
    }

    public void addBookingRequest(BookingRequest r) {
        if (r == null) return;
        bookingRequests.add(r);

        String user = (r.getRequester() != null) ? r.getRequester().getUsername() : "unknown";
        String category = (r.getSlot() != null && r.getSlot().getCategory() != null)
                ? r.getSlot().getCategory().getName()
                : "N/A";

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                user,
                category,
                "Submitted booking request #" + r.getId() + " (PENDING_CATEGORY_ADMIN)"
        ));
    }

    public List<BookingRequest> getBookingRequests() {
        return bookingRequests;
    }

    private BookingRequest findRequest(int id) {
        for (BookingRequest r : bookingRequests) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    public List<BookingRequest> getRequestsForCategoryAdmin(String adminUsername) {
        String u = adminUsername != null ? adminUsername.trim() : "";
        return bookingRequests.stream()
                .filter(r -> r.getStatus() == BookingRequestStatus.PENDING_CATEGORY_ADMIN)
                .filter(r -> r.getCategoryAdminUsername() != null
                        && r.getCategoryAdminUsername().equalsIgnoreCase(u))
                .collect(Collectors.toList());
    }

    public List<BookingRequest> getRequestsForBigAdmin() {
        return bookingRequests.stream()
                .filter(r -> r.getStatus() == BookingRequestStatus.PENDING_BIG_ADMIN)
                .collect(Collectors.toList());
    }

    public String approveByCategoryAdmin(int requestId, String adminUsername) {
        BookingRequest r = findRequest(requestId);
        if (r == null) return "Request not found.";

        if (r.getStatus() != BookingRequestStatus.PENDING_CATEGORY_ADMIN) {
            return "Request is not pending category admin.";
        }

        if (adminUsername == null
                || r.getCategoryAdminUsername() == null
                || !r.getCategoryAdminUsername().equalsIgnoreCase(adminUsername.trim())) {
            return "Not allowed: this request is not assigned to you.";
        }

        r.approveByCategoryAdmin(adminUsername);

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                adminUsername,
                (r.getSlot() != null && r.getSlot().getCategory() != null) ? r.getSlot().getCategory().getName() : "N/A",
                "Category admin approved request #" + r.getId() + " -> sent to big admin"
        ));

        return "Approved by category admin. Sent to big admin.";
    }

    public String rejectByCategoryAdmin(int requestId, String adminUsername, String reason) {
        BookingRequest r = findRequest(requestId);
        if (r == null) return "Request not found.";

        if (r.getStatus() != BookingRequestStatus.PENDING_CATEGORY_ADMIN) {
            return "Request is not pending category admin.";
        }

        if (adminUsername == null
                || r.getCategoryAdminUsername() == null
                || !r.getCategoryAdminUsername().equalsIgnoreCase(adminUsername.trim())) {
            return "Not allowed: this request is not assigned to you.";
        }

        r.rejectByCategoryAdmin(adminUsername, reason);

        if (r.getSlot() != null) r.getSlot().releaseHold();

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                adminUsername,
                (r.getSlot() != null && r.getSlot().getCategory() != null) ? r.getSlot().getCategory().getName() : "N/A",
                "Category admin rejected request #" + r.getId()
        ));

        return "Rejected by category admin. Slot is available again.";
    }

    public String approveByBigAdmin(int requestId, String adminUsername) {
        BookingRequest r = findRequest(requestId);
        if (r == null) return "Request not found.";

        if (r.getStatus() != BookingRequestStatus.PENDING_BIG_ADMIN) {
            return "Request is not pending big admin.";
        }

        if (r.getRequester() == null || r.getSlot() == null || r.getSlot().getCategory() == null) {
            return "Invalid request data (missing requester/slot/category).";
        }

        String username = r.getRequester().getUsername();
        String categoryName = r.getSlot().getCategory().getName();

        long confirmed = countConfirmedForUserCategory(username, categoryName);

        if (confirmed >= 2) {
            r.getSlot().releaseHold();
            r.rejectByBigAdmin(adminUsername, "Limit exceeded: already has 2 confirmed bookings in this category.");
            return "Rejected: user already has 2 confirmed bookings in this category (MAIN + Emergency).";
        }

        String label = (confirmed == 0) ? "MAIN" : "Emergency";

        Appointment appt = new Appointment(
                r.getRequester(),
                r.getSlot(),
                r.getDurationInMinutes(),
                r.getParticipants()
        );

        appt.confirm();

        appointments.add(appt);

        r.approveByBigAdmin(adminUsername);

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.APPOINTMENT_CONFIRMED,
                adminUsername != null ? adminUsername.trim() : "admin",
                categoryName,
                "Big admin approved request #" + r.getId()
                        + " -> Confirmed appointment #" + appt.getId()
                        + " (" + label + ")"
                        + " for user @" + username
        ));

        return "Final approval done. Appointment confirmed (" + label + ").";
    }

    public String rejectByBigAdmin(int requestId, String adminUsername, String reason) {
        BookingRequest r = findRequest(requestId);
        if (r == null) return "Request not found.";

        if (r.getStatus() != BookingRequestStatus.PENDING_BIG_ADMIN) {
            return "Request is not pending big admin.";
        }

        r.rejectByBigAdmin(adminUsername, reason);

        if (r.getSlot() != null) r.getSlot().releaseHold();

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                adminUsername != null ? adminUsername.trim() : "admin",
                (r.getSlot() != null && r.getSlot().getCategory() != null) ? r.getSlot().getCategory().getName() : "N/A",
                "Big admin rejected request #" + r.getId()
        ));

        return "Rejected by big admin. Slot is available again.";
    }

    public long countConfirmedForUserCategory(String username, String categoryName) {
        String u = username != null ? username.trim() : "";
        String cat = categoryName != null ? categoryName.trim() : "";

        return appointments.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .filter(a -> a.getUser() != null && a.getUser().getUsername().equalsIgnoreCase(u))
                .filter(a -> a.getSlot() != null && a.getSlot().getCategory() != null)
                .filter(a -> a.getSlot().getCategory().getName().equalsIgnoreCase(cat))
                .count();
    }

    public long countPendingRequestsForUserCategory(String username, String categoryName) {
        String u = username != null ? username.trim() : "";
        String cat = categoryName != null ? categoryName.trim() : "";

        return bookingRequests.stream()
                .filter(r -> r.getRequester() != null && r.getRequester().getUsername().equalsIgnoreCase(u))
                .filter(r -> r.getSlot() != null && r.getSlot().getCategory() != null)
                .filter(r -> r.getSlot().getCategory().getName().equalsIgnoreCase(cat))
                .filter(r -> r.getStatus() == BookingRequestStatus.PENDING_CATEGORY_ADMIN
                        || r.getStatus() == BookingRequestStatus.PENDING_BIG_ADMIN)
                .count();
    }

    public String cancelAppointment(Appointment appointment) {
        if (appointment == null) return "Invalid booking.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return "Only CONFIRMED bookings can be cancelled.";
        }

        if (appointment.getSlot() == null || appointment.getSlot().getStartDateTime() == null) {
            return "Cannot cancel booking (missing slot time).";
        }

        if (!appointment.getSlot().getStartDateTime().isAfter(LocalDateTime.now())) {
            return "Only FUTURE bookings can be cancelled.";
        }

        String username = appointment.getUser() != null ? appointment.getUser().getUsername() : null;

        String categoryName = null;
        if (appointment.getSlot() != null && appointment.getSlot().getCategory() != null) {
            categoryName = appointment.getSlot().getCategory().getName();
        }

        if (username == null || categoryName == null) {
            return "Cannot cancel booking (missing user or category).";
        }

        String key = cancelKey(username, categoryName);

        boolean alreadyUsed = cancelUsedByUserCategory.getOrDefault(key, false);
        if (alreadyUsed) {
            return "Cancellation not allowed. You can only cancel ONE booking in category \"" + categoryName + "\".";
        }

        cancelUsedByUserCategory.put(key, true);

        appointment.cancel();

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.APPOINTMENT_CANCELLED,
                username,
                categoryName,
                "Cancelled appointment #" + appointment.getId()
        ));

        return "Booking cancelled successfully (one cancellation used for category \"" + categoryName + "\").";
    }

    public String adminCancelAppointment(Appointment appointment, String adminUsername) {
        if (appointment == null) return "Invalid booking.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return "Only CONFIRMED bookings can be cancelled.";
        }

        if (appointment.getSlot() == null || appointment.getSlot().getStartDateTime() == null) {
            return "Cannot cancel booking (missing slot time).";
        }

        if (!appointment.getSlot().getStartDateTime().isAfter(LocalDateTime.now())) {
            return "Only FUTURE bookings can be cancelled.";
        }

        String categoryName = (appointment.getSlot() != null && appointment.getSlot().getCategory() != null)
                ? appointment.getSlot().getCategory().getName()
                : "N/A";

        String user = (appointment.getUser() != null && appointment.getUser().getUsername() != null)
                ? appointment.getUser().getUsername()
                : "unknown";

        appointment.cancel();

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.APPOINTMENT_CANCELLED,
                adminUsername != null ? adminUsername.trim() : "admin",
                categoryName,
                "Admin cancelled appointment #" + appointment.getId() + " for user @" + user
        ));

        return "Appointment cancelled by admin.";
    }

    public String modifyAppointment(Appointment appointment,
                                   TimeSlot newSlot,
                                   int newDurationInMinutes,
                                   int newParticipants,
                                   String actorUsername) {

        if (appointment == null || newSlot == null) return "Invalid modification.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return "Only CONFIRMED bookings can be modified.";
        }

        if (appointment.getSlot() == null || appointment.getSlot().getStartDateTime() == null) {
            return "Cannot modify booking (missing current slot time).";
        }

        if (!appointment.getSlot().getStartDateTime().isAfter(LocalDateTime.now())) {
            return "Only FUTURE bookings can be modified.";
        }

        if (newSlot.getStartDateTime() == null || !newSlot.getStartDateTime().isAfter(LocalDateTime.now())) {
            return "You cannot move a booking to a past time slot.";
        }

        if (!newSlot.isAvailable()) {
            return "Selected new time slot is not available.";
        }

        if (newDurationInMinutes <= 0) return "Invalid duration.";
        if (newParticipants <= 0) return "Invalid participants.";

        TimeSlot oldSlot = appointment.getSlot();

        oldSlot.cancel();
        newSlot.book();

        Appointment updated = new Appointment(
                appointment.getUser(),
                newSlot,
                newDurationInMinutes,
                newParticipants
        );
        updated.confirm();

        int idx = appointments.indexOf(appointment);
        appointments.set(idx, updated);

        String categoryName = (newSlot.getCategory() != null) ? newSlot.getCategory().getName() : "N/A";

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.APPOINTMENT_CONFIRMED,
                actorUsername != null ? actorUsername.trim() : "system",
                categoryName,
                "Modified appointment #" + appointment.getId() + " -> new appointment #" + updated.getId()
        ));

        return "Booking modified successfully.";
    }
}