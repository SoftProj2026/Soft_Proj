package persistence.dto;

import java.time.LocalDateTime;

public class ContactRequestDTO {
    public int id;
    public String fromUsername;
    public String toProviderUsername;
    public String message;
    public LocalDateTime createdAt;
    public boolean read;
}