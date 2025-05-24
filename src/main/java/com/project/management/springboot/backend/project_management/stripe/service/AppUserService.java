package com.project.management.springboot.backend.project_management.stripe.service;

import com.project.management.springboot.backend.project_management.stripe.model.AppUser;
import com.project.management.springboot.backend.project_management.stripe.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;

    public AppUserService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public Optional<AppUser> findByEmail(String email) {
        return appUserRepository.findByEmail(email);
    }

    public Optional<AppUser> findById(Long id) {
        return appUserRepository.findById(id);
    }

    public Optional<AppUser> findByStripeCustomerId(String stripeCustomerId) {
        return appUserRepository.findByStripeCustomerId(stripeCustomerId);
    }

    public Optional<AppUser> findByStripeSubscriptionId(String stripeSubscriptionId) {
        return appUserRepository.findByStripeSubscriptionId(stripeSubscriptionId);
    }

    @Transactional
    public AppUser saveUser(AppUser user) {
        return appUserRepository.save(user);
    }

    @Transactional
    public void updateUserSubscription(String userId, String stripeCustomerId, String stripeSubscriptionId,
            String status) {
        Long appUserId = Long.parseLong(userId);
        appUserRepository.findById(appUserId).ifPresent(user -> {
            user.setStripeCustomerId(stripeCustomerId);
            user.setStripeSubscriptionId(stripeSubscriptionId);
            user.setSubscriptionStatus(status);
            appUserRepository.save(user);
            System.out.println("User " + userId + " subscription updated. Stripe Customer ID: " + stripeCustomerId
                    + ", Subscription ID: " + stripeSubscriptionId + ", Status: " + status);
        });
    }

    @Transactional
    public void updateUserSubscriptionStatusBySubId(String stripeSubscriptionId, String status) {
        appUserRepository.findByStripeSubscriptionId(stripeSubscriptionId).ifPresent(user -> {
            user.setSubscriptionStatus(status);
            appUserRepository.save(user);
            System.out.println("Subscription " + stripeSubscriptionId + " status updated to " + status + " for user "
                    + user.getEmail());
        });
    }

    @Transactional
    public void updateUserSubscriptionStatusByCustomerId(String stripeCustomerId, String status) {
        appUserRepository.findByStripeCustomerId(stripeCustomerId).ifPresent(user -> {
            user.setSubscriptionStatus(status);
            appUserRepository.save(user);
            System.out.println(
                    "Customer " + stripeCustomerId + " status updated to " + status + " for user " + user.getEmail());
        });
    }
}
