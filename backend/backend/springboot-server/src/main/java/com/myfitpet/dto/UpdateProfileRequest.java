package com.myfitpet.dto;

import jakarta.validation.constraints.Size;

public class UpdateProfileRequest {
    @Size(min = 2, max = 64)
    private String username; // 可选

    @Size(max = 512)
    private String avatarUrl; // 可选

    @Size(max = 128)
    private String email; // 可选

    // @Size(max = 32)
    // private String phone; // 可选

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    // public String getPhone() { return phone; }
    // public void setPhone(String phone) { this.phone = phone; }
}