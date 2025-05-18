package com.example.orders.controller;

import org.springframework.beans.factory.annotation.Value;
import com.example.orders.model.OrderRequest;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${AUTH_SERVICE_URL:http://auth-python:8000}")
    private String authServiceUrl;

    @Value("${BILLING_SERVICE_URL:http://billing-csharp:80}")
    private String billingServiceUrl;

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest request,
                                             @RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String token = authHeader.substring(7);
        String username = validateToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
        }

        boolean billed = charge(username, request.productId, request.quantity);
        if (!billed) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body("Billing failed");
        }

        return ResponseEntity.ok("Order placed successfully by " + username);
    }

    protected String validateToken(String token) {
        String authUrl = authServiceUrl + "/auth/validate?token=" + token;
        try {
            ResponseEntity<String> authResponse = restTemplate.getForEntity(authUrl, String.class);
            if (!authResponse.getStatusCode().is2xxSuccessful()) return null;

            String body = authResponse.getBody();
            return body.contains("username") ? body.split(":")[1].replaceAll("[\"{} ]", "") : null;
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean charge(String username, String productId, int quantity) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Service-Secret", System.getenv("BILLING_SECRET"));

        JSONObject payload = new JSONObject();
        payload.put("username", username);
        payload.put("productId", productId);
        payload.put("quantity", quantity);

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
        try {
            ResponseEntity<String> billingResponse = restTemplate.postForEntity(
                billingServiceUrl + "/billing/charge", entity, String.class);
            return billingResponse.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}