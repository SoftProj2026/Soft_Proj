package persistence;

import domain.Appointment;
import domain.TimeSlot;
import domain.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

public class DataRepository {

    private static final String APPOINTMENTS_FILE = "appointments.csv";
    private static final String USERS_FILE = "users.csv";

    private List<User> users = new LinkedList<>();
    private List<Appointment> appointments = new LinkedList<>();

    public DataRepository() {
        loadUsers();
        loadAppointments();
    }

    public void addUser(User user) {
        users.add(user);
        saveUserToCSV(user);
    }

    public List<User> getUsers() {
        return users;
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appendAppointmentToCSV(appointment);
    }

    public List<Appointment> getAppointments() {
        return appointments;
    }

    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    users.add(new User(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load users: " + e.getMessage());
        }
    }

    private void loadAppointments() {
        File file = new File(APPOINTMENTS_FILE);
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; }
                String[] parts = line.split(",");
                if (parts.length >= 5) {
                    String username = parts[0].trim();
                    LocalDate date = LocalDate.parse(parts[1].trim());
                    LocalTime time = LocalTime.parse(parts[2].trim());
                    int duration = Integer.parseInt(parts[3].trim());
                    int participants = Integer.parseInt(parts[4].trim());

                    User user = findUserByUsername(username);
                    if (user == null) {
                        user = new User(username, "");
                    }

                    LocalDateTime dateTime = LocalDateTime.of(date, time);
                    TimeSlot slot = new TimeSlot(dateTime, duration);
                    Appointment appointment = new Appointment(user, slot, duration, participants);
                    appointment.confirm();
                    appointments.add(appointment);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load appointments: " + e.getMessage());
        }
    }

    private User findUserByUsername(String username) {
        for (User u : users) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    private synchronized void saveUserToCSV(User user) {
        boolean writeHeader = !new File(USERS_FILE).exists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE, true))) {
            if (writeHeader) {
                writer.println("Username,Password");
            }
            writer.println(user.getUsername() + "," + user.getPassword());
        } catch (IOException e) {
            System.err.println("Could not save user: " + e.getMessage());
        }
    }

    private synchronized void appendAppointmentToCSV(Appointment appointment) {
        boolean writeHeader = !new File(APPOINTMENTS_FILE).exists();
        try (PrintWriter writer = new PrintWriter(new FileWriter(APPOINTMENTS_FILE, true))) {
            if (writeHeader) {
                writer.println("User,Date,Time,Duration,Participants");
            }
            writer.println(
                    appointment.getUser().getUsername() + "," +
                    appointment.getSlot().getStartDateTime().toLocalDate() + "," +
                    appointment.getSlot().getStartDateTime().toLocalTime() + "," +
                    appointment.getDurationInMinutes() + "," +
                    appointment.getParticipants()
            );
        } catch (IOException e) {
            System.err.println("Could not save appointment: " + e.getMessage());
        }
    }
}