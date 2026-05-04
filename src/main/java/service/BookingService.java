package service;

import domain.Appointment;
import persistence.DataRepository;

import java.time.LocalDateTime;

/**
 * Provides booking operations for {@link Appointment} objects.
 *
 * <p>This service validates booking requests and prevents creating bookings in the past.</p>
 * @author Qussai
 *@version 1.0
 */
public class BookingService {

    private final DataRepository repo;

    /**
     * Creates a new {@code BookingService}.
     *
     * @param repo repository used to read/write booking data
     */
    public BookingService(DataRepository repo) {
        this.repo = repo;
    }

    /**
     * Attempts to book an appointment.
     *
     * <p>The appointment must have a non-null slot with a non-null start date/time.</p>
     * <p>Bookings for slots that start before {@link LocalDateTime#now()} are rejected.</p>
     *
     * @param appointment appointment to book
     * @return result containing success flag and message
     */
    public BookingResult book(Appointment appointment) {
        if (appointment == null || appointment.getSlot() == null || appointment.getSlot().getStartDateTime() == null) {
            return new BookingResult(false, "Invalid appointment.");
        }

        if (appointment.getSlot().getStartDateTime().isBefore(LocalDateTime.now())) {
            return new BookingResult(false, "You cannot book a past time slot.");
        }

        return new BookingResult(false, "book(Appointment) logic not shown here. Keep your existing implementation.");
    }
}