package Service;

import domain.Appointment;
import java.time.LocalDateTime;

public class MinimumNoticeRule implements BookingRuleStrategy {

    @Override
    public boolean isValid(Appointment appointment) {

        return appointment.getSlot()
                .getStartDateTime()
                .isAfter(LocalDateTime.now().plusHours(1));
    }

    @Override
    public String getErrorMessage() {
        return "Booking must be made at least 1 hour in advance.";
    }
}