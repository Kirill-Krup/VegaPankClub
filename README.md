# 🖥️ VegaPankClub — Computer Club Management System

A full-stack microservices application for managing a computer club with a built-in bar/cafe. Features PC/room booking, billing, payments, inventory, and user management — with a clean web frontend.

---

## 🏗️ Architecture Overview

```
                        ┌─────────────────┐
                        │    Frontend      │
                        │  HTML + CSS + JS │
                        └────────┬────────┘
                                 │
                        ┌────────▼────────┐
                        │   API Gateway   │
                        └────────┬────────┘
                                 │
        ┌────────────────────────┼────────────────────────┐
        │                        │                         │
┌───────▼───────┐    ┌───────────▼──────────┐   ┌────────▼────────┐
│  UserService  │    │    AdminService       │   │ ProductService  │
│ + AuthService │    │  (reactive aggr.)     │   │  (bar/cafe)     │
└───────────────┘    └──────────────────────┘   └─────────────────┘
        │                        │                         │
┌───────▼────────────────────────▼─────────────────────────▼──────┐
│                     Core Domain Services                          │
│         BillingService       │       InventoryService             │
│   (tariffs, sessions)        │   (PCs, rooms — Redis cached)     │
└───────────────────────────────────────────┬──────────────────────┘
                                            │ Kafka Events
                                   ┌────────▼────────┐
                                   │  PaymentService  │
                                   └─────────────────┘
```

---

## ⚙️ Tech Stack

| Layer | Technologies |
|---|---|
| **Backend** | Java 17, Spring Boot, Spring Cloud, Spring Security |
| **Reactive** | Spring WebFlux (admin aggregation) |
| **Messaging** | Apache Kafka |
| **Cache** | Redis (PC/room availability, session data) |
| **Database** | PostgreSQL, Hibernate / Spring Data JPA |
| **External API** | Yandex Object Storage (user profile photos) |
| **Frontend** | HTML, CSS, JavaScript |
| **DevOps** | Docker, Docker Compose, Kubernetes (K8s), GitHub Actions CI/CD (per-service) |

---

## 📦 Microservices

| Service | Description |
|---|---|
| `api-gateway` | Request routing and load balancing |
| `user-service` | User profile + authentication (JWT) — planned split into separate services |
| `billing-service` | Tariff management, session orders, billing records |
| `inventory-service` | PC and room data with Redis caching (availability updated in real-time) |
| `payment-service` | Payment processing and transaction handling |
| `product-service` | Bar/cafe menu — users can order food and drinks during sessions |
| `admin-service` | Reactive aggregation of complex admin dashboard data |
| `common-lib` | Shared DTOs, Redis config, and common configurations |

---

## 🔄 Kafka Event Flow

Payments are fully event-driven across three service chains:

```
BillingService  ──[SESSION_ORDER]──►  PaymentService  ──[CHECK_BALANCE]──►  UserService
                                                                                   │
ProductService  ──[PRODUCT_ORDER]──►  PaymentService                        Validates wallet
                                                                                   │
                                      ◄────────────────[PAYMENT_RESULT]───────────┘

Each service updates transaction status:
  PENDING → PAID / NOT_PAID / ERROR
```

---

## ⚡ Redis Caching Strategy

```
Cached data:       PCs, rooms, tariffs  (low write frequency)
Cache invalidation: On availability change (PC occupied/freed)
Config:            Shared via common-lib, extended per service
```

---

## 🌍 Environment Profiles

| Profile | Usage |
|---|---|
| `localhost` | Local development without Docker networking |
| `docker` | Containerized environment with Docker Compose |

---

## ☸️ Kubernetes

Kubernetes manifests are located in the `/k8s` directory, covering deployments, services, and config maps for each microservice.

---

## 🚀 Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17+

### Run with Docker Compose

```bash
git clone https://github.com/Kirill-Krup/VegaPankClub.git
cd VegaPankClub
docker-compose up --build
```

Frontend will be available at `http://localhost:80`  
API Gateway at `http://localhost:8080`

### Run with Kubernetes

```bash
kubectl apply -f k8s/
```
