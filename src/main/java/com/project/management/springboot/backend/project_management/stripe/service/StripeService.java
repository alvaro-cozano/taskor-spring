package com.project.management.springboot.backend.project_management.stripe.service;

import com.project.management.springboot.backend.project_management.services.user.UserService;
import com.project.management.springboot.backend.project_management.stripe.model.AppUser;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.InvoiceLineItem;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionUpdateParams;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StripeService {

    @Value("${stripe.secret.key}")
    private String secretKey;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @Value("${app.front-url}")
    private String frontendBaseUrl;

    @Value("${stripe.price.id}")
    private String defaultPriceId;

    private final AppUserService appUserService;
    private final UserService mainUserService;

    public StripeService(AppUserService appUserService, UserService mainUserService) {
        this.appUserService = appUserService;
        this.mainUserService = mainUserService;
    }

    @PostConstruct
    public void init() {
        Stripe.apiKey = secretKey;
    }

    public Session createCheckoutSession(String userEmail, String userId) throws StripeException {
        String successUrl = frontendBaseUrl + "/subscription?status=success&session_id={CHECKOUT_SESSION_ID}";
        String cancelUrl = frontendBaseUrl + "/subscription?status=cancel";
        Customer customer = findOrCreateCustomer(userEmail, userId);

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl)
                .setCancelUrl(cancelUrl)
                .setCustomer(customer.getId())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(defaultPriceId)
                                .setQuantity(1L)
                                .build())
                .putMetadata("userId", userId)
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .putMetadata("userId", userId)
                                .build())
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
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(true)
                .build();
        return subscription.update(params);
    }

    public Subscription reactivateSubscription(String subscriptionId) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                .setCancelAtPeriodEnd(false)
                .build();
        return subscription.update(params);
    }

    public Subscription getSubscription(String subscriptionId) throws StripeException {
        return Subscription.retrieve(subscriptionId);
    }

    public void handleCheckoutSessionCompleted(Session session) {
        String customerId = session.getCustomer();
        String subscriptionId = session.getSubscription();
        String userIdString = session.getMetadata().get("userId");

        if (userIdString != null && customerId != null && subscriptionId != null) {
            appUserService.updateUserSubscription(userIdString, customerId, subscriptionId, "active");
            try {
                Long userId = Long.parseLong(userIdString);
                mainUserService.addRoleToUser(userId, "Premium");
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear userId en handleCheckoutSessionCompleted: " + userIdString + " - "
                        + e.getMessage());
            }
        }
    }

    public void handleSubscriptionDeleted(Subscription subscription) {
        String subscriptionId = subscription.getId();
        appUserService.updateUserSubscriptionStatusBySubId(subscriptionId, "canceled");

        Optional<AppUser> appUserOptional = appUserService.findByStripeSubscriptionId(subscriptionId);
        if (appUserOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            mainUserService.removeRoleFromUser(appUser.getId(), "Premium");
        }
    }

    public void handleCustomerSubscriptionUpdated(Subscription subscription) {
        String stripeSubscriptionId = subscription.getId();
        String stripeCustomerId = subscription.getCustomer();
        String status = subscription.getStatus();
        Boolean cancelAtPeriodEnd = subscription.getCancelAtPeriodEnd();
        String userIdString = subscription.getMetadata().get("userId");
        Long mainUserId = null;

        if (userIdString != null) {
            try {
                mainUserId = Long.parseLong(userIdString);
            } catch (NumberFormatException e) {
                System.err.println("Error al parsear userId en handleCustomerSubscriptionUpdated: " + userIdString
                        + " - " + e.getMessage());
            }
        } else if (stripeCustomerId != null) {
            Optional<AppUser> appUserOpt = appUserService.findByStripeCustomerId(stripeCustomerId);
            if (appUserOpt.isPresent()) {
                mainUserId = appUserOpt.get().getId();
            }
        }

        if (mainUserId != null) {
            final Long finalMainUserId = mainUserId;
            appUserService.findById(finalMainUserId).ifPresentOrElse(appUser -> {
                appUser.setSubscriptionStatus(status);
                if (appUser.getStripeSubscriptionId() == null && stripeSubscriptionId != null) {
                    appUser.setStripeSubscriptionId(stripeSubscriptionId);
                }
                appUser.setCancelAtPeriodEnd(cancelAtPeriodEnd != null && cancelAtPeriodEnd);
                appUserService.saveUser(appUser);
            }, () -> {
                System.err.println(
                        "AppUser no encontrado con ID: " + finalMainUserId + " en handleCustomerSubscriptionUpdated");
            });

            if ("active".equals(status) && (cancelAtPeriodEnd == null || !cancelAtPeriodEnd)) {
                mainUserService.addRoleToUser(mainUserId, "Premium");
            } else if ("canceled".equals(status) || "incomplete_expired".equals(status) || "unpaid".equals(status)) {
                mainUserService.removeRoleFromUser(mainUserId, "Premium");
            }
        }
    }

    public void handleInvoiceCreated(Invoice invoice) {
        if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
            InvoiceLineItem firstLineItem = invoice.getLines().getData().get(0);
            if (firstLineItem.getSubscription() != null) {
            }
        }
    }

    public void handleInvoicePaymentSucceeded(Invoice invoice) {
        String subscriptionId = null;
        if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
            InvoiceLineItem firstLineItem = invoice.getLines().getData().get(0);
            if (firstLineItem.getSubscription() != null) {
                subscriptionId = firstLineItem.getSubscription();
            }
        }

        if (subscriptionId != null) {
            appUserService.updateUserSubscriptionStatusBySubId(subscriptionId, "active");
        } else {
            String customerId = invoice.getCustomer();
            if (customerId != null) {
                appUserService.updateUserSubscriptionStatusByCustomerId(customerId, "active");
            }
        }
    }

    public void handleInvoicePaymentFailed(Invoice invoice) {
        String subscriptionId = null;
        if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
            InvoiceLineItem firstLineItem = invoice.getLines().getData().get(0);
            if (firstLineItem.getSubscription() != null) {
                subscriptionId = firstLineItem.getSubscription();
            }
        }

        if (subscriptionId != null) {
            appUserService.updateUserSubscriptionStatusBySubId(subscriptionId, "past_due");
        } else {
            String customerId = invoice.getCustomer();
            if (customerId != null) {
                appUserService.updateUserSubscriptionStatusByCustomerId(customerId, "past_due");
            }
        }
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}
