package persistence;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AuditEvent;
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
 * This repository replaces a database for learning/demo purposes.
 * It stores:
 * <ul>
 *   <li>Users and Providers (providers are also stored as users for login)</li>
 *   <li>Time slots</li>
 *   <li>Appointments</li>
 *   <li>Categories</li>
 *   <li>Contact requests (messages from customers to providers)</li>
 *   <li>Audit events (admin activity log)</li>
 * </ul>
 * </p>
 *
 * <p>
 * Notes:
 * <ul>
 *   <li>This repository is NOT thread-safe.</li>
 *   <li>It returns underlying lists (mutable). For real systems, return copies.</li>
 * </ul>
 * </p>
 */
public class DataRepository {

    private final List<User> users = new LinkedList<>();
    private final List<Provider> providers = new LinkedList<>();

    private final List<TimeSlot> slots = new LinkedList<>();
    private final List<Appointment> appointments = new LinkedList<>();
    private final List<Category> categories = new LinkedList<>();

    private final List<ContactRequest> contactRequests = new LinkedList<>();

    /** Audit log used by admin to review actions. */
    private final List<AuditEvent> auditEvents = new LinkedList<>();

    /**
     * Tracks whether a user has already used their single allowed cancellation
     * for a given category.
     * <p>
     * Key format: {@code username|categoryName} (normalized lowercase/trimmed).
     * </p>
     */
    private final Map<String, Boolean> cancelUsedByUserCategory = new HashMap<>();

    /**
     * Creates a normalized key for cancellation tracking.
     *
     * @param username     username (may be null)
     * @param categoryName category name (may be null)
     * @return normalized key "username|category"
     */
    private String cancelKey(String username, String categoryName) {
        return (username == null ? "" : username.toLowerCase().trim())
                + "|"
                + (categoryName == null ? "" : categoryName.toLowerCase().trim());
    }

    /**
     * Adds a new user to the repository.
     *
     * @param user user to add
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
     * @return list of users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Adds a provider account (company/property owner).
     * <p>
     * Important: providers are also stored inside {@link #users} so they can login
     * using the existing {@code AuthService} logic.
     * </p>
     *
     * @param provider provider account
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
     * @param slot slot to add
     */
    public void addSlot(TimeSlot slot) {
        slots.add(slot);
    }

    /**
     * Adds a new appointment.
     *
     * @param appointment appointment to store
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
     * Adds a new booking category.
     *
     * @param c category to add
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
     * Adds a contact request (message) and logs it into the audit log.
     *
     * @param req contact request to add
     */
    public void addContactRequest(ContactRequest req) {
        if (req == null) return;
        contactRequests.add(req);

        // audit
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
     * @return list of contact requests
     */
    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }

    /**
     * Returns contact requests for a given provider username.
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
     * Marks a contact request as read by its id.
     *
     * @param requestId request id
     * @return true if request found and marked; false otherwise
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
     * Returns all audit log events.
     *
     * @return list of audit events
     */
    public List<AuditEvent> getAuditEvents() {
        return auditEvents;
    }

    /**
     * Adds a custom audit event to the log.
     *
     * @param e audit event
     */
    public void addAuditEvent(AuditEvent e) {
        if (e == null) return;
        auditEvents.add(e);
    }

    /**
     * Cancels a confirmed appointment and enforces:
     * <ul>
     *   <li>appointment exists in the repository</li>
     *   <li>only CONFIRMED can be cancelled</li>
     *   <li>one cancellation per user per category</li>
     * </ul>
     * Also writes an {@link AuditEvent} for the cancellation.
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