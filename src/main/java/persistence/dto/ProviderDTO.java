package persistence.dto;

/**
 * Optional: if you want to store providers separately (mirrors domain.Provider)
 * We keep it to reconstruct repo.getProviders().
 */
public class ProviderDTO {
    public String username;
    public String password;

    public String displayName;
    public String phone;
    public String email;
    public String address;
}