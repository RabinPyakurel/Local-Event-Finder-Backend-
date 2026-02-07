# Local Event Finder - Backend API

A production-grade RESTful backend for a local event discovery and management platform built with **Spring Boot**. The system supports event creation, ticketing with QR code check-in, dual payment gateway integration, community groups, and a full admin moderation panel — all secured with **JWT authentication** and a granular **Role-Based Access Control (RBAC)** system.

---

## Tech Stack

| Layer            | Technology                          |
|------------------|-------------------------------------|
| Language         | Java 17                             |
| Framework        | Spring Boot 4.0.1                   |
| Security         | Spring Security + JWT (JJWT 0.11.5) |
| ORM              | Spring Data JPA / Hibernate         |
| Database         | PostgreSQL (Supabase-hosted)        |
| Build Tool       | Maven                               |
| API Docs         | Springdoc OpenAPI 3.0 (Swagger UI)  |
| Email            | Spring Mail (Gmail SMTP)            |
| QR Codes         | Google ZXing 3.5.2                  |
| Payments         | Khalti API v2 & eSewa ePay v2       |

---

## Architecture Overview

```
┌─────────────┐     ┌──────────────────────────────────────────────────┐
│   Client     │────▶│  Spring Security Filter Chain                    │
│  (React/App) │     │  ┌────────────────────────────────────────────┐  │
└─────────────┘     │  │  JwtAuthenticationFilter                   │  │
                    │  │  • Extract & validate Bearer token          │  │
                    │  │  • Check token blacklist (logout support)   │  │
                    │  │  • Load user + roles + permissions          │  │
                    │  └────────────────────────────────────────────┘  │
                    └──────────────┬───────────────────────────────────┘
                                   │
                    ┌──────────────▼───────────────────────────────────┐
                    │  Controllers (REST endpoints)                     │
                    │  @PreAuthorize role/permission checks             │
                    └──────────────┬───────────────────────────────────┘
                                   │
                    ┌──────────────▼───────────────────────────────────┐
                    │  Service Layer (business logic)                   │
                    └──────────────┬───────────────────────────────────┘
                                   │
                    ┌──────────────▼───────────────────────────────────┐
                    │  Repository Layer (Spring Data JPA)               │
                    └──────────────┬───────────────────────────────────┘
                                   │
                    ┌──────────────▼───────────────────────────────────┐
                    │  PostgreSQL Database                              │
                    └──────────────────────────────────────────────────┘
```

---

## Key Features

### Event Management
- Full CRUD for events with image uploads, location coordinates, capacity limits, and pricing
- Tag-based categorization across 10 interest categories
- Search and explore with location-based filtering (Haversine distance calculation)
- Personalized event recommendations based on user interests
- Event lifecycle management: `ACTIVE` → `COMPLETED` / `CANCELLED` / `FLAGGED`

### Ticketing & QR Code Check-in
- Automatic unique ticket code generation per enrollment
- QR code generation (ZXing) for each ticket
- Organizer-side ticket verification and scan-based check-in
- Multi-ticket booking support with per-event limits
- Ticket status tracking: `ACTIVE` → `USED` / `CANCELLED`

### Payment Processing
- **Khalti** integration — initiate payment, receive callback, verify transaction
- **eSewa** integration — form-based payment flow with HMAC verification
- Automatic enrollment creation on successful payment
- Admin-controlled refund processing with audit trail
- Payment status lifecycle: `PENDING` → `COMPLETED` / `FAILED` / `REFUNDED`

### Community & Groups
- Group creation with optional membership approval workflows
- Group-scoped events (public/private)
- Group membership roles (member / group admin) with status management
- Tag-based group discovery

### User System
- User profiles with interests, profile images, and follow/follower relationships
- Role upgrade requests (User → Organizer) with admin approval workflow
- OTP-based password reset via email (10-minute expiry, one-time use tokens)
- Account status controls: `ACTIVE`, `INACTIVE`, `BLOCKED`, `SUSPENDED`

### Notification System
- In-app notifications for enrollments, cancellations, follows, feedback, payments, and more
- Paginated notification retrieval with unread count tracking
- 9 notification types covering all major platform events

