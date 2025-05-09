let token = null;

async function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const res = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  if (!res.ok) return document.getElementById("output").textContent = "Login failed.";
  token = (await res.json()).token;
  if (!token) return document.getElementById("output").textContent = "No token received.";
  document.getElementById("output").textContent = "Logged in!";
  document.getElementById("login-section").style.display = "none";
}

async function placeOrder() {
  const productId = document.getElementById("product").value;
  const quantity = parseInt(document.getElementById("quantity").value);

  const res = await fetch("/api/orders", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": "Bearer " + token
    },
    body: JSON.stringify({ productId, quantity }),
  });

  const text = await res.text();
  document.getElementById("output").textContent = text;
}

async function getOrderHistory() {
  const res = await fetch("/api/orders/history", {
    method: "GET",
    headers: {
      "Authorization": "Bearer " + token
    }
  });

  if (!res.ok) {
    document.getElementById("output").textContent = "Failed to fetch order history.";
    return;
  }

  const orders = await res.json();
  let output = `Order History for ${orders.username || document.getElementById("username").value}:\n`;
  const orderList = orders.orders || orders;
  if (orderList.length === 0) {
    output += "No orders found.\n";
  } else {
    orderList.forEach(order => {
      output += `Order #${order.orderId}: ${order.productId} x ${order.quantity}\n`;
    });
  }
  document.getElementById("output").textContent = output;
}
