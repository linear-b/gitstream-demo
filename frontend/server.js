const express = require("express");
const fetch = require("node-fetch");
const app = express();
const swaggerUi = require("swagger-ui-express");
const swaggerJSDoc = require("swagger-jsdoc");

const swaggerDefinition = {
  openapi: "3.0.0",
  info: {
    title: "Frontend Gateway API",
    version: "1.0.0",
    description: "API Gateway for login and order forwarding",
  },
  servers: [{ url: "http://localhost:3000" }],
};

const swaggerSpec = swaggerJSDoc({
  swaggerDefinition,
  apis: ["./server.js"], // this file (can add others too)
});

const AUTH_URL   = process.env.AUTH_URL   || "http://auth-python:8000";
const ORDERS_URL = process.env.ORDERS_URL || "http://orders-java:8080";

app.use(express.static("public"));
app.get("/swagger.json", (req, res) => {
  res.setHeader("Content-Type", "application/json");
  res.send(swaggerSpec);
});
app.use("/api-docs", swaggerUi.serve, swaggerUi.setup(swaggerSpec));
app.use(express.json());

/**
 * @swagger
 * /api/login:
 *   post:
 *     summary: Login using auth backend
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               username:
 *                 type: string
 *               password:
 *                 type: string
 *     responses:
 *       200:
 *         description: Login successful
 */
app.post("/api/login", async (req, res) => {
  const response = await fetch(`${AUTH_URL}/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req.body),
  });

  const data = await response.json();
  res.json(data);
});

/**
 * @swagger
 * /api/orders:
 *   post:
 *     summary: Submit an order to the orders backend
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             example:
 *               itemId: "abc123"
 *               quantity: 2
 *     responses:
 *       200:
 *         description: Order submitted successfully
 *       401:
 *         description: Unauthorized - invalid or missing token
 *       500:
 *         description: Server error from orders service
 */
app.post("/api/orders", async (req, res) => {
  const token = req.headers["authorization"];
  const response = await fetch(`${ORDERS_URL}/orders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": token,
    },
    body: JSON.stringify(req.body),
  });

  const text = await response.text();
  res.status(response.status).send(text);
});

/**
 * @swagger
 * /api/orders/history:
 *   get:
 *     summary: Get order history for the authenticated user
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Order history retrieved successfully
 *       401:
 *         description: Unauthorized - invalid or missing token
 *       500:
 *         description: Server error from orders service
 */
app.get("/api/orders/history", async (req, res) => {
  const token = req.headers["authorization"];

  try {
    const response = await fetch(`${ORDERS_URL}/orders/history`, {
      method: "GET",
      headers: {
        "Authorization": token,
        "Content-Type": "application/json",
      },
    });

    const text = await response.text();
    res.status(response.status).send(text);
  } catch (err) {
    res.status(500).json({ error: "Failed to fetch order history" });
  }
});

app.listen(3000, () => console.log("Frontend on :3000"));
