package persistence;

import domain.Appointment;
import domain.Category;
import domain.TimeSlot;
import domain.User;

import java.util.LinkedList;
import java.util.List;

public class DataRepository {

    private List<User> users = new LinkedList<>();
    private List<TimeSlot> slots = new LinkedList<>();
    private List<Appointment> appointments = new LinkedList<>();
    private List<Category> categories = new LinkedList<>();

    public void addUser(User user) { users.add(user); }
    public List<User> getUsers() { return users; }

    public List<TimeSlot> getSlots() { return slots; }
    public void addSlot(TimeSlot slot) { slots.add(slot); }

    public void addAppointment(Appointment appointment) { appointments.add(appointment); }
    public List<Appointment> getAppointments() { return appointments; }

    public void addCategory(Category c) { categories.add(c); }
    public List<Category> getCategories() { return categories; }

    public String cancelAppointment(Appointment appointment) {
        if (appointment == null) return "Invalid booking.";
        if (!appointments.contains(appointment)) return "Booking not found.";

        appointment.cancel();
        return "Booking cancelled successfully.";
    }
}