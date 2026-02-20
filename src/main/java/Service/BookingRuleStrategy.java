package Service;

import domain.Appointment;

public interface BookingRuleStrategy {

    
     
    boolean isValid(Appointment appointment);

    
    String getErrorMessage();
}