### Admin Moderation Panel
- User management — suspend, block, reactivate accounts with email notifications
- Event moderation — flag, review, and remove events
- Report resolution — review user-submitted event reports
- Payment administration — view transactions, process refunds
- Role upgrade management — approve/reject organizer requests with admin notes
- Group moderation and oversight

### Email Notifications
- Password reset OTP delivery
- Event cancellation alerts with refund details
- Account suspension/reactivation notices

---

## Security & Authentication

### JWT Authentication
- Stateless authentication using signed JWT tokens (JJWT)
- Token contains `userId`, `email`, and `roles`
- Configurable expiration (default: 24 hours)
- **Token blacklisting** on logout — prevents reuse of invalidated tokens
- Custom `JwtAuthenticationFilter` runs once per request before authorization

### Password Security
- BCrypt hashing with **strength factor 12**
- JWT signing key: 256-bit secret via environment variable

### Session & Transport Security
- Stateless session management (no server-side sessions)
- CSRF protection disabled (appropriate for stateless JWT architecture)
- CORS restricted to configured frontend origins
- Custom `AuthenticationEntryPoint` for structured 401 error responses

---

## RBAC & Permissions

The system implements a **two-layer authorization model** — roles define broad access levels, while permissions provide granular control over individual operations.

### Roles

| Role        | Description                                                       |
|-------------|-------------------------------------------------------------------|
| `USER`      | Browse events, enroll, submit feedback, follow users, report events |
| `ORGANIZER` | Everything a User can do + create/manage events and groups        |
| `ADMIN`     | Full platform control — moderation, user management, refunds      |

### Permissions

| Permission         | Description                         |
|--------------------|-------------------------------------|
| `EVENT_READ`       | View event details and listings     |
| `EVENT_CREATE`     | Create new events                   |
| `EVENT_UPDATE`     | Modify owned events                 |
| `EVENT_DELETE`     | Cancel/delete owned events          |
| `EVENT_ENROLL`     | Enroll in events                    |
| `EVENT_FEEDBACK`   | Submit ratings and comments         |
| `EVENT_REPORT`     | Report problematic events           |
| `USER_FOLLOW`      | Follow/unfollow other users         |
| `GROUP_MANAGE`     | Create and manage groups            |
| `ADMIN_MODERATE`   | Full moderation capabilities        |

### How It Works

```
User ──── many-to-many ──── Role ──── many-to-many ──── Permission
```

- Roles and permissions are stored as separate entities with a many-to-many relationship
- Users are assigned roles, and each role maps to a set of permissions
- **Method-level security** via `@PreAuthorize` annotations on controller endpoints
- Admin registration is protected by a server-side secret key
- User status (`ACTIVE`, `BLOCKED`, `SUSPENDED`) is checked during authentication — blocked/suspended users are denied access at the security filter level

---

## API Endpoints

### Public (No Authentication)

| Method | Endpoint                            | Description                |
|--------|-------------------------------------|----------------------------|
| POST   | `/api/auth/register`                | User registration          |
| POST   | `/api/auth/login`                   | User login                 |
| GET    | `/api/events`                       | List events                |
| GET    | `/api/events/{id}`                  | Event details              |
| GET    | `/api/events/explore`               | Explore events             |
| GET    | `/api/events/search`                | Search events              |
| GET    | `/api/users/{id}/public`            | Public user profile        |
| GET    | `/api/public/interests`             | Available interests        |
| GET    | `/api/public/tags`                  | Available event tags       |
| GET    | `/api/public/stats`                 | Platform statistics        |
| POST   | `/api/password/forgot`              | Request password reset     |
| POST   | `/api/password/reset`               | Reset password with OTP    |

### Authenticated (JWT Required)

