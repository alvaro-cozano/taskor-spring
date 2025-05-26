package com.project.management.springboot.backend.project_management.stripe.controller;

import com.project.management.springboot.backend.project_management.stripe.service.StripeService;
import com.project.management.springboot.backend.project_management.stripe.model.AppUser;
import com.project.management.springboot.backend.project_management.stripe.service.AppUserService;
import com.project.management.springboot.backend.project_management.services.user.UserService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Invoice;
import com.stripe.model.StripeObject;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private final StripeService stripeService;
    private final AppUserService appUserService;
    private final UserService mainUserService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.price.id}")
    private String monthlyPriceId;

    public StripeController(StripeService stripeService, AppUserService appUserService, UserService mainUserService) {
        this.stripeService = stripeService;
        this.appUserService = appUserService;
        this.mainUserService = mainUserService;
    }

    public static class CreateCheckoutSessionRequest {
        private String priceId;

        public String getPriceId() {
            return priceId;
        }

        public void setPriceId(String priceId) {
            this.priceId = priceId;
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody(required = false) CreateCheckoutSessionRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String usernameFromToken = authentication.getName();

        com.project.management.springboot.backend.project_management.entities.models.User mainAppUser = mainUserService
                .findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario principal no encontrado con username: " + usernameFromToken));

        String userEmail = mainAppUser.getEmail();
        Long mainAppUserId = mainAppUser.getId();

        appUserService.findById(mainAppUserId)
                .orElseGet(() -> {
                    AppUser newAppUser = new AppUser(mainAppUserId, mainAppUser.getUsername(), userEmail);
                    return appUserService.saveUser(newAppUser);
                });

        try {
            Session session = stripeService.createCheckoutSession(userEmail, String.valueOf(mainAppUserId));
            return ResponseEntity.ok(Map.of("sessionId", session.getId(), "checkoutUrl", session.getUrl()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel-subscription")
    public ResponseEntity<Map<String, String>> cancelSubscription(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String usernameFromToken = authentication.getName();
        com.project.management.springboot.backend.project_management.entities.models.User mainAppUser = mainUserService
                .findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario principal no encontrado con username: " + usernameFromToken));

        Long mainAppUserId = mainAppUser.getId();

        AppUser appUser = appUserService.findById(mainAppUserId)
                .orElseThrow(() -> new RuntimeException("AppUser no encontrado para el ID: " + mainAppUserId));

        String subscriptionId = appUser.getStripeSubscriptionId();

        if (subscriptionId == null || subscriptionId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active subscription found for this user."));
        }

        try {
            Subscription canceledSubscription = stripeService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok(Map.of("message", "Subscription cancellation initiated.", "status",
                    canceledSubscription.getStatus()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reactivate-subscription")
    public ResponseEntity<Map<String, String>> reactivateSubscription(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String usernameFromToken = authentication.getName();
        com.project.management.springboot.backend.project_management.entities.models.User mainAppUser = mainUserService
                .findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario principal no encontrado con username: " + usernameFromToken));

        Long mainAppUserId = mainAppUser.getId();

        AppUser appUser = appUserService.findById(mainAppUserId)
                .orElseThrow(() -> new RuntimeException("AppUser no encontrado para el ID: " + mainAppUserId));

        String subscriptionId = appUser.getStripeSubscriptionId();

        if (subscriptionId == null || subscriptionId.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No subscription found for this user to reactivate."));
        }

        if (!appUser.isCancelAtPeriodEnd()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subscription is not scheduled for cancellation."));
        }

        try {
            Subscription reactivatedSubscription = stripeService.reactivateSubscription(subscriptionId);
            appUser.setCancelAtPeriodEnd(false);
            appUser.setSubscriptionStatus(reactivatedSubscription.getStatus());
            appUserService.saveUser(appUser);

            if ("active".equals(reactivatedSubscription.getStatus())) {
                mainUserService.addRoleToUser(mainAppUserId, "Premium");
            }

            return ResponseEntity.ok(Map.of("message", "Subscription reactivated successfully.", "status",
                    reactivatedSubscription.getStatus()));
        } catch (StripeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/subscription-status")
    public ResponseEntity<?> getUserSubscriptionStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "User not authenticated"));
        }

        String usernameFromToken = authentication.getName();
        com.project.management.springboot.backend.project_management.entities.models.User mainAppUser = mainUserService
                .findByUsername(usernameFromToken)
                .orElseThrow(() -> new RuntimeException(
                        "Usuario principal no encontrado con username: " + usernameFromToken));

        Long mainAppUserId = mainAppUser.getId();

        Optional<AppUser> appUserOptional = appUserService.findById(mainAppUserId);
        if (appUserOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptionStatus", appUser.getSubscriptionStatus());
            response.put("stripeSubscriptionId", appUser.getStripeSubscriptionId());
            response.put("cancelAtPeriodEnd", appUser.isCancelAtPeriodEnd());
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("subscriptionStatus", "not_found");
            response.put("cancelAtPeriodEnd", false);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing webhook.");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;

        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        }

        if (stripeObject == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook event data error.");
        }

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                stripeService.handleCheckoutSessionCompleted(session);
                break;
            case "customer.subscription.created":
                break;
            case "customer.subscription.updated":
                Subscription subscriptionUpdated = (Subscription) stripeObject;
                stripeService.handleCustomerSubscriptionUpdated(subscriptionUpdated);
                break;
            case "customer.subscription.deleted":
                Subscription subscriptionDeleted = (Subscription) stripeObject;
                stripeService.handleSubscriptionDeleted(subscriptionDeleted);
                break;
            case "invoice.created":
                Invoice invoiceCreated = (Invoice) stripeObject;
                stripeService.handleInvoiceCreated(invoiceCreated);
                break;
            case "invoice.payment_succeeded":
                Invoice invoicePaid = (Invoice) stripeObject;
                stripeService.handleInvoicePaymentSucceeded(invoicePaid);
                break;
            case "invoice.payment_failed":
                Invoice invoiceFailed = (Invoice) stripeObject;
                stripeService.handleInvoicePaymentFailed(invoiceFailed);
                break;
            default:
                break;
        }

        return ResponseEntity.ok("Webhook received");
    }
}