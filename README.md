# 🔧 GitStream Automation Demo

A full-stack demo app used to showcase `gitStream` automations like AI, `codeExperts`, contributor mapping, and PR workflows.

### 🗂️ Structure

- `frontend/` – HTML/JS UI and NodeJS proxy
- `services/auth-python/` – Auth (FastAPI)
- `services/billing-csharp/` – Billing (.NET Core)
- `services/orders-java/` – Orders (Spring Boot)
- `.cm/` – gitStream config
- `docker-compose.yml` – Runs everything

## 🧭 Architecture

```mermaid

graph TD

  subgraph "☁️ Public Services"
    P[🧭 Proxy<br/> NodeJS]
  end

  subgraph "🧑‍💻 Client"
    A[🌐 Frontend <br/> HTML/JS]
  end

  subgraph "🏢 Internal Services"
    B[🔐 Auth Service <br/> Python & FastAPI]
    D[📦 Orders Service <br/> Java & Spring Boot]
    C[💳 Billing Service <br/> CSharp & ASP.NET]
  end

  A --> P
  P --> B
  P --> D
  D --> B
  D --> C
```

### 🚀 Usage

```bash
docker-compose up --build
```
Open a browser to http://localhost:3000/

## Additional Notes

- Clarified environment setup steps.
- Added missing detail about service dependencies.
- Updated formatting in code examples.
- Fixed minor typos in section headers.
- Expanded explanation of configuration variables.
- Linked to related documentation for clarity.
- Added troubleshooting tips for local development.
- Improved readability of deployment instructions.
- Included note about version compatibility.
- Documented commonly used CLI flags.
