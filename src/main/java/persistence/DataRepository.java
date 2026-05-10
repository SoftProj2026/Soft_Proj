package persistence;

import domain.Appointment;
import domain.AppointmentStatus;
import domain.AppointmentType;
import domain.AuditEvent;
import domain.BookingRequest;
import domain.BookingRequestStatus;
import domain.Category;
import domain.ContactRequest;
import domain.Provider;
import domain.TimeSlot;
import domain.User;
import service.AppointmentTypeRules;
import service.EmailSender;
import service.SmtpEmailSender;

import java.time.LocalDate;
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
 * <p>This repository maintains collections for users, providers, categories, slots, appointments, booking requests,
 * contact requests, and audit events. It also contains utility operations used by the approval workflow and booking
 * lifecycle management.</p>
 *
 * @author s12219530-cpu (remaa)
 * @version 1.0
 */
public class DataRepository {

    /**
     * All user accounts in the system.
     */
    private final List<User> users = new LinkedList<>();

    /**
     * All provider accounts in the system.
     */
    private final List<Provider> providers = new LinkedList<>();

    /**
     * All time slots in the system.
     */
    private final List<TimeSlot> slots = new LinkedList<>();

    /**
     * All appointments in the system.
     */
    private final List<Appointment> appointments = new LinkedList<>();

    /**
     * All categories in the system.
     */
    private final List<Category> categories = new LinkedList<>();

    /**
     * All contact requests (messages) in the system.
     */
    private final List<ContactRequest> contactRequests = new LinkedList<>();

    /**
     * All audit events in the system.
     */
    private final List<AuditEvent> auditEvents = new LinkedList<>();

    /**
     * All booking requests in the system.
     */
    private final List<BookingRequest> bookingRequests = new LinkedList<>();

    /**
     * Tracks whether the user already used their one allowed cancellation per category.
     */
    private final Map<String, Boolean> cancelUsedByUserCategory = new HashMap<>();

    /**
     * Phone number included in review appointment notification emails.
     */
    private static final String COMPANY_REVIEW_PHONE = "059-507-9549";

    /**
     * Creates a normalized key for cancellation tracking.
     *
     * @param username     the username
     * @param categoryName the category name
     * @return normalized cancellation key
     */
    private String cancelKey(String username, String categoryName) {
        return (username == null ? "" : username.toLowerCase().trim())
                + "|"
                + (categoryName == null ? "" : categoryName.toLowerCase().trim());
    }

    /**
     * Purges removed categories and any dependent objects from the repository.
     *
     * @param categoryNamesToRemove category names to remove
     * @return number of categories removed
     */
    public int purgeCategories(Set<String> categoryNamesToRemove) {
        if (categoryNamesToRemove == null || categoryNamesToRemove.isEmpty()) {
            return 0;
        }

        Set<String> norm = categoryNamesToRemove.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> s.trim().toLowerCase())
                .collect(Collectors.toSet());

        if (norm.isEmpty()) {
            return 0;
        }

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
            if (k == null) {
                return false;
            }
            String[] parts = k.split("\\|", 2);
            if (parts.length < 2) {
                return false;
            }
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
            if (e == null) {
                return false;
            }

            String target = e.getTarget() != null ? e.getTarget().trim().toLowerCase() : "";
            String details = e.getDetails() != null ? e.getDetails().trim().toLowerCase() : "";

