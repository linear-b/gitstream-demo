let token = null;

async function login() {
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const res = await fetch("/api/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password }),
  });

  const data = await res.json();
  token = data.token;
  document.getElementById("output").textContent = "Logged in!";
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
