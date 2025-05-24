package com.project.management.springboot.backend.project_management.stripe.service;

import com.project.management.springboot.backend.project_management.services.user.UserService;
import com.project.management.springboot.backend.project_management.stripe.model.AppUser;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);
    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    private final AppUserService appUserService;
    private final UserService mainUserService; // Debe ser UserService

    public StripeService(AppUserService appUserService, UserService mainUserService) {
        this.appUserService = appUserService;
        this.mainUserService = mainUserService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Session createCheckoutSession(String priceId, String userEmail, String userId) throws StripeException {
        String successUrl = appBaseUrl + "/payment/success?session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = appBaseUrl + "/payment/cancel";
        Customer customer = findOrCreateCustomer(userEmail, userId);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomer(customer.getId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(priceId)
                                .setQuantity(1L)
                                .build())
                .putMetadata("userId", userId)
                .build();
        return Session.create(params);
    }

    private Customer findOrCreateCustomer(String email, String userId) throws StripeException {
        Map<String, Object> customerSearchParams = new HashMap<>();
        customerSearchParams.put("email", email);
        com.stripe.model.CustomerSearchResult customerSearchResult = Customer
                .search(com.stripe.param.CustomerSearchParams.builder().setQuery("email:'" + email + "'").build());

        if (!customerSearchResult.getData().isEmpty()) {
            return customerSearchResult.getData().get(0);
        } else {
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setEmail(email)
                    .setName(email)
                    .putMetadata("app_user_id", userId)
                    .build();
            Customer newCustomer = Customer.create(customerParams);
            try {
                Long appUserId = Long.parseLong(userId);
                appUserService.findById(appUserId).ifPresent(user -> {
                    user.setStripeCustomerId(newCustomer.getId());
                    appUserService.saveUser(user);
                });
            } catch (NumberFormatException e) {
                System.err.println("Error: userId '" + userId + "' no es un Long válido en findOrCreateCustomer.");
            }
            return newCustomer;
        }
    }

    public Subscription cancelSubscription(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.cancel();
    }

    public Subscription getSubscription(String subscriptionId) throws StripeException {
        return Subscription.retrieve(subscriptionId);
    }

    public void handleCheckoutSessionCompleted(Session session) {
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();
        String userId = session.getMetadata().get("userId");

        if (userId != null && customerId != null && subscriptionId != null) {
            appUserService.updateUserSubscription(userId, customerId, subscriptionId, "active");
            System.out.println(
                    "Webhook: Checkout session completed for user: " + userId + ", subscriptionId: " + subscriptionId);
        } else {
            System.err.println("Webhook: Checkout session completed but missing critical data. UserID: " + userId
                    + ", CustomerID: " + customerId + ", SubscriptionID: " + subscriptionId);
        }
    }

    public void handleSubscriptionDeleted(Subscription subscription) {
        String subscriptionId = subscription.getId();
        appUserService.updateUserSubscriptionStatusBySubId(subscriptionId, "canceled");
        System.out.println("Webhook: Subscription deleted/canceled: " + subscriptionId);
    }

    public void handleCustomerSubscriptionCreated(Subscription subscription) {
        String subscriptionId = subscription.getId();
        String customerId = subscription.getCustomer();
        String status = subscription.getStatus();

        System.out.println("Webhook: Customer subscription created. ID: " + subscriptionId + ", Customer: " + customerId
                + ", Status: " + status);
    }

    public void handleCustomerSubscriptionUpdated(Subscription subscription) {
        String stripeSubscriptionId = subscription.getId();
        String stripeCustomerId = subscription.getCustomer();
        String status = subscription.getStatus();
        String userIdString = subscription.getMetadata().get("userId");
        Long mainUserId = null;

        if (userIdString != null) {
            try {
                mainUserId = Long.parseLong(userIdString);
            } catch (NumberFormatException e) {
                logger.error("Error al parsear userIdString '{}' de metadata en handleCustomerSubscriptionUpdated.",
                        userIdString, e);
            }
        } else if (stripeCustomerId != null) {
            Optional<AppUser> appUserOpt = appUserService.findByStripeCustomerId(stripeCustomerId);
            if (appUserOpt.isPresent()) {
                mainUserId = appUserOpt.get().getId();
            } else {
                logger.warn("No se encontró AppUser con stripeCustomerId {} para handleCustomerSubscriptionUpdated.",
                        stripeCustomerId);
            }
        }

        if (mainUserId != null) {
            final Long finalMainUserId = mainUserId;
            appUserService.findById(mainUserId).ifPresent(appUser -> {
                appUser.setSubscriptionStatus(status);
                if (appUser.getStripeSubscriptionId() == null && stripeSubscriptionId != null) {
                    appUser.setStripeSubscriptionId(stripeSubscriptionId);
                }
                appUserService.saveUser(appUser);
                logger.info("AppUser {} actualizado a status '{}' por handleCustomerSubscriptionUpdated.",
                        finalMainUserId, status);
            });

            if ("active".equals(status)) {
                mainUserService.addRoleToUser(mainUserId, "Premium");
                logger.info("Rol Premium añadido al usuario {} por handleCustomerSubscriptionUpdated (status active).",
                        mainUserId);
            } else if ("canceled".equals(status) || "incomplete_expired".equals(status) || "past_due".equals(status)
                    || "unpaid".equals(status) || "trialing".equals(status) || "incomplete".equals(status)) {
                logger.info(
                        "Estado de suscripción para usuario {} actualizado a '{}'. La gestión del rol Premium se hará en 'active' o 'deleted'.",
                        mainUserId, status);
            }
        } else {
            logger.error(
                    "No se pudo determinar el mainUserId para handleCustomerSubscriptionUpdated. SubscriptionID: {}",
                    stripeSubscriptionId);
        }
    }

    public void handleInvoiceCreated(com.stripe.model.Invoice invoice) {
        String invoiceId = invoice.getId();
        String subscriptionId = invoice.getSubscription();
        String customerId = invoice.getCustomer();
        Long amountDue = invoice.getAmountDue();

        System.out.println("Webhook: Invoice created. ID: " + invoiceId + ", Subscription: " + subscriptionId
                + ", Customer: " + customerId + ", Amount Due: " + amountDue);
    }

    public void handleInvoicePaymentSucceeded(com.stripe.model.Invoice invoice) {
        String subscriptionId = invoice.getSubscription();
        if (subscriptionId != null) {
            appUserService.updateUserSubscriptionStatusBySubId(subscriptionId, "active");
            System.out.println("Webhook: Invoice payment succeeded for subscription: " + subscriptionId);
        } else {
            String customerId = invoice.getCustomer();
            if (customerId != null) {
                appUserService.updateUserSubscriptionStatusByCustomerId(customerId, "active");
                System.out.println("Webhook: Invoice payment succeeded for customer: " + customerId
                        + " (no direct subscription ID in invoice event)");
            } else {
                System.err.println("Webhook: Invoice payment succeeded but no subscription or customer ID found.");
            }
        }
    }

    public void handleInvoicePaymentFailed(com.stripe.model.Invoice invoice) {
        String subscriptionId = invoice.getSubscription();
        if (subscriptionId != null) {
            appUserService.updateUserSubscriptionStatusBySubId(subscriptionId, "past_due");
            System.out.println("Webhook: Invoice payment failed for subscription: " + subscriptionId);
        } else {
            String customerId = invoice.getCustomer();
            if (customerId != null) {
                appUserService.updateUserSubscriptionStatusByCustomerId(customerId, "past_due");
                System.out.println("Webhook: Invoice payment failed for customer: " + customerId
                        + " (no direct subscription ID in invoice event)");
            } else {
                System.err.println("Webhook: Invoice payment failed but no subscription or customer ID found.");
            }
        }
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}
