package persistence;
import domain.User;
import domain.TimeSlot;
import java.util.LinkedList;
import java.util.List;
import domain.Appointment;


public class DataRepository {
    private List<User> users = new LinkedList<>();
    private List<TimeSlot> slots = new LinkedList<>();
    private List<Appointment> appointments = new LinkedList<>();

    
    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
    	return users; 
    	}
    public List<TimeSlot> getSlots() {
    	return slots; 
    	}
    public void addSlot(TimeSlot slot) { 
    	slots.add(slot);
    	}


    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }
}
