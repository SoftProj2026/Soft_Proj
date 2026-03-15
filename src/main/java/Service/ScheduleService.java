package Service;

import domain.TimeSlot;
import persistence.DataRepository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Provides scheduling operations (e.g., listing available slots).
 * @author remaa
 * @version 1.0
 */
public class ScheduleService {

    private DataRepository repo;
    private AuthService auth;

    /**
     * Creates a ScheduleService.
     *
     * @param repo data repository
     * @param auth authentication service used to check login status
     */
    public ScheduleService(DataRepository repo, AuthService auth) {
        this.repo = repo;
        this.auth = auth;
    }

    /**
     * Returns all available time slots for booking.
     * <p>
     * (US1.5) Logged-in user can view available time slots only.
     * Booked slots must not appear in the list.
     * </p>
     *
     * @return list of available slots
     * @throws IllegalStateException if the user is not logged in
     */

    public List<TimeSlot> getAvailableSlots() {

        if (!auth.isLoggedIn()) {
            throw new IllegalStateException("You must login first!");
        }

        return repo.getSlots()
                .stream()
                .filter(TimeSlot::isAvailable)
                .collect(Collectors.toList());
    }
}