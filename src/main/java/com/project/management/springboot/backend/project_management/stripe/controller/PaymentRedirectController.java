package com.project.management.springboot.backend.project_management.stripe.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PaymentRedirectController {

    @GetMapping("/payment/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam("session_id") String sessionId) {
        return ResponseEntity.ok("Pago procesado. Session ID: " + sessionId);
    }

    @GetMapping("/payment/cancel")
    public ResponseEntity<String> paymentCancel() {
        return ResponseEntity.ok("Pago cancelado.");
    }
}
