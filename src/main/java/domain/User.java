package domain;

import java.time.LocalDate;

public class User {

    private final String username;
    private final String password;

    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;

    private final String email; 

    public User(String firstName,
                String lastName,
                String username,
                String password,
                LocalDate dateOfBirth,
                String email) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
        this.email = email != null ? email.trim() : "";
    }

    public User(String firstName,
                String lastName,
                String username,
                String password,
                LocalDate dateOfBirth) {
        this(firstName, lastName, username, password, dateOfBirth, "");
    }

    public User(String username, String password) {
        this("", "", username, password, null, "");
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }

    public String getEmail() { return email; }
}