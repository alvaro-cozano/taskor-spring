package com.project.management.springboot.backend.project_management.stripe.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "app_users")
@Getter
@Setter
public class AppUser {

    @Id
    private Long id;

    private String username;
    private String email;
    private String stripeCustomerId;
    private String stripeSubscriptionId;
    private String subscriptionStatus;

    public AppUser() {
    }

    public AppUser(Long id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    public AppUser(String username, String email) {
        this.username = username;
        this.email = email;
    }
}