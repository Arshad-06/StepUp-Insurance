# 🛡️ StepUp Insurance: Enterprise Micro-Lending & Flex-Credit Platform

An enterprise-grade, full-stack insurance ecosystem engineered to deliver seamless policy management, automated compliance, and real-time analytical dashboards. Built with a high-throughput Java/Spring Boot backend, a responsive Angular frontend, and automated CI/CD pipelines, this platform prioritizes low-latency execution, defensive form design, and bulletproof security.

---

## 🏗️ System Architecture & Core Stack

    ┌────────────────────────────────────────────────────────┐
    │                   Angular Frontend                     │
    │      (TypeScript, Tailwind CSS, HTML5, Chart.js)       │
    └───────────────────────────┬────────────────────────────┘
                                │ HTTPS (Port 443)
                                ▼
    ┌────────────────────────────────────────────────────────┐
    │            Spring Boot REST API Gateway                │
    │  (Java 17, Spring Security JWT, Rate-Limiting Filter)  │
    └───────────────────────────┬────────────────────────────┘
                                │
        ┌───────────────────────┼───────────────────────┐
        ▼                       ▼                       ▼
    ┌──────────────────────┐┌──────────────────────┐┌──────────────────────┐
    │  Caffeine L2 Cache   ││   Task Executor      ││  Hibernate JPA ORM   │
    │ (In-Memory Platform) ││ (@Async Multi-Thread)││ (Connection Pooling) │
    └──────────────────────┘└───────────┬──────────┘└───────────┬──────────┘
    │                       │
    ▼                       ▼
    ┌──────────────────────┐┌──────────────────────┐
    │    Brevo HTTP API    ││   MySQL Database     │
    │ (Port 443 Web Relay) ││ (Persistent Storage) │
    └──────────────────────┘└──────────────────────┘

### 💻 Frontend Architecture
*   **Core Framework:** Angular (TypeScript, HTML5) structured with lazy-loaded domain modules (Customer, Agent, Auth).
*   **Styling Engine:** Tailwind CSS providing a utility-first, fully scannable fluid layout.
*   **State & Theme Management:** Custom reactive state drivers supporting seamless **Global Dark Mode / Light Mode** switching across all viewport modules.

### ⚙️ Backend Core Infrastructure
*   **Runtime Environment:** Java 17 / Spring Boot 3.x configured with customized Hibernate JPA mapping layer.
*   **Database Engine:** MySQL Database with optimized relational constraints and indexes on high-frequency search vectors.
*   **Cloud Production Deployments:** 
    *   **Frontend:** Vercel (Edge-network static hosting with automated production builds).
    *   **Backend:** Render (Isolated container hosting environment).

---

## 🚀 Key Technical Highlights & Engineering Design

### 🔒 Zero-Trust Spring Security & JWT Architecture
*   **Stateless Authentication:** Fully custom Spring Security filter chain processing asymmetric JSON Web Tokens (JWT) on every incoming HTTP resource string.
*   **Role-Based Access Control (RBAC):** Explicit boundary separation enforcing strict `@PreAuthorize` rules between `ROLE_CUSTOMER` and `ROLE_AGENT` entry points.

### ⚡ Non-Blocking Async Architecture & Port-Bypassing Email Delivery
*   **Firewall Evasion via Web API:** Traditional cloud environments frequently drop standard SMTP email ports (`25`, `465`, `587`) via network firewalls. This platform circumvents network blocking by encapsulating email payloads over standard **HTTPS Web REST API requests via Spring RestClient** directly to Brevo’s v3 infrastructure.
*   **Multi-Threaded `@Async` Execution:** Email transmissions (OTPs and Payment Reminders) are pushed to an independent background thread pool via `@Async`. This guarantees that long-running third-party HTTP handshakes never freeze the primary user request thread pool.

### 🚀 Low-Latency Caching (Caffeine Cache Engine)
*   **In-Memory Optimization:** Integrated high-performance **Caffeine Cache** provider layer directly into the Spring Cache context.
*   **Eviction Strategy:** High-frequency, static metadata streams (like Policy Types, constants, and basic analytical rules) are cached in-memory, dropping database traversal steps and reducing resource request latency by up to 90%.

### 🧪 Defensive UI Form Validation: User-First Strategy
*   **Proactive Error Prevention:** Instead of punishing users with server-side rejections after submission, all intake blocks utilize proactive **Reactive Custom Validators** in Angular.
*   **Stitched OTP Array Field Components:** The security verification form displays **4 independent, isolated input boxes**. The custom Angular controller tracks keyboard behavior, automatically managing element focus shifts on keypresses and stitching the fragments into a unified payload array for the backend.

---

## 📊 Analytics, Scale & UX Features

### 📈 Analytics-Driven Agent Dashboard
*   **KPI Metrics & Charts:** High-level performance tracking rendered through dynamic, responsive data charts capturing active policy ratios, revenue metrics, and volume thresholds.
*   **High-Volume Scalability:** 
    *   **Search Vector:** Real-time customer indexing filtering by exact `Name` or `Contact No`.
    *   **Collapsible Customer Cards:** Hierarchical UI design containing lazy-loaded contextual nested tables for their respective policies. This layout shields the browser DOM from component layout bottlenecks when managing massive datasets.
*   **PDF Report Engine:** Server-side document assembler allowing agents to extract, compile, and instantly download historical customer policy metrics as structured audit-ready PDF reports.

### 👥 UX-Optimized Customer Portal
*   **Smart Pagination UI:** Customer policy tables are pre-configured with active data pagination, enforcing a clean limit of **5 rows per viewport** to optimize readability.
*   **Multi-Dimensional Filtering:** Direct client-side dropdown filtering sorting policy data structures instantly by Insurance Category rules.
*   **Comprehensive Profile Sidebar:** A single unified drawer UI controlling secure state mutations for personal details across both Agent and Customer dashboards.

---

## 🧪 Testing Metrics & Code Quality Assurance

This repository maintains an uncompromising standard of software quality, verifying system logic and data boundaries using a mock-isolated unit environment.

JUnit 5 Test Coverage Framework: 100 Total Test Cases 
● Controller Tests  [🟢 PASSED]
● Service Tests     [🟢 PASSED]
