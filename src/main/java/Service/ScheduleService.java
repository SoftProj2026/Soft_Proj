package Service;
import domain.TimeSlot;
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
