# 🏛️ Architectural Design & Project Structure
This project is developed using a modular and sustainable **N-Tier Architecture** based on the **SoC (Separation of Concerns)** principle. Each layer is assigned a specific responsibility, and higher-level layers interact with lower-level layers through abstracted Interfaces.

### 🛠️ Tech Stack & Infrastructure
The system is built on a modern technology stack designed for high availability, data integrity, and professional schema management:

* **Core Framework:** **Spring Boot 3.4.2** with **Java 17**, ensuring a robust environment for enterprise banking logic.
* **Data Persistence:** **PostgreSQL** with **Spring Data JPA** for efficient ORM operations and relational data storage.
* **Database Migration:** **Liquibase** handles all schema changes. This ensures that every database environment (Dev, Test, Prod) is perfectly synchronized and versioned.
* **Messaging:** **RabbitMQ** for event-driven asynchronous tasks, such as automated transaction notifications.
* **API Security:** **Spring Security** with **JWT** for stateless authentication and **Resilience4j** for request rate-limiting.

### 🗄️ Professional Database Migration
Instead of manual scripts, the project uses **Liquibase** to manage the database lifecycle. Below is the internal log table that tracks applied migrations:

![Liquibase Changelog](docs/screenshots/05_database_migration_liquibase.png)
> **Insight:** The `databasechangelog` table guarantees that the "Initial banking schema" is correctly applied and synchronized across all instances.

### 📂 Project Directory Structure
The project hierarchy is organized according to established standards in enterprise Java applications:
**Path:** `src/main/java/com/batuhan/banking_service/`

