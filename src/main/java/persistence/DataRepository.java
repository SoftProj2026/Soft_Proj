package persistence;

import domain.User;
import domain.Appointment;

import java.util.LinkedList;
import java.util.List;

public class DataRepository {

    private List<User> users = new LinkedList<>();
    private List<Appointment> appointments = new LinkedList<>();


    public void addUser(User user) {
        users.add(user);
    }

    public List<User> getUsers() {
        return users;
    }


    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }
}