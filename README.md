# 🏦 Secure Banking Core System

**Enterprise-grade financial ecosystem engineered with Java 17 & Spring Boot 3.**

[![Java](https://img.shields.io/badge/Java-17-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.1-green?style=flat-square&logo=spring-boot)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Enabled-blue?style=flat-square&logo=docker)](https://www.docker.com/)

The **Secure Banking Core System** is a microservices-ready modular monolith designed to bridge the gap between robust monolithic stability and distributed scalability.

---

### 🚀 Project Overview & Vision

* **☕ Backend Core:** High-performance engine utilizing **Amazon Corretto 17** and **Spring Boot 3**.
* **⚛️ Full-Stack Evolution:** Modern, responsive dashboard currently being integrated with **React.js** and **Tailwind CSS**.
* **🛡️ Security & Auth:** Stateless **RBAC** implementation via **Spring Security** and **JJWT**.
* **⛓️ Transactional Integrity:** Atomic fund transfers ensured by strict **ACID principles**.
* **📊 Financial Reporting:** Professional exports via **OpenPDF** (PDF) and **Apache POI** (Excel).
* **🛰️ Future-Ready:** Architecture prepared for **Apache Kafka** and Observability tools (**Prometheus & Grafana**).

---

### 🛠 Technical Stack (Full-Stack Ecosystem)

| Category | Technology | Version | Key Role | Status |
| :--- | :--- | :--- | :--- | :--- |
| **Runtime** | Java (Amazon Corretto) | 17.0.15 | Backend execution environment. | ✅ Stable |
| **Framework** | Spring Boot | 3.4.1 | Core application backbone. | ✅ Stable |
| **Frontend** | **React.js** | **Latest** | **Modern SPA Dashboard.** | 🏗️ *In Progress* |
| **Security** | Spring Security & JWT | 0.11.5 | Authentication and RBAC. | ✅ Stable |
| **Persistence** | PostgreSQL & Hibernate | 17.x / 6.x | Data integrity and ORM management. | ✅ Stable |
| **Testing** | JUnit 5 / Mockito / H2 | Managed | Comprehensive QA infrastructure. | ✅ Stable |
| **Reporting** | Apache POI / OpenPDF | Latest | Document generation engine. | ✅ Stable |

---

### 🧰 Development Tools & Applications

| Tool | Version | Purpose |
| :--- | :--- | :--- |
| **IntelliJ IDEA** | 2024.x | Primary Backend development IDE. |
| **VS Code** | Latest | Primary IDE for **React.js** integration. |
| **Postman** | Latest | REST API testing and documentation. |
| **pgAdmin 4** | 9.11 | PostgreSQL administration. |
| **Docker Desktop** | 4.58.0 | Containerized service orchestration. |
| **DBeaver** | 25.2.2 | Database design and SQL execution. |
| **Git & GitHub** | - | Version control and collaboration. |

---

### 🏛 Architecture & Design Philosophy

![Database Schema](./docs/diagrams/db-schema.png)

* **🧩 Strategic Modularity:** The system is intentionally built as a **Modular Monolith**. This ensures high transactional integrity and lower operational complexity while maintaining a clean separation between Auth, Account, and Transaction domains.
* **⚖️ ACID & Consistency:** By choosing this architecture over distributed microservices, we guarantee 100% atomic transaction management and data consistency, which are paramount for financial safety.
* **📐 SOLID Foundations:** Engineered for long-term maintainability, allowing the system to scale vertically and modularly without the overhead of network latency between services.

---

### 🚀 Future Roadmap & Vision

The following features are planned to further enhance the modular capabilities and observability of the ecosystem:

| Feature | Category | Description | Status |
| :--- | :--- | :--- | :--- |
| **Observability Stack** | **DevOps** | Integration of **Prometheus & Grafana** for real-time health metrics. | 📅 Planned |
| **Event-Driven Arch** | **Architecture** | Implementing **Apache Kafka** for asynchronous internal notifications. | 📅 Planned |
| **Advanced Security** | **Identity** | Transitioning to **OAuth2 / OpenID Connect** for enterprise IAM. | 📅 Planned |
| **Performance Tuning** | **Optimization** | Advanced caching strategies and database indexing for high-volume traffic. | 📅 Planned |

---

### 🌟 Key Features

![Swagger Documentation](../../OneDrive/Desktop/docs/screenshots/swagger-docs.png)

* **🛡️ Secure Auth:** Stateless JWT authentication with BCrypt password hashing.
* **💸 High-Precision Transfers:** Atomic engine ensuring data consistency.
* **🌐 Interactive API Docs:** Real-time endpoint exploration via **Swagger UI**.

---

### 🐳 Containerization & DevOps

![Docker Status](./docs/screenshots/docker-status.png)

* **🐳 Dockerization:** Identical environments across development, testing, and production.
* **🏗️ Orchestration:** **Docker Compose** managing the App and Database as one ecosystem.
* **🗄️ High Availability:** Persistent volume mapping to ensure banking data survives restarts.

---

### 💾 Installation & Setup

![Startup Logs](./docs/screenshots/startup-logs.png)

Follow these steps to get the system running locally:

1. **Clone the Repository:**
   ```bash
   git clone [https://github.com/BatuhanBaysal/secure-banking-core.git](https://github.com/BatuhanBaysal/secure-banking-core.git)
   cd secure-banking-core
   ```

2. **Environment Configuration:** Create a .env file in the root directory and add your credentials:
   ```bash
   DB_URL=jdbc:postgresql://db:5432/banking_db
   DB_USERNAME=your_db_user
   DB_PASSWORD=your_db_password
   JWT_SECRET=your_32_character_long_secret_key
   ```

3. **Deploy with Docker:**
   ```bash
   docker-compose up --build
   ```

4. **Access the Application:**
    * **Backend API:** `http://localhost:8080`
    * **Swagger UI:** `http://localhost:8080/swagger-ui.html`

---

Developed by [Batuhan Baysal](https://github.com/BatuhanBaysal)