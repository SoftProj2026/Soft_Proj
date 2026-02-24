package domain;

import java.time.LocalDate;

public class User {
    private String username;
    private String password;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String residence;

    public User(String username, String password) {
        this(username, password, "", "", null, "");
    }

    public User(String username,
                String password,
                String firstName,
                String lastName,
                LocalDate dateOfBirth,
                String residence) {
        this.username = username;
        this.password = password;
        this.firstName = firstName == null ? "" : firstName;
        this.lastName = lastName == null ? "" : lastName;
        this.dateOfBirth = dateOfBirth;
        this.residence = residence == null ? "" : residence;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getResidence() { return residence; }

    public void setPassword(String password) { this.password = password; }

    public boolean isAdult() {
        if (dateOfBirth == null) return false;
        LocalDate today = LocalDate.now();
        LocalDate eighteenth = dateOfBirth.plusYears(18);
        return !today.isBefore(eighteenth); 
    }
}