            for (String cat : norm) {
                if (cat.isEmpty()) {
                    continue;
                }
                if (target.equals(cat)) {
                    return true;
                }
                if (details.contains(cat)) {
                    return true;
                }
            }
            return false;
        });

        return removedCats;
    }

    /**
     * Derives a deterministic category-admin username from a category name.
     *
     * @param categoryName category name
     * @return derived category-admin username
     */
    private static String deriveCategoryAdminUsername(String categoryName) {
        String raw = categoryName != null ? categoryName : "";
        String cleaned = raw
                .replaceAll("[^A-Za-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (cleaned.isEmpty()) {
            return "ca123";
        }

        String[] words = cleaned.split(" ");
        StringBuilder acronym = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                acronym.append(Character.toUpperCase(w.charAt(0)));
            }
        }

        if (acronym.length() == 0) {
            acronym.append("CA");
        }
        return (acronym + "123").toLowerCase();
    }

    /**
     * Adds a user account.
     *
     * @param user user to add
     */
    public void addUser(User user) {
        users.add(user);
    }

    /**
     * Returns all users.
     *
     * @return list of users
     */
    public List<User> getUsers() {
        return users;
    }

    /**
     * Adds a provider account and also adds it to the user list.
     *
     * @param provider provider to add
     */
    public void addProvider(Provider provider) {
        if (provider == null) {
            return;
        }
        providers.add(provider);
        users.add(provider);
    }

    /**
     * Returns all providers.
     *
     * @return list of providers
     */
    public List<Provider> getProviders() {
        return providers;
    }

    /**
     * Returns all time slots.
     *
     * @return list of slots
     */
    public List<TimeSlot> getSlots() {
        return slots;
    }

    /**
     * Adds a time slot.
     *
     * @param slot slot to add
     */
    public void addSlot(TimeSlot slot) {
        slots.add(slot);
    }

    /**
     * Adds an appointment.
     *
     * @param appointment appointment to add
     */
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    /**
     * Returns all appointments.
     *
     * @return list of appointments
     */
    public List<Appointment> getAppointments() {
        return appointments;
    }

    /**
     * Adds a category.
     *
     * @param c category to add
     */
    public void addCategory(Category c) {
        categories.add(c);
    }

    /**
     * Returns all categories.
     *
     * @return list of categories
     */
    public List<Category> getCategories() {
        return categories;
    }

    /**
     * Adds a contact request and records an audit event.
     *
     * @param req contact request to add
     */
    public void addContactRequest(ContactRequest req) {
        if (req == null) {
            return;
        }
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
     * @return list of contact requests
     */
    public List<ContactRequest> getContactRequests() {
        return contactRequests;
    }

    /**
     * Returns contact requests for a given provider.
     *
     * @param providerUsername provider username
     * @return list of contact requests
     */
    public List<ContactRequest> getRequestsForProvider(String providerUsername) {
        String u = providerUsername != null ? providerUsername.trim() : "";
        return contactRequests.stream()
                .filter(r -> r.getToProviderUsername().equalsIgnoreCase(u))
                .collect(Collectors.toList());
    }

    /**
     * Marks a contact request as read.
     *
     * @param requestId contact request id
     * @return {@code true} if updated; otherwise {@code false}
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
     * @return list of audit events
     */
    public List<AuditEvent> getAuditEvents() {
        return auditEvents;
    }

    /**
     * Adds an audit event.
     *
     * @param e audit event
     */
    public void addAuditEvent(AuditEvent e) {
        if (e == null) {
            return;
        }
        auditEvents.add(e);
    }

    /**
     * Adds a booking request and records an audit event.
     *
     * @param r booking request to add
     */
    public void addBookingRequest(BookingRequest r) {
        if (r == null) {
            return;
        }
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
     * @return list of booking requests
     */
    public List<BookingRequest> getBookingRequests() {
        return bookingRequests;
    }

    /**
     * Finds a booking request by id.
     *
     * @param id request id
     * @return booking request or {@code null}
     */
    private BookingRequest findRequest(int id) {
        for (BookingRequest r : bookingRequests) {
            if (r.getId() == id) {
                return r;
            }
        }
        return null;
    }

    /**
     * Returns requests pending category-admin decision that are assigned to a given admin.
     *
     * @param adminUsername admin username
     * @return list of requests
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
     * Returns requests pending big-admin decision.
     *
     * @return list of requests
     */
    public List<BookingRequest> getRequestsForBigAdmin() {
        return bookingRequests.stream()
                .filter(r -> r.getStatus() == BookingRequestStatus.PENDING_BIG_ADMIN)
                .collect(Collectors.toList());
    }

    /**
     * Approves a request by category admin and forwards it to big admin.
     *
     * @param requestId     request id
     * @param adminUsername category admin username
     * @return result message
     */
    public String approveByCategoryAdmin(int requestId, String adminUsername) {
        BookingRequest r = findRequest(requestId);
        if (r == null) {
            return "Request not found.";
        }

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
     * Rejects a request by category admin and releases the held slot.
     *
     * @param requestId     request id
     * @param adminUsername category admin username
     * @param reason        rejection reason
     * @return result message
     */
    public String rejectByCategoryAdmin(int requestId, String adminUsername, String reason) {
        BookingRequest r = findRequest(requestId);
        if (r == null) {
            return "Request not found.";
        }

        if (r.getStatus() != BookingRequestStatus.PENDING_CATEGORY_ADMIN) {
            return "Request is not pending category admin.";
        }

        if (adminUsername == null
                || r.getCategoryAdminUsername() == null
                || !r.getCategoryAdminUsername().equalsIgnoreCase(adminUsername.trim())) {
            return "Not allowed: this request is not assigned to you.";
        }

        r.rejectByCategoryAdmin(adminUsername, reason);

        if (r.getSlot() != null) {
            r.getSlot().releaseHold();
        }

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                adminUsername,
                (r.getSlot() != null && r.getSlot().getCategory() != null) ? r.getSlot().getCategory().getName() : "N/A",
                "Category admin rejected request #" + r.getId()
        ));

        return "Rejected by category admin. Slot is available again.";
    }

    /**
     * Approves a request by big admin, confirms it, and creates a confirmed appointment.
     *
     * @param requestId     request id
     * @param adminUsername big admin username
     * @return result message
     */
    public String approveByBigAdmin(int requestId, String adminUsername) {
        BookingRequest r = findRequest(requestId);
        if (r == null) {
            return "Request not found.";
        }

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

        String email = r.getRequester().getEmail();
        if (email != null && !email.trim().isEmpty()) {
            try {
                EmailSender sender = new SmtpEmailSender();

                LocalDateTime confirmedStart = (r.getSlot() != null) ? r.getSlot().getStartDateTime() : null;
                LocalDate baseDate = (confirmedStart != null) ? confirmedStart.toLocalDate() : LocalDate.now();
                LocalDate reviewDate = baseDate.plusDays(10);

                String subject = "Review Appointment - Choose a Time";
                String body =
                        "You have a review appointment in 10 days to review the changes made to the website.\n"
                                + "Please choose ONE of the following available times on " + reviewDate + ":\n"
                                + "1) 13:00\n"
                                + "2) 14:00\n"
                                + "3) 15:00\n"
                                + "4) 16:00\n\n"
                                + "Please contact: " + COMPANY_REVIEW_PHONE + " and tell us your preferred time.\n";

                sender.send("noreply@qrbooking.local", email.trim(), subject, body);
            } catch (Exception ignored) {
            }
        }

        return "Final approval done. Appointment confirmed (" + label + ").";
    }

    /**
     * Rejects a request by big admin and releases the held slot.
     *
     * @param requestId     request id
     * @param adminUsername big admin username
     * @param reason        rejection reason
     * @return result message
     */
    public String rejectByBigAdmin(int requestId, String adminUsername, String reason) {
        BookingRequest r = findRequest(requestId);
        if (r == null) {
            return "Request not found.";
        }

        if (r.getStatus() != BookingRequestStatus.PENDING_BIG_ADMIN) {
            return "Request is not pending big admin.";
        }

        r.rejectByBigAdmin(adminUsername, reason);

        if (r.getSlot() != null) {
            r.getSlot().releaseHold();
        }

        auditEvents.add(new AuditEvent(
                AuditEvent.Type.MESSAGE_SENT,
                adminUsername != null ? adminUsername.trim() : "admin",
                (r.getSlot() != null && r.getSlot().getCategory() != null) ? r.getSlot().getCategory().getName() : "N/A",
                "Big admin rejected request #" + r.getId()
        ));

        return "Rejected by big admin. Slot is available again.";
    }

    /**
     * Counts confirmed appointments for a given user and category.
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
     * Counts pending booking requests for a given user and category.
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
     * Cancels a confirmed appointment for a user, enforcing a one-cancellation-per-category rule.
     *
     * @param appointment appointment to cancel
     * @return result message
     */
    public String cancelAppointment(Appointment appointment) {
        if (appointment == null) {
            return "Invalid booking.";
        }
        if (!appointments.contains(appointment)) {
            return "Booking not found.";
        }

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

    /**
     * Cancels a confirmed appointment by an admin.
     *
     * @param appointment   appointment to cancel
     * @param adminUsername admin username
     * @return result message
     */
    public String adminCancelAppointment(Appointment appointment, String adminUsername) {
        if (appointment == null) {
            return "Invalid booking.";
        }
        if (!appointments.contains(appointment)) {
            return "Booking not found.";
        }

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

    /**
     * Modifies a confirmed appointment by moving it to a new available slot.
     *
     * @param appointment          appointment to modify
     * @param newSlot              new slot to move to
     * @param newDurationInMinutes new duration in minutes
     * @param newParticipants      participants count
     * @param actorUsername        username performing the action
     * @return result message
     */
    public String modifyAppointment(Appointment appointment,
                                   TimeSlot newSlot,
                                   int newDurationInMinutes,
                                   int newParticipants,
                                   String actorUsername) {

        if (appointment == null || newSlot == null) {
            return "Invalid modification.";
        }
        if (!appointments.contains(appointment)) {
            return "Booking not found.";
        }

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

        if (newDurationInMinutes <= 0) {
            return "Invalid duration.";
        }
        if (newParticipants <= 0) {
            return "Invalid participants.";
        }

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

    /**
     * Modifies a confirmed appointment and enforces type-based validation rules.
     *
     * @param appointment          appointment to modify
     * @param newSlot              new slot to move to
     * @param newDurationInMinutes new duration in minutes
     * @param newParticipants      participants count
     * @param actorUsername        username performing the action
     * @param type                 appointment type
     * @param groupSize            group size when type is {@link AppointmentType#GROUP}
     * @return result message
     */
    public String modifyAppointment(Appointment appointment,
                                   TimeSlot newSlot,
                                   int newDurationInMinutes,
                                   int newParticipants,
                                   String actorUsername,
                                   AppointmentType type,
                                   Integer groupSize) {

        String rules = AppointmentTypeRules.validate(type, newDurationInMinutes, newParticipants, groupSize);
        if (!"OK".equals(rules)) {
            return rules;
        }

        String baseMsg = modifyAppointment(appointment, newSlot, newDurationInMinutes, newParticipants, actorUsername);
        if (!"Booking modified successfully.".equals(baseMsg)) {
            return baseMsg;
        }

        Appointment updated = findAppointmentByUserAndSlotStart(actorUsername, newSlot != null ? newSlot.getStartDateTime() : null);
        if (updated != null) {
            updated.setAppointmentType(type);
            if (type == AppointmentType.GROUP) {
                updated.setGroupSize(groupSize);
            }
        }

        return baseMsg;
    }

    /**
     * Finds an appointment by actor username and slot start time.
     *
     * @param username username
     * @param start    slot start time
     * @return appointment or {@code null}
     */
    private Appointment findAppointmentByUserAndSlotStart(String username, LocalDateTime start) {
        if (username == null || username.trim().isEmpty() || start == null) {
            return null;
        }

        for (Appointment a : appointments) {
            if (a == null) {
                continue;
            }
            if (a.getUser() == null || a.getUser().getUsername() == null) {
                continue;
            }
            if (!a.getUser().getUsername().equalsIgnoreCase(username.trim())) {
                continue;
            }
            if (a.getSlot() == null || a.getSlot().getStartDateTime() == null) {
                continue;
            }

            if (a.getSlot().getStartDateTime().equals(start)) {
                return a;
            }
        }

        return null;
    }
}