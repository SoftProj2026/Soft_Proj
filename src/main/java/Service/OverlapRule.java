package Service;

import domain.Appointment;
import domain.TimeSlot;
import persistence.DataRepository;


  //US3.1 - Prevent overlapping bookings
 
public class OverlapRule implements BookingRuleStrategy {

    private DataRepository repo;

    public OverlapRule(DataRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean isValid(Appointment newAppointment) {

        for (Appointment existing : repo.getAppointments()) {

            TimeSlot newSlot = newAppointment.getSlot();
            TimeSlot existingSlot = existing.getSlot();

            boolean overlap =
                    newSlot.getStartDateTime().isBefore(existingSlot.getEndDateTime()) &&
                    newSlot.getEndDateTime().isAfter(existingSlot.getStartDateTime());

            if (overlap) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String getErrorMessage() {
        return "Time slot overlaps with an existing booking.";
    }
}