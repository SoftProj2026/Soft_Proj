package Service;

import domain.TimeSlot;
import domain.Category;
import persistence.DataRepository;

import java.util.List;
import java.util.stream.Collectors;


public class ScheduleService {

    private DataRepository repo;
    private AuthService auth;

    public ScheduleService(DataRepository repo, AuthService auth) {
        this.repo = repo;
        this.auth = auth;
    }

       public List<TimeSlot> getAvailableSlotsByCategory(Category category) {

        if (!auth.isLoggedIn()) {
            throw new IllegalStateException("You must login first!");
        }

        return repo.getSlots()
                .stream()
                .filter(TimeSlot::isAvailable)
                .filter(slot -> slot.getCategory().equals(category))
                .collect(Collectors.toList());
    }
}