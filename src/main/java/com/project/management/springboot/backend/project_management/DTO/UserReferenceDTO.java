package com.project.management.springboot.backend.project_management.DTO;

import jakarta.validation.constraints.NotBlank;

public class UserReferenceDTO {

    @NotBlank
    private String email;

    private String profileImageBase64;

    public UserReferenceDTO() {
    }

    public UserReferenceDTO(String email, String profileImageBase64) {
        this.email = email;
        this.profileImageBase64 = profileImageBase64;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }

    public void setProfileImageBase64(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }
}
