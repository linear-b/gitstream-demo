package com.example.orders.controller;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import com.example.orders.model.OrderRequest;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.annotation.PostConstruct;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Value("${AUTH_SERVICE_URL:http://auth-python:8000}")
    private String authServiceUrl;

    @Value("${BILLING_SERVICE_URL:http://billing-csharp:80}")
    private String billingServiceUrl;

    protected final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    protected javax.sql.DataSource dataSource;

    // Initialize table after Spring dependency injection
    @PostConstruct
    public void init() {
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS orders (" +
                    "orderId VARCHAR(255), " +
                    "username VARCHAR(255), " +
                    "productId VARCHAR(255), " +
                    "quantity INT, " +
                    "timestamp VARCHAR(255))");
        } catch (Exception e) {
            // Log error, but don't prevent app startup
            e.printStackTrace();
        }
    }

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

        String orderId = java.util.UUID.randomUUID().toString();
        String timestamp = Instant.now().toString();
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO orders (orderId, username, productId, quantity, timestamp) VALUES (?, ?, ?, ?, ?)")) {
            pstmt.setString(1, orderId);
            pstmt.setString(2, username);
            pstmt.setString(3, request.productId);
            pstmt.setInt(4, request.quantity);
            pstmt.setString(5, timestamp);
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Order storage failed");
        }

        return ResponseEntity.ok("Order placed successfully by " + username);
    }

    @GetMapping("/history")
    public ResponseEntity<String> getOrderHistory(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing token");
        }

        String token = authHeader.substring(7);
        String username = validateToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid session");
        }

        org.json.JSONArray orders = new org.json.JSONArray();
        try (java.sql.Connection conn = dataSource.getConnection();
             java.sql.PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM orders WHERE username = ?")) {
            pstmt.setString(1, username);
            try (java.sql.ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    org.json.JSONObject order = new org.json.JSONObject();
                    order.put("orderId", rs.getString("orderId"));
                    order.put("productId", rs.getString("productId"));
                    order.put("quantity", rs.getInt("quantity"));
                    order.put("timestamp", rs.getString("timestamp"));
                    orders.put(order);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to retrieve order history");
        }

        org.json.JSONObject response = new org.json.JSONObject();
        response.put("username", username);
        response.put("orders", orders);

        return ResponseEntity.ok(response.toString());
    }

    protected String validateToken(String token) {
        String authUrl = authServiceUrl + "/auth/validate?token=" + token;
        try {
            ResponseEntity<String> authResponse = restTemplate.getForEntity(authUrl, String.class);
            if (!authResponse.getStatusCode().is2xxSuccessful()) return null;

            String body = authResponse.getBody();
            return body.contains("username") ? body.split(":")[1].replaceAll("[\"{} ]", "") : null;
        } catch (Exception e) {
            e.printStackTrace();
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
        payload.put("date", Instant.now().toString());

        HttpEntity<String> entity = new HttpEntity<>(payload.toString(), headers);
        try {
            ResponseEntity<String> billingResponse = restTemplate.postForEntity(
                billingServiceUrl + "/billing/charge", entity, String.class);
            return billingResponse.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}