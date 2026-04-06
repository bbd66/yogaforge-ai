package com.myfitpet.dto;

public class RegisterResponse {
    private Long id;
    private String email;
    private String username;
    private String avatarUrl;
    private String token;

    public RegisterResponse(Long id, String email, String username, String avatarUrl, String token) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.avatarUrl = avatarUrl;
        this.token = token;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getToken() { return token; }
}