| Folder | Description |
| :--- | :--- |
| `config/` | System configurations (Security, RabbitMQ, Swagger) |
| `constant/` | Application-wide constants and message management |
| `controller/` | REST API endpoints (The application's entry point) |
| `dto/` | Data Transfer Objects (Common, Request, Response, Event) |
| `entity/` | JPA database models and Enum types |
| `exception/` | Custom exception classes and Global Error Handling |
| `mapper/` | Entity-DTO mapping logic (MapStruct) |
| `repository/` | Data access and query layer (Spring Data JPA) |
| `service/` | Business Logic definitions (Interfaces) |
| `service/helper/` | Logical utility tools used by services |
| `service/impl/` | Concrete business logic (Implementations) |
| `service/messaging/` | Message Queue (RabbitMQ) producers and consumers |
| `validator/` | Custom data validation annotations (TCKN, IBAN, etc.) |

### 💎 Our Architectural Standards
* **Interface-First:** All services are initially defined as interfaces. This reduces dependencies between system components and facilitates the seamless implementation of Unit Tests.
* **Event-Driven:** Long-running processes (such as sending emails) are executed asynchronously via **RabbitMQ** to ensure that the main application flow is never blocked.
* **Centralized Validation:** Data validations are centralized within the Validator layer, preventing code duplication and enforcing uniform security standards for all API entries.
* **Automated Mapping:** Transformations between Entities and DTOs are automated using **MapStruct**, eliminating the risk of manual mapping errors.
* **Fail-Fast Strategy:** Utilizing a **Global Exception Handler** structure, errors are caught at the moment they occur and presented to the user in a standardized, meaningful format.

---

# 🚀 Project Configuration and Execution
This project is developed using the **Spring Boot 3** architecture. The configuration files define the technical infrastructure and runtime behavior of the application.

### 📜 Core Configuration Files

| File | Role | Description |
| :--- | :--- | :--- |
| **BankingServiceApplication.java** | Main Class | The entry point of the application. It loads the Spring Context via `@SpringBootApplication`. It also activates core capabilities using `@EnableAsync` (for asynchronous tasks) and `@EnableJpaAuditing` (for data auditing). |
| **pom.xml** | Dependency Management | The Maven configuration file. It manages versions for Spring Boot Starters (Web, JPA, Security), database drivers (PostgreSQL), MapStruct, Lombok, and critical libraries like **Resilience4j**. |
| **application.yml** | Runtime Settings | Contains database connection details, port configuration (8080), server context path, JPA/Hibernate properties, and **RabbitMQ** connection settings. |

### 🛠️ Technical Details
* **Database Management:** The PostgreSQL driver is defined within `application.yml`. The `jpa.hibernate.ddl-auto` setting is configured as `update` to ensure the database schema is automatically synchronized with the entities.
* **Dependency Orchestration:** Special configurations (`annotationProcessorPaths`) are defined in the `maven-compiler-plugin` within `pom.xml` to ensure that **MapStruct** and **Lombok** work seamlessly together during the compilation phase.
* **Asynchronous Operations:** The `@EnableAsync` annotation activated in the `BankingServiceApplication` class ensures that non-blocking operations, such as email notifications and logging, run in the background without slowing down the primary request flow.

> **Note:** The following log output demonstrates a successful system bootstrap, including database connectivity and RabbitMQ synchronization:

![App Startup Logs](docs/screenshots/03_app_startup_success.png)

---

# 🏗️ Data Model & Entity Architecture
This project utilizes **Hibernate/JPA** standards for database management and is built on a layered architecture to prevent code duplication and ensure data consistency.

### 📊 Database Schema
The following diagram illustrates the relational database structure and core entities:

![Database Schema](docs/screenshots/06_system_architecture_diagram.png)

### 💎 Core Architectural Approaches
* **BaseEntity Abstraction:** All database tables inherit from a `BaseEntity` class, which automatically tracks record creation and update timestamps.
* **JPA Auditing:** The lifecycle of data is automatically monitored using `@CreatedDate` and `@LastModifiedDate` annotations.
* **Optimistic Locking:** Data concurrency is managed using the `@Version` annotation to prevent "lost update" scenarios.
* **Relational Data Structure:** A normalized database structure is established using **OneToMany**, **ManyToOne**, and **OneToOne** relationships.
* **Performance Optimization:** **Database Indexing** is applied to frequently queried fields (such as `externalId`, `email`, `iban`, etc.) to enhance search performance.

### 📊 Entity Definitions
The following table summarizes the core entities in the system and their respective roles:

| Class Name | Database Table | Description |
| :--- | :--- | :--- |
| **BaseEntity** | - | The superclass providing common fields: `createdAt`, `updatedAt`, `lastModifiedBy`, and `version`. |
| **UserEntity** | `users` | Stores bank customers' identity, contact, and role information. Linked with addresses and accounts. |
| **AddressEntity** | `addresses` | Maintains physical address details. An address can be associated with multiple users. |
| **AccountEntity** | `accounts` | Manages customer bank accounts (IBAN, balance, currency). The core of all financial operations. |
| **AccountLimitEntity** | `account_limits` | Tracks daily spending limits and current daily usage for accounts. |
| **TransactionEntity** | `transactions` | Records money transfers between accounts (sender, recipient, amount, status). |
| **AuditLogEntity** | `audit_logs` | Used to monitor critical system actions (who, when, which IP, what action). |

### 🛠️ Technical Specifications
* **UUID Usage:** For security and external system integration, every entity includes a unique **UUID** field named `externalId`.
* **Lombok Support:** Utilizes `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, and the `@SuperBuilder` pattern to simplify object instantiation.
* **Data Precision:** For all currency and limit-related operations, **BigDecimal** is used to guarantee precise financial calculations.

---

# 📑 Business Logic & Enum Definitions
To maintain data integrity and minimize human error, critical parameters within the project are managed using **Enum** structures. Each Enum includes a descriptive `description` field, ensuring that the business workflow is inherently documented within the code.

### 🛠️ Enum Classes and Responsibilities
The following table summarizes the Enum structures used in the system and their functional roles:

| Enum Name | Scope | Content & Functionality |
| :--- | :--- | :--- |
| **AccountStatus** | Account State | Defines whether an account is **ACTIVE**, temporarily **SUSPENDED**, or permanently **CLOSED**. |
| **CurrencyType** | Currency | Determines the currency for transactions (**TRY, USD, EUR, GBP**) and contains associated symbols and descriptions. |
| **TransactionStatus** | Transaction State | Represents the lifecycle of a transfer: **PENDING**, **COMPLETED**, or **FAILED** due to an error. |
| **TransactionType** | Transaction Category | Categorizes the type of financial operation (**TRANSFER**, **DEPOSIT**, **WITHDRAW**, or **FEE**). |
| **Role** | User Authorization | Defines the access level of the user (**USER** for standard customers or **ADMIN** for bank management). |

### 🔍 Detailed Features
* **Rich Content:** Moving beyond simple constants, each Enum member is mapped to a unique descriptive text, making the code more expressive.
* **International Standards:** The `CurrencyType` enum allows us to manage currency-specific symbols (₺, $, €, £) directly at the code level, ensuring consistency in financial reporting.
* **Process Management:** Through `TransactionStatus`, the tracking of asynchronous or approval-required financial processes is standardized across the entire system.

---

# 🛡️ Data Validation & Verification Architecture
To ensure system-wide data consistency and minimize financial security risks, a **Custom Annotation-based** validation layer has been implemented. This structure ensures that business rules are enforced at the **Controller** level, preventing invalid data from ever reaching the service layer.

### 🔍 Custom Validation Definitions
The project features four core custom validation mechanisms designed for banking-specific requirements:

| Annotation | Validator Class | Description & Rule Set |
| :--- | :--- | :--- |
| **@MinAge** | `AgeValidator` | Checks the user's birth date against the legal banking age limit (Default: 18). |
| **@ValidIban** | `IbanValidator` | Verifies the IBAN format and confirms mathematical accuracy using the **MOD 97** algorithm. |
| **@ValidPassword** | `PasswordStrengthValidator` | Enforces a strong password policy (at least one uppercase, one lowercase, one digit, one special character, and min. 8 characters). |
| **@ValidTckn** | `TcknValidator` | Performs a comprehensive validation of the **T.C. Identity Number** algorithm (11 digits, non-zero first digit, checksum and MOD checks). |

### 🛠️ Technical Implementation Details
* **Extensibility:** By implementing the `ConstraintValidator` interface, each validator is designed to be customizable with different error messages and parameters.
* **Performance:** Validations are performed at the application level **before** interacting with the database, ensuring efficient use of system resources.
* **Algorithmic Verification:**
    * **TCKN:** Beyond simple length checks, the system performs complex mathematical checksum calculations for the 10th and 11th digits.
    * **IBAN:** Letters are converted to numeric values and processed using `BigInteger` to comply with international verification standards.

---

# 📦 DTO (Data Transfer Object) Layer
Data transfer across the application is handled using **Java Records**, a modern and lightweight structure. The DTO layer is organized into four main categories: **Request, Response, Common,** and **Event**.

### 🔄 DTO Categories and Class Details
The following table summarizes the roles of the 13 DTO classes used in the system:

| Category | DTO Class | Description |
| :--- | :--- | :--- |
| **Common** | `GlobalResponse` | A standard generic container for all API responses; includes success status, messages, and data payload. |
| | `AddressDto` | Carries address details (country, city, zip code) with embedded validation rules. |
| | `TransactionCategoryDTO` | Represents the distribution of expenses by category, including amounts and percentages. |
| | `TransactionSummaryDTO` | Holds a summary of total sent/received amounts and transaction counts. |
| | `WeeklyTrendDTO` | Used to track date-based financial change trends. |
| **Request** | `UserCreateRequest` | Contains all necessary information and validations (TCKN, Password, etc.) for new user registration. |
| | `UserUpdateRequest` | Data model used during the update of existing user information. |
| | `AccountCreateRequest` | Represents a request to open a new account with customer ID, limits, and currency. |
| | `TransactionRequest` | Carries amount and currency information for inter-IBAN money transfer operations. |
| **Response** | `UserResponse` | Presents user information to the external world while hiding sensitive data (e.g., passwords). |
| | `AccountResponse` | Response model containing account balance, status, and IBAN details. |
| | `TransactionResponse` | Provides transaction history details with sender/receiver names and reference numbers. |
| **Event** | `TransferEvent` | Event data used for inter-system communication or asynchronous operations (e.g., email notifications). |

### 💎 Key Technical Features
* **Data Security & Validation:** Request classes are equipped with standard validations like `@NotBlank`, `@Size`, and `@Email`, alongside custom validators such as `@ValidTckn`, `@ValidIban`, and `@ValidPassword`.
* **Immutability:** Thanks to the **Java Record** structure, DTOs are immutable once created, which ensures **thread-safety** and prevents unintended side effects.
* **Flexible Response Structure:** The `GlobalResponse<T>` class utilizes **Generics** to unify responses from all services into a standardized format.
* **Financial Precision:** In all fields related to money (`amount`, `balance`, `totalSent`), **BigDecimal** is used to prevent precision loss during calculations.

---

# 🔄 Object Mapping (Mapper) Architecture
To manage data conversion between DTOs and Entities, the project utilizes **MapStruct**, a high-performance and type-safe code generation library. This layer minimizes "boilerplate" code, ensuring a clean and controlled data flow between the architectural layers.

### 🛠️ Mapper Interfaces and Responsibilities
The system features four core mapper interfaces that handle object transformations:

| Mapper Interface | Description |
| :--- | :--- |
| **AccountMapper** | Converts `AccountCreateRequest` to `AccountEntity` while initializing the account status as **ACTIVE** by default. It also automatically extracts the customer ID from the associated user during response mapping. |
| **AddressMapper** | Provides bidirectional transformation between Address DTOs and Entities. It utilizes `NullValuePropertyMappingStrategy.IGNORE` during updates to ensure only non-null fields are updated. |
| **TransactionMapper** | Transforms complex transaction data into user-friendly responses. It maps sender/receiver IBANs directly from associated accounts and resolves full names from user entities via a custom `@Named` method (`getFullName`). |
| **UserMapper** | Manages mapping for user creation, updates, and viewing. It integrates **AddressMapper** to handle nested address transformations and automatically assigns the **USER** role to new registrations. |

### 💎 Technical Design Decisions
* **Spring Integration:** All mappers are defined as Spring Beans using `@Mapper(componentModel = "spring")`, allowing for seamless dependency injection into the service layer.
* **Secure Mapping:** Sensitive fields (such as IDs, passwords, and audit fields) are excluded from mapping using `@Mapping(target = "...", ignore = true)`. This prevents external requests from manipulating protected data.
* **Partial Update Support:** Through `updateEntityFromDto` methods, existing entities can be updated only with changed fields from the DTO, without the need to reload the entire object.
* **Custom Business Logic:** As seen in the `TransactionMapper`, the project goes beyond simple property copying by implementing custom transformation logic (e.g., name concatenation) using `@Named` and default methods.

---

# 🗄️ Data Access Layer (Repository)
The Repository layer manages the application's communication with the database. In this project, data access processes are standardized using **Spring Data JPA**, and advanced techniques such as **JPQL** and the **Criteria API (Specifications)** are utilized for complex queries.

### 🛠️ Repository Classes and Key Features
The system features seven specialized repositories, each tailored to its respective entity:

| Repository | Primary Responsibilities & Custom Methods |
| :--- | :--- |
| **AccountRepository** | Manages accounts. Implements **Pessimistic Locking** via `findByIbanWithLock` to maintain data consistency during concurrent money transfers. |
| **TransactionRepository** | Manages transaction history. Supports flexible filtering via `JpaSpecificationExecutor` and includes custom JPQL queries for weekly spending trends and summaries. |
| **UserRepository** | Provides access to user data. Optimized for rapid querying through unique fields such as email, TCKN, or customer number. |
| **AccountLimitRepository** | Tracks daily spending limits for accounts. Performs limit checks for specific accounts and dates (`findByAccountIdAndLimitDate`). |
| **AuditLogRepository** | Accesses security and tracking logs. Lists past actions by user email, ordered from newest to oldest (`OrderByCreatedAtDesc`). |
| **AddressRepository** | Executes standard CRUD operations and ensures the persistence of physical address information. |
| **TransactionSpecifications** | The dynamic query engine. Uses the **Criteria API** to build complex filters based on IBAN, date range, or transaction amount. |

### 💎 Technical Design Details
* **Concurrency Control:** The `@Lock(LockModeType.PESSIMISTIC_WRITE)` annotation used in the `AccountRepository` prevents **race conditions** by queuing simultaneous transactions attempting to access the same account balance.
* **Performance Optimization:** The "N+1 query problem" is eliminated through the strategic use of **JOIN FETCH**. Related data (such as User and Account) is retrieved in a single database round-trip, significantly enhancing performance.
* **Dynamic Filtering:** Thanks to the `TransactionSpecifications` class, users can freely filter their transaction history based on dates, amounts, or specific account criteria.
* **Statistical Queries:** Custom constructor queries (`SELECT new ...`) defined within the `TransactionRepository` return DTO objects directly from the database. This avoids loading heavy entities into memory and improves efficiency.

---

# 🧠 Service Layer (Business Logic Layer)
The Service Layer acts as the **"brain"** of the application. It receives requests from the Controller, applies business rules, enforces security policies, and interacts with the Repository layer to process data. This project follows an **"Interface-First"** approach to maximize flexibility and testability.

### 🛠️ Service Interfaces & Responsibilities
The system is coordinated by **8 core service interfaces**, each with a distinct focus:

| Service Interface / Component | Responsibility Area | Key Functions |
| :--- | :--- | :--- |
| **AccountService** | Account Management | Manages new account creation (including IBAN generation), balance inquiries, and account closure processes. |
| **TransactionService** | Financial Operations | Handles fund transfers, transaction history filtering, weekly trend analysis, and spending categorization. |
| **UserService** | Customer Management | Orchestrates user registration, profile updates, and customer number-based tracking. |
| **RabbitMQProducer** | Async Messaging | Forwards `TransferEvent` objects to the queue to ensure cross-system communication after successful transactions. |
| **AuditService** | Auditing & Monitoring | Ensures observability by logging critical system actions into the database. |
| **EmailService** | Notification Management | Sends automated transaction details and reference numbers to users via email. |
| **ExcelService** | Data Reporting | Converts transaction history into banking-standard XLSX (Excel) files. |
| **PdfService** | Receipt Generation | Generates official digital PDF receipts for completed financial transactions. |

### 💎 Architectural Design Principles
* **Event-Driven Architecture:** Utilizing `RabbitMQProducer`, time-consuming tasks like email dispatching are executed asynchronously without blocking the main application flow.
* **Analytical Capabilities:** `TransactionService` provides Business Intelligence (BI) insights, such as spending trends and category distribution, rather than just raw data storage.
* **Rich Reporting:** The system offers a professional banking experience by transforming raw data into user-friendly PDF (receipts) and Excel (bulk reports) formats.
* **Abstraction & Testability:** Defining all services through interfaces allows for seamless Unit Testing using mock objects.

---

# ⚙️ Service Implementations
This layer is where the business rules are materialized, data integrity is enforced, and integrations with external systems (Email, RabbitMQ, Database) are managed.

### 🛠️ Implementation Classes & Technical Details

| Implementation Class | Responsibility & Featured Specs |
| :--- | :--- |
| **UserServiceImpl** | Manages the user lifecycle. Optimizes performance using `@CacheEvict` and `@CachePut`. |
| **AccountServiceImpl** | Executes account operations. Handles IBAN generation and user authorization via `businessValidator`. |
| **TransactionServiceImpl** | Implements transfer logic. Updates balances via `AccountingManager` and ensures data safety with `@Transactional`. |
| **LimitServiceImpl** | Monitors spending limits. Tracks daily expenditures and throws `DailyLimitExceededException` upon breach. |
| **AuditServiceImpl** | Performs non-blocking system logging using `@Async`. Automatically detects the user's IP address. |
| **EmailServiceImpl** | Sends HTML-formatted notifications via `JavaMailSender`, including transaction specifics (amount, IBAN, ref no). |
| **ExcelServiceImpl** | Leverages the **Apache POI** library to transform transaction logs into professional XLSX reports. |
| **PdfServiceImpl** | Uses the **iText** library to generate dynamic receipts featuring bank logos and timestamps. |
| **RabbitMQProducerImpl** | Dispatches `TransferEvent` objects in JSON format to the designated queue asynchronously. |

### 💎 Advanced Architectural Features
* **Financial Integrity (ACID):** Within `TransactionServiceImpl`, balance and limit updates are wrapped in a single database transaction; any failure triggers an automatic **rollback**.
* **Caching Strategy:** The Spring Cache mechanism implemented in `UserServiceImpl` minimizes database load by keeping frequently accessed user data in memory.
* **IP Detection:** `AuditService` captures real user IPs even behind proxies by inspecting the *X-Forwarded-For* header.

---

# ⚡ Service Helpers & Messaging Components
These components lighten the load on service implementations, handle repetitive tasks, and manage the system's asynchronous capabilities (RabbitMQ), keeping the business logic clean and modular.

### 🔍 Helper Components & Functions

| Component | Category | Responsibility Area |
| :--- | :--- | :--- |
| **AccountingManager** | Financial Manager | Handles balance updates and daily limit tracking within a single transaction, adhering to ACID principles. |
| **SecurityHelper** | Security Utility | Provides secure access to current session (JWT) user data via `SecurityContextHolder`. |
| **RabbitMQConsumer** | Message Listener | Constantly monitors the queue; triggers the email notification process once a new transfer event is detected. |

### 💎 Technical Critical Tasks
* **Data Consistency:** `AccountingManager` uses `Propagation.REQUIRED` to ensure the "all-or-nothing" principle for balance and limit updates.
* **Async Workflow:** `RabbitMQConsumer` is the exit point of the event-driven architecture, ensuring users receive immediate responses without waiting for email delivery.
* **JWT Integration:** `SecurityHelper` abstracts the complexity of the Spring Security authentication object, providing identity data through static utility methods.
* **Fail-Safety:** `AccountingManager` prevents inconsistent financial records by validating sufficient balances at the lowest level before execution.

---

# 🎮 API Layer (Controller)
The Controller layer serves as the gateway of the application to the external world. This project implements a structure that strictly adheres to **RESTful standards**, prioritizes security, and provides comprehensive API documentation via **Swagger/OpenAPI**.

![Swagger UI Documentation](docs/screenshots/04_swagger_docs.png)

### 🛠️ Controller Classes & Responsibilities
There are **3 primary controllers** that bridge the business logic to the endpoints:

| Controller | Responsibility Area | Key Features |
| :--- | :--- | :--- |
| **AccountController** | Bank Account Management | Handles account opening, balance inquiries, and listing. Uses `RateLimiter` for request throttling and `@PreAuthorize` for account-level authorization. |
| **TransactionController** | Financial Operations | Manages money transfers, transaction history, and summary reports. Supports dynamic filtering (Spring Data Specification) by date/amount and weekly trend analysis. |
| **UserController** | User Lifecycle | Manages registration, updates, profile viewing, and soft-delete operations. Enforces access control based on **Admin** roles or **Resource Ownership**. |

### 💎 Technical Design & Standards

* **Security (Spring Security):** Method-level security is enforced using `@PreAuthorize`. Logic like `@bankingBusinessValidator.isOwner` ensures users can only access their own data, while administrative actions are restricted to the Admin role.
* **Rate Limiting:** Critical endpoints are protected using **Resilience4j** with the `@RateLimiter` annotation, safeguarding the system against brute-force attacks and request flooding.
* **API Documentation (OpenAPI/Swagger):** Every endpoint is documented using `@Tag` and `@Operation` annotations, detailing the endpoint's purpose, required parameters, and potential HTTP error codes.
* **Standardized Response Structure:** All endpoints utilize a `GlobalResponse` wrapper, ensuring a consistent JSON structure that includes the data payload, success messages, and status codes.
* **HTTP Standards & RFC Compliance:** For resource creation (`POST`), the system returns the `Location` header using `ServletUriComponentsBuilder`, following RFC standards for RESTful services.
* **Observability (Logging):** Every incoming request is logged via **SLF4J** before processing, enhancing system traceability and easier debugging.

---

## 🚦 API Endpoints & Proof of Work
The following section demonstrates the core functional flow. For more examples, visit the [Postman Screenshots Folder](docs/screenshots/postman/).

### 🔄 Core Transaction Flow (Example)
The system orchestrates a secure path from user registration to digital receipt generation.

| Step | Action | Outcome                                                        |
| :--- | :--- |:---------------------------------------------------------------|
| **1. Register** | `POST /api/v1/users` | [View Details](docs/screenshots/postman/01_register_user.png)  |
| **2. Account** | `POST /api/v1/accounts` | [View Details](docs/screenshots/postman/03_create_account.png) |
| **3. Transfer**| `POST /api/v1/transactions/transfer` | Real-time money transfer between accounts.                     |

#### 📸 Transfer Execution Result
![Money Transfer Request](docs/screenshots/postman/04_transfer_money.png)

Upon success, the system triggers an **Asynchronous Email** and generates a **Digital Receipt**:
* 📩 [View Email Notification](docs/screenshots/postman/05_transfer_email_notification.png)
* 📄 [View PDF Receipt](docs/screenshots/postman/07_transaction_receipt_pdf.png)

---

# ⚙️ Configuration & System Settings (Config)
The Config layer is the central hub that dictates the application's runtime behavior, organizes security protocols, and manages seed data. This project utilizes a modular configuration approach to enhance system flexibility and maintainability.

### 🛠️ Configuration Classes & Responsibilities
The system features **7 core configuration and utility classes**, each serving a specific architectural purpose:

| Class / Component | Responsibility Area |
| :--- | :--- |
| **SecurityConfig** | Acts as the application's security shield. Manages **JWT-based OAuth2** protection, CORS policies, and endpoint-level (Public/Private) access authorizations. |
| **OpenApiConfig** | Configures Swagger UI integration. Defines the **"Bearer Token"** security scheme, allowing protected endpoints to be tested directly through the documentation. |
| **JpaConfig** | Enables `@EnableJpaAuditing` to automatically populate audit fields (e.g., creation dates, last modified) in `BaseEntity` classes. |
| **DataInitializer** | Bootstraps the system by automatically creating test users, accounts, addresses, and audit logs upon application startup. |
| **DataGenerator** | An algorithmic utility class used to generate random but valid data (e.g., **TCKN Checksum** compliant, **MOD97 IBAN** compliant). |
| **Messages** | Centralizes all application-wide success and error messages (String constants) to ensure consistency and facilitate future localization (i18n). |
| **OpenApiProperties** | A configuration model that externalizes Swagger titles, versioning, and contact details via application properties. |

### 💎 Technical Details & Approaches
* **Security Architecture:** Utilizing **stateless session management** on Spring Security, every request is validated via JWT. The separation of concerns between Admin and User roles is centrally defined here.
* **Smart Data Generation:** Instead of simple random strings, the `DataGenerator` class simulates real-world TCKN (Turkish Identity Number) and IBAN algorithms. This ensures that test data can successfully pass through the validation layer.
* **Audit Support:** Thanks to `JpaConfig`, metadata such as "who created this record and when" is persisted automatically without manual intervention in the service layer.
* **Resilience in Initialization:** Errors occurring during the seeding process (`DataInitializer`) are wrapped in try-catch blocks to prevent startup failures, ensuring the application remains resilient even if initial data insertion fails.

---

# 🚨 Exception Handling Architecture
To enhance application resilience and user experience, a centralized exception management strategy has been implemented. This structure catches unexpected system errors and transforms banking business rule violations into meaningful, standardized responses.

### 🛠️ Custom Exception Classes & Hierarchy
The error management hierarchy consists of **7 core components**:

| Class / Component | Responsibility Area |
| :--- | :--- |
| **GlobalExceptionHandler** | Listens to the entire application via `@RestControllerAdvice`. It packages caught errors into the `GlobalResponse` format for a consistent API output. |
| **BankingServiceException** | The base class for all custom exceptions in the project. It carries `HttpStatus` information to ensure the correct HTTP code is returned. |
| **InsufficientFundsException** | Triggered during transfers or withdrawals when the account balance is lower than the transaction amount. |
| **DailyLimitExceededException** | Activated when an account exceeds its predefined total transaction limit for the current day. |
| **AccountStatusException** | Thrown when an operation is attempted on **SUSPENDED** or **CLOSED** accounts. |
| **IneligibleAgeException** | Used to reject applications that fall below the mandatory age limit (e.g., 18) for opening a bank account. |
| **EmailAlreadyExistsException** | Prevents data duplication when a user tries to register with an email address already present in the database. |

### 💎 Technical Design & Flexibility
* **Centralized Control:** Thanks to the `GlobalExceptionHandler`, any error thrown across the application (validation errors, resilience limits, authorization issues) is caught, logged, and processed at a single point.
* **Resilience4j Integration:** Errors originating from resilience mechanisms like `@RateLimiter` or `@Bulkhead` (e.g., `RequestNotPermitted`) are intercepted to provide user-friendly feedback such as "Too many requests."
* **Validation Feedback:** Errors from DTOs marked with `@Valid` (`MethodArgumentNotValidException`) are returned as a detailed map, specifying exactly which field failed and why (e.g., "IBAN format is invalid").
* **Security & Privacy:** Sensitive information, such as stack traces, is strictly hidden from error responses; only the message and the status code are shared with the client to prevent information leakage.

---

# 📂 Constants & Message Management
To mitigate the risks associated with hard-coding and to enhance code readability, all text-based error messages, success notifications, and static values used throughout the application are managed within a centralized structure. This approach ensures that messages can be updated from a single source of truth.

### 🛠️ Message Class & Functions
The project features a primary class dedicated to the management of all constants:

| Constant Class | Responsibility Area |
| :--- | :--- |
| **Messages** | Contains all success and informational message strings used across user operations, account management, and fund transfer processes. |

### 💎 Technical Design Features

* **Immutability:** The `Messages` class is defined as `final`, and its constructor is made `private` to prevent instantiation. All variables are declared as `public static final`, guaranteeing that they remain constant and unchangeable throughout the application lifecycle.
* **Centralized Management:** When an error message or a user notification needs to be modified, there is no need to traverse 13+ DTOs or 3+ Controller classes; updating only the `Messages` class is sufficient.
* **Consistency:** By ensuring that messages used in API responses remain uniform across the entire application, a standardized and professional user experience is maintained.

---

# 🧪 Test Strategy & Directory Structure
This project is built with a strong focus on code quality and financial data security, following the **Test Pyramid** principle. The test suite ranges from fast, isolated **Unit Tests** to comprehensive **Integration Tests** that verify the application context and end-to-end flows.

### 📂 Test Directory Structure
Tests are organized to mirror the main application package structure for clarity and maintainability:
**Path:** `src/test/java/com/batuhan/banking_service/`

| File / Package | Description |
| :--- | :--- |
| **BankingServiceApplicationTests** | **Smoke Tests:** Ensures the Spring Context loads correctly. |
| **TestDataFactory** | **Central Data Factory:** Utility for generating valid test data (IBAN, TCKN, etc.). |
| **`controller/`** | **Web Layer Tests:** Verifies REST endpoints, JSON mapping, and Security/RBAC. |
| **`service/`** | **Business Logic Tests:** Unit tests utilizing **Mockito** to isolate service rules. |
| **`repository/`** | **Data Access Tests:** Uses **H2 In-Memory Database** to verify queries and constraints. |
| **`validator/`** | **Algorithm Tests:** Direct testing of TCKN, IBAN, and Password validation logic. |

### 🛠️ Detailed Test Scope
* **Controller Layer:** Validates API contracts, HTTP status codes, and Role-Based Access Control (RBAC) using `@WebMvcTest`.
* **Service Layer:** Focused on core banking rules (e.g., fund sufficiency, limit checks) using Mockito for dependency isolation.
* **Repository Layer:** Tests custom JPQL queries, dynamic filtering (Specifications), and Soft-Delete mechanisms.
* **Validator Layer:** Ensures mathematical correctness for critical algorithms:
    * **TCKN:** 11-digit checksum validation.
    * **IBAN:** International MOD-97 algorithm verification.
    * **Age:** Legal banking age (18+) enforcement.

### ⚙️ Test Configuration
The testing environment is isolated from production settings to ensure safety and speed:
**Path:** `src/test/resources/`

* **`application-test.yml`**: Specialized configuration for the test profile.
* **Database:** Uses **H2 In-Memory Database** for rapid execution without external dependencies.
* **Messaging:** RabbitMQ is mocked or disabled to ensure tests remain fast and deterministic.

---

# 🧪 Testing Strategy & Quality Assurance
Due to the critical nature of financial transactions, this project is designed with a high test coverage ratio and a strong focus on reliability. The testing infrastructure is architected to isolate real database dependencies and ensure algorithmic data integrity.

> **Status:** All **108 tests** are passing successfully across all layers.
![Test Success Summary](docs/screenshots/20_banking_service_tests.png)

---

### 🏗️ Core Test Components & Responsibilities
The testing layer consists of 4 primary components that manage the system's reliability:

| Component | Role | Technical Description |
| :--- | :--- | :--- |
| **BankingServiceApplicationTests** | **Smoke Test** | Uses `@SpringBootTest` to verify that the entire Application Context bootstraps without errors. |
| **TestDataFactory** | **Data Generation Center** | Produces mathematically valid mock data (IBAN, TCKN Checksum) compatible with MOD-97 algorithms. |
| **`application-test.yml`** | **Test Configuration** | Contains dedicated profile settings (`ActiveProfiles("test")`) to redirect messaging and DB connections to mock modes. |
| **H2 Database Config** | **In-Memory Database** | Provides a fast, PostgreSQL-independent environment with a zero-state (`mem:testdb`) for every test execution. |

### 💎 Highlighted Technical Approaches
* **Algorithmic Accuracy (TestDataFactory):** Since TCKN and IBAN validations are strictly enforced, ordinary random data fails. The factory simulates real-world validation rules (e.g., checksum parity) to provide "realistic" test data.
* **Isolated Integration Tests:** Uses `DB_CLOSE_DELAY=-1` and `DATABASE_TO_LOWER=TRUE` settings to provide a consistent and clean SQL environment for every test method.
* **Rapid Feedback:** Utilizing the **H2 database** allows hundreds of tests to complete in seconds, ensuring fast feedback loops in CI/CD pipelines.

---

### 🗄️ Repository Layer Testing
Repository tests are performed in an isolated environment using the **H2 Database** to verify data access logic, database constraints, and query accuracy.

| Test Group | Responsibility Area |
| :--- | :--- |
| **User Tests** | Validates user registration, `customerNumber` queries, and "Soft Delete" state transitions at the data level. |
| **Account Tests** | Verifies IBAN uniqueness, balance consistency, and relational Join queries between accounts and users. |
| **Transaction Tests** | Audits financial record accuracy, date-range filtering, and dynamic query results generated via `TransactionSpecifications`. |

* **Data Integrity:** Repository tests guarantee not only data retrieval but also duplicate record prevention and null-check enforcement.
* **Query Performance:** Ensures that custom JPQL and Criteria API queries fetch the expected datasets using correct strategies to prevent the N+1 problem.

---

### ⚙️ Service Layer Testing
Service tests verify the core business logic and financial rules. This layer uses **Mockito** to mock dependencies (RabbitMQ, Mail, Repositories), focusing exclusively on the internal logic of the service class.

| Test Group | Responsibility Area |
| :--- | :--- |
| **User Service** | Validates business rules for user lifecycle (age limits, duplicate checks, etc.). |
| **Account Service** | Audits account opening rules, IBAN generation, and authorization checks for account closure. |
| **Transaction Service** | Simulates fund transfer logic, balance checks, and event-driven messaging triggers in isolation. |

* **Behavioral Verification:** During transfer tests, the system verifies the flow (e.g., "how many times was this method called") using `@ExtendWith(MockitoExtension.class)`.
* **Negative Testing:** Ensures that scenarios like insufficient funds or limit violations throw the correct `BankingServiceException` and return expected error codes.

---

### 🌐 Controller (Web Layer) Testing
Controller tests verify the REST endpoints—the gateway to the external world. Using **MockMvc**, these tests confirm request handling, authorization, and response mapping without starting a full HTTP server.

| Test Group | Responsibility Area |
| :--- | :--- |
| **User Controller** | Tests HTTP status codes, data validation, and JSON serialization for user profiles. |
| **Account Controller** | Verifies access controls for account creation and accurate DTO transformations. |
| **Transaction Controller** | Audits **Role-Based Access Control (RBAC)** and ownership checks (`isAccountOwner`) for financial history. |

* **Security Testing:** Uses `@WebMvcTest` and `@EnableMethodSecurity` to verify `@PreAuthorize` expressions and SpEL logic.
* **Validation Enforcement:** Confirms that invalid payloads (e.g., bad IBAN format) are correctly rejected with `400 Bad Request`.

---

### 🛡️ Validator Layer Testing
Validators act as the "first line of defense." These tests verify that data conforms to legal algorithms and business rules before reaching the Controller.

| Test Group | Responsibility Area |
| :--- | :--- |
| **Age Validator** | Verifies the "18+ age limit" using critical boundary dates (LocalDate). |
| **Iban Validator** | Validates the MOD-97 checksum algorithm for Turkey-format IBANs. |
| **Tckn Validator** | Tests the 11-digit T.C. Identity Number checksum and digit-relation rules. |
| **Password Validator** | Enforces complexity rules (length, special characters) before database persistence. |

* **Boundary Analysis:** Uses `LocalDate` to test age rules at the exact transition points (one day before/after 18th birthday).
* **Parameterized Tests:** Utilizes `@ParameterizedTest` to quickly scan dozens of "valid" and "invalid" datasets within a single test method.
* **Fail-Safe Security:** Confirms that null or empty inputs are handled gracefully (returning `false`) instead of causing system crashes.