package com.example.orders;

import com.example.orders.controller.OrderController;
import com.example.orders.model.OrderRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerTests {

    static class MockedOrderController extends OrderController {
        private final boolean validToken;
        private final boolean billingSuccess;
        private final String mockUser;

        public MockedOrderController(boolean validToken, boolean billingSuccess, String mockUser) {
            this.validToken = validToken;
            this.billingSuccess = billingSuccess;
            this.mockUser = mockUser;
            org.h2.jdbcx.JdbcDataSource dataSource = new org.h2.jdbcx.JdbcDataSource();
            dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
            dataSource.setUser("sa");
            dataSource.setPassword("");
            this.dataSource = dataSource;
            super.init();
        }

        @Override
        protected String validateToken(String token) {
            return validToken ? mockUser : null;
        }

        @Override
        protected boolean charge(String username, String productId, int quantity) {
            return billingSuccess;
        }
    }

    @Test
    void testOrderSuccess() {
        MockedOrderController controller = new MockedOrderController(true, true, "alice");

        OrderRequest req = new OrderRequest();
        req.productId = "widget-123";
        req.quantity = 2;

        ResponseEntity<String> response = controller.placeOrder(req, "Bearer faketoken");

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("alice"));
    }

    @Test
    void testInvalidToken() {
        MockedOrderController controller = new MockedOrderController(false, true, "alice");

        OrderRequest req = new OrderRequest();
        req.productId = "widget-123";
        req.quantity = 2;

        ResponseEntity<String> response = controller.placeOrder(req, "Bearer faketoken");

        assertEquals(401, response.getStatusCodeValue());
    }

    @Test
    void testBillingFailure() {
        MockedOrderController controller = new MockedOrderController(true, false, "alice");

        OrderRequest req = new OrderRequest();
        req.productId = "widget-123";
        req.quantity = 2;

        ResponseEntity<String> response = controller.placeOrder(req, "Bearer faketoken");

        assertEquals(402, response.getStatusCodeValue());
    }

    @Test
    void testMissingAuthHeader() {
        MockedOrderController controller = new MockedOrderController(true, true, "alice");

        OrderRequest req = new OrderRequest();
        req.productId = "widget-123";
        req.quantity = 2;

        ResponseEntity<String> response = controller.placeOrder(req, null);

        assertEquals(401, response.getStatusCodeValue());
    }
}