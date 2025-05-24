package com.project.management.springboot.backend.project_management.stripe.repository;

import com.project.management.springboot.backend.project_management.stripe.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByStripeCustomerId(String stripeCustomerId);

    Optional<AppUser> findByStripeSubscriptionId(String stripeSubscriptionId);
}
