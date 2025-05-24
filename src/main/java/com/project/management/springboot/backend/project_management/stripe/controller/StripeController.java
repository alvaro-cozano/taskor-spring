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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeController {

    private static final Logger logger = LoggerFactory.getLogger(StripeController.class);

    private final StripeService stripeService;
    private final AppUserService appUserService;
    private final UserService mainUserService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${stripe.price.id}")
    private String monthlyPriceId;

    public StripeController(StripeService stripeService, AppUserService appUserService, UserService mainUserService) { // Modifica
                                                                                                                       // el
                                                                                                                       // constructor
        this.stripeService = stripeService;
        this.appUserService = appUserService;
        this.mainUserService = mainUserService;
    }

    public static class CreateCheckoutSessionRequest {
        private String priceId;
        private String userEmail;

        public String getPriceId() {
            return priceId;
        }

        public void setPriceId(String priceId) {
            this.priceId = priceId;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }
    }

    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(
            @RequestBody CreateCheckoutSessionRequest request) {

        com.project.management.springboot.backend.project_management.entities.models.User mainAppUser = mainUserService
                .findByEmail(request.getUserEmail())
                .orElseThrow(() -> {
                    logger.error("Usuario principal no encontrado con email: {}", request.getUserEmail());
                    return new RuntimeException("Usuario principal no encontrado con email: " + request.getUserEmail());
                });

        Long mainAppUserId = mainAppUser.getId();

        appUserService.findById(mainAppUserId)
                .orElseGet(() -> {
                    logger.warn("Stripe AppUser con ID {} (email {}) no encontrado. Creando uno nuevo.", mainAppUserId,
                            request.getUserEmail());
                    AppUser newAppUser = new AppUser(mainAppUserId, mainAppUser.getUsername(), mainAppUser.getEmail());
                    return appUserService.saveUser(newAppUser);
                });

        String effectivePriceId = (request.getPriceId() != null && !request.getPriceId().isEmpty())
                ? request.getPriceId()
                : this.monthlyPriceId;

        try {
            Session session = stripeService.createCheckoutSession(effectivePriceId, mainAppUser.getEmail(),
                    mainAppUserId.toString());
            return ResponseEntity.ok(Map.of("sessionId", session.getId(), "checkoutUrl", session.getUrl()));
        } catch (StripeException e) {
            logger.error("StripeException while creating checkout session: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/cancel-subscription")
    public ResponseEntity<Map<String, String>> cancelSubscription(@RequestBody Map<String, String> payload) {
        String subscriptionId = payload.get("subscriptionId");
        if (subscriptionId == null || subscriptionId.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "subscriptionId is required."));
        }

        try {
            Subscription canceledSubscription = stripeService.cancelSubscription(subscriptionId);
            logger.info("Subscription {} cancellation initiated. Final status: {}", canceledSubscription.getId(),
                    canceledSubscription.getStatus());
            return ResponseEntity.ok(Map.of("message", "Subscription cancellation initiated.", "status",
                    canceledSubscription.getStatus()));
        } catch (StripeException e) {
            logger.error("StripeException while canceling subscription: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("Webhook signature verification failed.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Signature verification failed.");
        } catch (Exception e) {
            logger.error("Error parsing webhook event.", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error parsing webhook.");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);

        if (stripeObject == null) {
            logger.error("Webhook event data object is not present for event type: {}", event.getType());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook event data error.");
        }

        logger.info("Received Stripe event: id={}, type={}", event.getId(), event.getType());

        switch (event.getType()) {
            case "checkout.session.completed":
                Session session = (Session) stripeObject;
                logger.info("Checkout session completed for customer: {}, subscription: {}", session.getCustomer(),
                        session.getSubscription());
                stripeService.handleCheckoutSessionCompleted(session);
                break;
            case "customer.subscription.created":
                Subscription subscriptionCreated = (Subscription) stripeObject;
                logger.info("Subscription created: {} for customer: {}", subscriptionCreated.getId(),
                        subscriptionCreated.getCustomer());
                stripeService.handleCustomerSubscriptionCreated(subscriptionCreated);
                break;
            case "customer.subscription.updated":
                Subscription subscriptionUpdated = (Subscription) stripeObject;
                logger.info("Subscription updated: {} to status {}", subscriptionUpdated.getId(),
                        subscriptionUpdated.getStatus());
                stripeService.handleCustomerSubscriptionUpdated(subscriptionUpdated);
                break;
            case "customer.subscription.deleted":
                Subscription subscriptionDeleted = (Subscription) stripeObject;
                logger.info("Subscription deleted: {}", subscriptionDeleted.getId());
                stripeService.handleSubscriptionDeleted(subscriptionDeleted);
                break;
            case "invoice.created":
                Invoice invoiceCreated = (Invoice) stripeObject;
                logger.info("Invoice created: {} for subscription: {}, customer: {}", invoiceCreated.getId(),
                        invoiceCreated.getSubscription(), invoiceCreated.getCustomer());
                stripeService.handleInvoiceCreated(invoiceCreated);
                break;
            case "invoice.payment_succeeded":
                Invoice invoicePaid = (Invoice) stripeObject;
                logger.info("Invoice payment succeeded for subscription: {}", invoicePaid.getSubscription());
                stripeService.handleInvoicePaymentSucceeded(invoicePaid);
                break;
            case "invoice.payment_failed":
                Invoice invoiceFailed = (Invoice) stripeObject;
                logger.info("Invoice payment failed for subscription: {}", invoiceFailed.getSubscription());
                stripeService.handleInvoicePaymentFailed(invoiceFailed);
                break;
            default:
                logger.warn("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook received");
    }
}