| Method | Endpoint                            | Description                |
|--------|-------------------------------------|----------------------------|
| POST   | `/api/events`                       | Create event (Organizer)   |
| PUT    | `/api/events/{id}`                  | Update event (Organizer)   |
| DELETE | `/api/events/{id}`                  | Cancel event (Organizer)   |
| POST   | `/api/enrollments`                  | Enroll in event            |
| POST   | `/api/payments/initiate`            | Initiate payment           |
| POST   | `/api/tickets/verify`               | Verify ticket (Organizer)  |
| POST   | `/api/groups`                       | Create group (Organizer)   |
| POST   | `/api/follow/{userId}`              | Follow a user              |
| POST   | `/api/feedback`                     | Submit event feedback      |
| GET    | `/api/notifications`                | Get notifications          |

### Admin

| Method | Endpoint                            | Description                     |
|--------|-------------------------------------|---------------------------------|
| GET    | `/api/admin/users`                  | Manage users                    |
| PUT    | `/api/admin/users/{id}/suspend`     | Suspend user                    |
| PUT    | `/api/admin/users/{id}/block`       | Block user                      |
| GET    | `/api/admin/reports`                | View event reports              |
| PUT    | `/api/admin/reports/{id}/resolve`   | Resolve report                  |
| GET    | `/api/admin/role-upgrades`          | View upgrade requests           |
| PUT    | `/api/admin/role-upgrades/{id}`     | Approve/reject upgrade          |
| POST   | `/api/admin/payments/{id}/refund`   | Process refund                  |

> Full interactive API documentation available at `/swagger-ui/` when the server is running.

---

## Database Schema (Entity Relationship)

```
┌──────────┐       ┌──────────┐       ┌────────────┐
│   User   │──M:M──│   Role   │──M:M──│ Permission │
└────┬─────┘       └──────────┘       └────────────┘
     │
     ├──1:M──▶ Event
     │           ├──1:M──▶ EventEnrollment (tickets)
     │           ├──1:M──▶ EventFeedback
     │           ├──1:M──▶ EventInterest
     │           ├──M:M──▶ EventTag (via EventTagMap)
     │           ├──1:M──▶ Payment
     │           └──1:M──▶ Report
     │
     ├──1:M──▶ Group
     │           ├──1:M──▶ GroupMembership
     │           ├──M:M──▶ Event (via GroupEventMap)
     │           └──M:M──▶ EventTag (via GroupTagMap)
     │
     ├──1:M──▶ Notification
     ├──1:M──▶ UserFollow (follower/following)
     ├──M:M──▶ InterestTag (via UserInterest)
     ├──1:M──▶ RoleUpgradeRequest
     └──1:M──▶ PasswordResetToken
```

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- PostgreSQL database

### Environment Variables

Create a `.env` file in the project root:

```env
DB_URL=jdbc:postgresql://localhost:5432/event_finder
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

JWT_SECRET=your-256-bit-secret-key

MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

KHALTI_SECRET_KEY=your-khalti-secret
ESEWA_SECRET_KEY=your-esewa-secret
ESEWA_PRODUCT_CODE=EPAYTEST

ADMIN_SECRET_KEY=your-admin-registration-secret

APP_BASE_URL=http://localhost:8080
APP_FRONTEND_URL=http://localhost:3000
```

### Run

```bash
# Clone the repository
git clone https://github.com/your-username/local-event-finder-backend.git

# Navigate to project directory
cd local-event-finder-backend

# Build and run
mvn spring-boot:run
```

The server starts at `http://localhost:8080`. API docs are available at `http://localhost:8080/swagger-ui/`.

---

## Project Structure

```
src/main/java/com/example/LocalEventFinder/
├── config/          # Security config, CORS, OpenAPI, ModelMapper
├── controller/      # REST controllers (Auth, Event, Admin, Payment, etc.)
├── dto/             # Request/Response DTOs
├── enums/           # RoleName, EventStatus, PaymentStatus, etc.
├── exception/       # Custom exceptions and global handler
├── model/           # JPA entities (User, Event, Role, Permission, etc.)
├── repository/      # Spring Data JPA repositories
├── security/        # JWT filter, CustomUserDetails, AuthEntryPoint
├── service/         # Business logic layer
└── util/            # FileUtil, EmailUtil, QRCodeGenerator, Haversine, etc.
```

---

## License

This project is for educational and portfolio demonstration purposes.
