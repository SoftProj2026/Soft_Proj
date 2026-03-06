package persistence.dto;

import java.time.LocalDate;

/**
 * JSON-friendly representation of a User (and subclasses).
 * Used by RepoStorage for persistence.
 */
public class UserDTO {

    public String type;

    public String username;
    public String password;

    public String firstName;
    public String lastName;
    public LocalDate dateOfBirth;

    public String email;

    public String displayName;
    public String phone;
    public String address;

    
}