package it.unimol.newunimol.attendance_management.DTO;

public class TokenJWTDto {
    private String token;
    private String type = "Bearer";
    private String sub;
    private String username;
    private String role;
    private Long iat;
    private Long exp;
    
    public TokenJWTDto() {}
    
    public TokenJWTDto(String token, String userId, String username, String role, Long iat, Long exp) {
        this.token = token;
        this.sub = userId;
        this.username = username;
        this.role = role;
        this.iat = iat;
        this.exp = exp;
    }
    
    public String getToken() {
        return this.token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
    
    public String getType() {
        return this.type;
    }

    public String getUserId() {
        return this.sub;
    }
    
    public void setUserId(String userId) {
        this.sub = userId;
    }

    public String getUsername() {
        return this.username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return this.role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }

    public Long getIssuedAt() {
        return this.iat;
    }
    
    public void setIssuedAt(Long iat) {
        this.iat = iat;
    }
    
    public Long getExpiresIn() {
        return this.exp;
    }
    
    public void setExpiresIn(Long expiresIn) {
        this.exp = expiresIn;
    }
}