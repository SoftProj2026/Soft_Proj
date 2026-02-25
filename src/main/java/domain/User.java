package domain;

import java.time.LocalDate;

public class User {
    private String username;
    private String password;

    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;

    public User(String firstName, String lastName, String username, String password, LocalDate dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.password = password;
        this.dateOfBirth = dateOfBirth;
    }

    public User(String username, String password) {
        this("", "", username, password, null);
    }
    

    public String getUsername() {
    	return username; 
    	}
    public String getPassword() {
    	return password; 
    	}

    public String getFirstName() { 
    	return firstName; 
    	}
    public String getLastName() { 
    	return lastName; 
    	}

    public LocalDate getDateOfBirth() { 
    	return dateOfBirth; 
    	}
}