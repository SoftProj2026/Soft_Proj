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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * In-memory repository that stores the application's runtime data.
 * <p>
 * This repository replaces a database for learning/demo purposes. It stores:
 * </p>
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
 * <p>
 * For each user within the same category:
 * </p>
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
     * Adds a new user to the repository.
     *
     * @param user user to add (may be null)
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Returns all users.
     * <p>
     * Warning: returns the underlying list.
     * </p>
     *
     * @return users list
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Adds a provider account and also registers it as a user
     * (so the provider can login using the same auth logic).
     *
     * @param provider provider account to add (may be null)
     */
    public void addProvider(Provider provider) {
        if (provider == null) return;
        providers.add(provider);
        users.add(provider);
    }

    /**
     * Returns the list of provider accounts.
     *
     * @return providers list
     */
    public List<Provider> getProviders() {
        return providers;
    }

    /**
     * Returns all time slots.
     *
     * @return slots list
     */
    public List<TimeSlot> getSlots() {
        return slots;
    }

    /**
     * Adds a new time slot.
     *
     * @param slot slot to add (may be null)
     */
    public void addSlot(TimeSlot slot) {
        slots.add(slot);
    }

    /**
     * Adds a new appointment.
     *
     * @param appointment appointment to add (may be null)
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    /**
     * Returns all appointments.
     *
     * @return appointments list
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Adds a new category.
     *
     * @param c category to add (may be null)
     */
    public void addCategory(Category c) {
        categories.add(c);
    }

    /**
     * Returns all categories.
     *
     * @return categories list
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * Adds a new contact request and logs a corresponding audit event.
     *
     * @param req contact request to add (may be null)
     */
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

    /**
     * Returns all contact requests.
     *
     * @return contact requests list
     */
    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }

    /**
     * Returns contact requests addressed to a specific provider.
     *
     * @param providerUsername provider username
     * @return list of requests addressed to that provider
     */
    public List<ContactRequest> getRequestsForProvider(String providerUsername) {
        String u = providerUsername != null ? providerUsername.trim() : "";
        return contactRequests.stream()
                .filter(r -> r.getToProviderUsername().equalsIgnoreCase(u))
                .collect(Collectors.toList());
    }

    /**
     * Marks a contact request as read by id.
     *
     * @param requestId request id
     * @return true if found and marked read; false otherwise
     */
    public boolean markRequestRead(int requestId) {
        for (ContactRequest r : contactRequests) {
            if (r.getId() == requestId) {
                r.markRead();
                return true;
            }
        }
        return false;
    }

    /**
     * Returns all audit events.
     *
     * @return audit events list
     */
    public List<AuditEvent> getAuditEvents() {
        return auditEvents;
    }

    /**
     * Adds an audit event.
     *
     * @param e audit event to add (may be null)
     */
    public void addAuditEvent(AuditEvent e) {
        if (e == null) return;
        auditEvents.add(e);
    }

    /**
     * Adds a booking request and logs an audit entry.
     *
     * @param r booking request (may be null)
     */
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

    /**
     * Returns all booking requests.
     *
     * @return booking requests list
     */
    public List<BookingRequest> getBookingRequests() {
        return bookingRequests;
    }

    /**
     * Finds a booking request by id.
     *
     * @param id request id
     * @return request if found; otherwise null
     */
    private BookingRequest findRequest(int id) {
        for (BookingRequest r : bookingRequests) {
            if (r.getId() == id) return r;
        }
        return null;
    }

    /**
     * Returns requests visible to a category admin (strictly assigned to that admin username).
     *
     * @param adminUsername category admin username
     * @return list of pending requests assigned to this admin
     */
    public List<BookingRequest> getRequestsForCategoryAdmin(String adminUsername) {
        String u = adminUsername != null ? adminUsername.trim() : "";
        return bookingRequests.stream()
                .filter(r -> r.getStatus() == BookingRequestStatus.PENDING_CATEGORY_ADMIN)
                .filter(r -> r.getCategoryAdminUsername() != null
                        && r.getCategoryAdminUsername().equalsIgnoreCase(u))
                .collect(Collectors.toList());
    }

    /**
     * Returns requests visible to the big admin.
     *
     * @return list of requests pending big admin approval
     */
    public List<BookingRequest> getRequestsForBigAdmin() {
        return bookingRequests.stream()
                .filter(r -> r.getStatus() == BookingRequestStatus.PENDING_BIG_ADMIN)
                .collect(Collectors.toList());
    }

    /**
     * Approves a request by its assigned category admin and forwards it to the big admin.
     *
     * @param requestId     booking request id
     * @param adminUsername acting admin username
     * @return user-friendly result message
     */
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

    /**
     * Rejects a request by its assigned category admin and releases the slot hold.
     *
     * @param requestId     booking request id
     * @param adminUsername acting admin username
     * @param reason        optional reject reason (may be null)
     * @return user-friendly result message
     */
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

    /**
     * Approves a request by the big admin, confirms the appointment, and books the slot.
     * <p>
     * This method enforces the policy of at most two confirmed bookings per user per category.
     * The first is labeled MAIN and the second is labeled Emergency (طوارئ) in the audit details.
     * </p>
     *
     * @param requestId     booking request id
     * @param adminUsername acting admin username (may be null)
     * @return user-friendly result message
     */
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

    /**
     * Rejects a request by the big admin and releases the slot hold.
     *
     * @param requestId     booking request id
     * @param adminUsername acting admin username (may be null)
     * @param reason        optional reject reason (may be null)
     * @return user-friendly result message
     */
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

    /**
     * Counts confirmed appointments for a user within a given category.
     *
     * @param username     username
     * @param categoryName category name
     * @return number of confirmed appointments
     */
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

    /**
     * Counts pending requests for a user within a given category.
     *
     * @param username     username
     * @param categoryName category name
     * @return number of pending requests
     */
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

    /**
     * Cancels a confirmed appointment and enforces:
     * <ul>
     *   <li>Only appointments stored in the repository can be cancelled</li>
     *   <li>Only CONFIRMED appointments can be cancelled</li>
     *   <li>One cancellation per user per category</li>
     * </ul>
     * The cancellation is also logged as an audit event.
     *
     * @param appointment appointment to cancel
     * @return user-friendly result message
     */
    public String cancelAppointment(Appointment appointment) {
        if (appointment == null) return "Invalid booking.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            return "Only CONFIRMED bookings can be cancelled.";
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
}