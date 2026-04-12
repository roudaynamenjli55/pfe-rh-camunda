package org.example.rhcamunda.dto.auth;

public class LoginResponse {
    private String message;
    private String status;
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;

    // Constructeur
    public LoginResponse(String message, String status, String accessToken,
                         String refreshToken, Integer expiresIn) {
        this.message = message;
        this.status = status;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    // Getters
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Integer getExpiresIn() { return expiresIn; }
}