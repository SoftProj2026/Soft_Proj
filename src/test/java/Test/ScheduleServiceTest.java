package Test;

import domain.Category;
import domain.TimeSlot;
import domain.User;
import org.junit.jupiter.api.Test;
import persistence.DataRepository;
import Service.AuthService;
import Service.ScheduleService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ScheduleServiceTest {

    @Test
    void getAvailableSlots_returns_only_available() {
        DataRepository repo = new DataRepository();

        User u = new User("A","B","user1","pass", LocalDate.of(2000,1,1), "a@b.com");
        repo.addUser(u);

        Category cat = new Category("Doctor Appointment");

        TimeSlot free = new TimeSlot(LocalDateTime.now().plusDays(1), 60, cat);
        TimeSlot booked = new TimeSlot(LocalDateTime.now().plusDays(1).plusHours(1), 60, cat);
        booked.book();

        repo.addSlot(free);
        repo.addSlot(booked);

        AuthService auth = new AuthService(repo);
        assertTrue(auth.login("user1","pass"));

        ScheduleService svc = new ScheduleService(repo, auth);

        List<TimeSlot> list = svc.getAvailableSlots(); 
        assertEquals(1, list.size());
        assertTrue(list.get(0).isAvailable());
        assertEquals(free.getStartDateTime(), list.get(0).getStartDateTime());
    }
}