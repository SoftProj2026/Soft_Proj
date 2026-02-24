package persistence;

import domain.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataRepository {

    private static final String USERS_FILE = "users.txt";
    private static final String APPOINTMENTS_FILE = "appointments.txt";

    private List<User> users = new ArrayList<>();
    private List<Category> categories = new ArrayList<>();
    private List<TimeSlot> slots = new ArrayList<>();
    private List<Appointment> appointments = new ArrayList<>();

    public DataRepository() {
        loadUsersFromFile();
        loadAppointmentsFromFile();

        if (users.isEmpty()) {
            users.add(new User("admin", "1234"));
            saveUsersToFile();
        }
    }

    public List<User> getUsers() { return users; }

    public void addUser(User user) {
        users.add(user);
        saveUsersToFile();
    }

    public List<Category> getCategories() { return categories; }
    public void addCategory(Category c) { categories.add(c); }

    public List<TimeSlot> getSlots() { return slots; }
    public void addSlot(TimeSlot slot) { slots.add(slot); }

    public List<Appointment> getAppointments() { return appointments; }

    public void addAppointment(Appointment a) {
        appointments.add(a);
        saveAppointmentsToFile();
    }

    public void applyBookedSlotsFromAppointments() {
        for (Appointment a : appointments) {
            if (a.getStatus() == AppointmentStatus.CONFIRMED) {
                TimeSlot slot = a.getSlot();

                for (TimeSlot s : slots) {
                    if (sameSlot(s, slot)) {
                        s.book();
                    }
                }
            }
        }
    }

    private boolean sameSlot(TimeSlot a, TimeSlot b) {
        return a.getCategory().getName().equals(b.getCategory().getName())
                && a.getStartDateTime().equals(b.getStartDateTime())
                && a.getDuration() == b.getDuration();
    }

    public String cancelAppointment(domain.Appointment appointmentToCancel) {

        if (appointmentToCancel == null) {
            return "Please select a booking to cancel.";
        }

        if (appointmentToCancel.getStatus() != domain.AppointmentStatus.CONFIRMED) {
            return "Only confirmed bookings can be cancelled.";
        }

        java.time.LocalDateTime start = appointmentToCancel.getSlot().getStartDateTime();
        long hours = java.time.Duration.between(java.time.LocalDateTime.now(), start).toHours();

        if (hours < 12) {
            return "Reservations cannot be cancelled less than 12 hours before the scheduled time. Please choose another reservation.";
        }

        appointmentToCancel.cancel();

        if (this.getSlots() != null) {
            for (domain.TimeSlot s : this.getSlots()) {
                if (s.getCategory().getName().equalsIgnoreCase(appointmentToCancel.getSlot().getCategory().getName())
                        && s.getStartDateTime().equals(appointmentToCancel.getSlot().getStartDateTime())
                        && s.getDuration() == appointmentToCancel.getSlot().getDuration()) {
                    s.cancel();
                }
            }
        }

        saveAppointmentsToFile();

        return "Your reservation has been successfully cancelled";
    }

    private void loadUsersFromFile() {
        System.out.println("Loading users from: " + Paths.get(USERS_FILE).toAbsolutePath());

        users.clear();

        Path path = Paths.get(USERS_FILE);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("username")) continue;
                }

               
                String[] parts = line.split(",", -1);

                if (parts.length >= 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();

                    if (username.isEmpty() || password.isEmpty()) continue;

                    if (parts.length >= 6) {
                        String firstName = parts[2].trim();
                        String lastName = parts[3].trim();
                        String dobStr = parts[4].trim();
                        String residence = parts[5].trim();

                        LocalDate dob = null;
                        if (!dobStr.isEmpty()) {
                            try {
                                dob = LocalDate.parse(dobStr);
                            } catch (Exception ignored) {}
                        }

                        users.add(new User(username, password, firstName, lastName, dob, residence));
                    } else {
                        users.add(new User(username, password));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to load users: " + e.getMessage());
        }
    }

    private void saveUsersToFile() {
        System.out.println("Saving users to: " + Paths.get(USERS_FILE).toAbsolutePath());
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(USERS_FILE))) {
            // NEW header
            bw.write("Username,Password,FirstName,LastName,DOB,Residence");
            bw.newLine();

            for (User u : users) {
                String dob = (u.getDateOfBirth() == null) ? "" : u.getDateOfBirth().toString();

                bw.write(u.getUsername() + ","
                        + u.getPassword() + ","
                        + safe(u.getFirstName()) + ","
                        + safe(u.getLastName()) + ","
                        + dob + ","
                        + safe(u.getResidence()));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save users: " + e.getMessage());
        }
    }

    private String safe(String s) {
        if (s == null) return "";
        return s.replace(",", " ").trim();
    }

    private void loadAppointmentsFromFile() {
        appointments.clear();

        Path path = Paths.get(APPOINTMENTS_FILE);
        if (!Files.exists(path)) return;

        try (BufferedReader br = Files.newBufferedReader(path)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (first) {
                    first = false;
                    if (line.toLowerCase().startsWith("username")) continue;
                }

                String[] p = line.split(",", 6);
                if (p.length < 6) continue;

                String username = p[0].trim();
                String categoryName = p[1].trim();
                String startStr = p[2].trim();
                int duration = Integer.parseInt(p[3].trim());
                int participants = Integer.parseInt(p[4].trim());
                AppointmentStatus status = AppointmentStatus.valueOf(p[5].trim());

                User user = findOrCreateUser(username);
                Category cat = new Category(categoryName);
                LocalDateTime start = LocalDateTime.parse(startStr);

                TimeSlot slot = new TimeSlot(start, duration, cat);
                Appointment a = new Appointment(user, slot, duration, participants);

                if (status == AppointmentStatus.CONFIRMED) a.confirm();
                else if (status == AppointmentStatus.CANCELLED) a.cancel();

                appointments.add(a);
            }
        } catch (Exception e) {
            System.err.println("Failed to load appointments: " + e.getMessage());
        }
    }

    private void saveAppointmentsToFile() {
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(APPOINTMENTS_FILE))) {
            bw.write("Username,Category,StartDateTime,Duration,Participants,Status");
            bw.newLine();
            for (Appointment a : appointments) {
                bw.write(a.getUser().getUsername() + ","
                        + a.getSlot().getCategory().getName() + ","
                        + a.getSlot().getStartDateTime() + ","
                        + a.getDurationInMinutes() + ","
                        + a.getParticipants() + ","
                        + a.getStatus().name());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Failed to save appointments: " + e.getMessage());
        }
    }

    private User findOrCreateUser(String username) {
        for (User u : users) {
            if (u.getUsername().equalsIgnoreCase(username)) return u;
        }
        User created = new User(username, "");
        users.add(created);
        return created;
    }
}