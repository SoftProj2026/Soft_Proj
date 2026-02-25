package Service;

import domain.TimeSlot;
import persistence.DataRepository;

import java.util.List;
import java.util.stream.Collectors;


 //Sprint 1 - Viewing Available Time Slots
 
public class ScheduleService {

    private DataRepository repo;
    private AuthService auth;

    public ScheduleService(DataRepository repo, AuthService auth) {
        this.repo = repo;
        this.auth = auth;
    }

    
     //US1.5 - Logged-in user can view available time slots only
     //Booked slots must not appear in the list
     
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