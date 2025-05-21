package com.project.management.springboot.backend.project_management.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserProfileDTO {

    private String first_name;
    private String last_name;
    private String email;
    private String username;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password1;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password2;

    private String profileImageBase64;

    public UserProfileDTO() {
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword1() {
        return password1;
    }

    public void setPassword1(String password1) {
        this.password1 = password1;
    }

    public String getPassword2() {
        return password2;
    }

    public void setPassword2(String password2) {
        this.password2 = password2;
    }

    public String getProfileImageBase64() {
        return profileImageBase64;
    }
    
    public void setProfileImage(String profileImageBase64) {
        this.profileImageBase64 = profileImageBase64;
    }
}
