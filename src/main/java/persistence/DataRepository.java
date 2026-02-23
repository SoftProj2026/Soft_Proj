
package persistence;

import domain.*;
import java.util.ArrayList;
import java.util.List;

public class DataRepository {

    private List<User> users = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<TimeSlot> slots = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();

    public DataRepository() {
        users.add(new User("admin", "1234"));
    }

    public List<User> getUsers() {
        return users;
    }

    public void addUser(User user) {
        users.add(user);
    }

    public List<Category> getCategories() {
        return categories;
    }

    public void addCategory(Category c) {
        categories.add(c);
    }

    public List<TimeSlot> getSlots() {
        return slots;
    }

    public void addSlot(TimeSlot slot) {
        slots.add(slot);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    public void addAppointment(Appointment a) {
        appointments.add(a);
    }
}