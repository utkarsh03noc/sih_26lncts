# Integrated Dashboard for Monitoring and Tracking Institution-Related Complaint Cases Across AICTE

> **ACADEMIC & DEMO DISCLAIMER:**  
> This software is an academic demonstration and evaluation project. It is **NOT** officially affiliated with, endorsed by, or connected to the **All India Council for Technical Education (AICTE)**. All institutional names, logos, codes, and sample grievance records are used purely for educational and demonstration purposes.

---

## 📌 Project Overview

A robust, enterprise-grade full-stack grievance monitoring and tracking platform engineered with **Spring Boot 3** and **React 18 + Vite + Tailwind CSS**. The portal demonstrates how technical complaints across higher education institutions can be lodged, tracked, assigned, investigated, resolved, escalated, and analyzed through an apex monitoring dashboard.

### Key Capabilities
- **Role-Based Access Control (RBAC):** Distinct workflows for `ADMIN`, `INSTITUTION_OFFICER`, and `COMPLAINANT`.
- **Complete Grievance Lifecycle:** 8 status transitions (`SUBMITTED` ➔ `ASSIGNED` ➔ `UNDER_REVIEW` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED` / `REOPENED` / `ESCALATED`).
- **Dynamic SLA & Escalation Engine:** Automatically calculates SLA due dates by priority (`CRITICAL`: 3 days, `HIGH`: 7 days, `MEDIUM`: 15 days, `LOW`: 30 days) and flags overdue violations.
- **Audit Trail & Timeline:** Immutable chronological event logs tracking every state transition, investigator remarks, and system action.
- **Citizen Feedback & Reopening:** Resolution satisfaction ratings (1–5 stars) and case reopening mechanism for dissatisfied complainants.
- **Interactive Visual Intelligence:** Charts for grievance categorization, monthly velocity trends, status distribution, and institutional compliance rankings powered by **Recharts**.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Frontend** | React 18, Vite, Tailwind CSS, React Router v6, Axios, Recharts, Lucide React |
| **Backend** | Java 17+, Spring Boot 3.3.4, Spring Security, Spring Data JPA, JWT (jjwt 0.12.5), Bean Validation |
| **Database** | MySQL 8.0+ (with zero-setup H2 in-memory profile available for offline viva evaluation) |
| **Build & Tools** | Maven, npm / vite |

---

## 📂 Project Folder Structure

```text
sih/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/aicte/grievance/
│   │   │   │   ├── config/
│   │   │   │   │   ├── DataInitializer.java        # 50+ Complaints, 12 Institutions, 9 Users seeder
│   │   │   │   │   └── WebMvcConfig.java           # CORS & web MVC configuration
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java         # /api/auth (login, register, me)
│   │   │   │   │   ├── ComplaintController.java    # /api/complaints (CRUD, status, assign, reopen)
│   │   │   │   │   ├── DashboardController.java    # /api/dashboard (stats, institution-stats)
│   │   │   │   │   ├── InstitutionController.java  # /api/institutions (directory & search)
│   │   │   │   │   ├── UserController.java         # /api/users (officers directory)
│   │   │   │   │   ├── AuditLogController.java     # /api/audit-logs (security logs)
│   │   │   │   │   └── ReportController.java       # /api/reports (audit report summary)
│   │   │   │   ├── dto/                            # Data Transfer Objects & API envelopes
│   │   │   │   ├── entity/                         # 8 JPA Entities + 4 Enums
│   │   │   │   │   ├── User.java
│   │   │   │   │   ├── Institution.java
│   │   │   │   │   ├── Complaint.java
│   │   │   │   │   ├── ComplaintUpdate.java
│   │   │   │   │   ├── Attachment.java
│   │   │   │   │   ├── Notification.java
│   │   │   │   │   ├── Feedback.java
│   │   │   │   │   ├── AuditLog.java
│   │   │   │   │   ├── Role.java
│   │   │   │   │   ├── ComplaintStatus.java
│   │   │   │   │   ├── ComplaintCategory.java
│   │   │   │   │   └── Priority.java
│   │   │   │   ├── exception/                      # Global exception handler & custom exceptions
│   │   │   │   ├── repository/                     # Spring Data JPA repositories
│   │   │   │   ├── security/                       # JWT provider, filter, UserDetails, SecurityConfig
│   │   │   │   ├── service/                        # Business logic services
│   │   │   │   └── GrievanceApplication.java       # Main entry point
│   │   │   └── resources/
│   │   │       ├── application.properties          # MySQL configuration
│   │   │       └── application-h2.properties       # H2 in-memory offline testing profile
│   │   └── test/
│   ├── pom.xml                                     # Maven build file
│   └── mvnw.cmd                                    # Maven wrapper script
│
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── common/                             # Badges, Modal, Navbar, Sidebar, Pagination, Toast
│   │   │   ├── complaints/                         # Filters, Timeline, Status modal, Assign modal
│   │   │   └── dashboard/                          # KPI Grid, Recharts components, Performance table
│   │   ├── context/
│   │   │   ├── AuthContext.jsx                     # Authentication & session state
│   │   │   └── ToastContext.jsx                    # Toast notification manager
│   │   ├── hooks/                                  # useAuth, useToast
│   │   ├── layouts/
│   │   │   ├── DashboardLayout.jsx                 # Sidebar + Navbar + Disclaimer layout
│   │   │   └── AuthLayout.jsx                      # Centered login/registration layout
│   │   ├── pages/
│   │   │   ├── admin/                              # 9 Admin views (Dashboard, Complaints, SLA, etc.)
│   │   │   ├── officer/                            # 4 Officer views (Desk, Assigned, Detail, Stats)
│   │   │   ├── complainant/                        # 6 Complainant views (Dashboard, Register, Track)
│   │   │   ├── auth/                               # Login & Register with 1-click Demo Fillers
│   │   │   └── shared/                             # Unauthorized & 404 pages
│   │   ├── services/                               # Axios API service clients
│   │   ├── utils/                                  # Status, priority constants & date formatters
│   │   ├── App.jsx                                 # React Router v6 route configuration
│   │   ├── index.css                               # Tailwind CSS base styles
│   │   └── main.jsx                                # React root mounting
│   ├── index.html
│   ├── package.json
│   ├── tailwind.config.js
│   ├── postcss.config.js
│   └── vite.config.js
└── README.md
```

---

## 🔑 Demo Login Credentials

The application automatically seeds a comprehensive set of test accounts with realistic mock data on initial startup:

| Role | Username | Password | Email / Affiliation |
|---|---|---|---|
| **AICTE Central Admin** | `admin` | `Password@123` | `admin@aicte-demo.gov.in` |
| **Nodal Officer (IIT Delhi)** | `officer_iit` | `Password@123` | `officer.delhi@institution.edu` |
| **Nodal Officer (BITS Pilani)** | `officer_bits` | `Password@123` | `officer.bits@institution.edu` |
| **Nodal Officer (VJTI Mumbai)** | `officer_vjti` | `Password@123` | `officer.vjti@institution.edu` |
| **Student (Complainant)** | `student_rohit` | `Password@123` | `rohit.sharma@gmail.com` |
| **Student (Complainant)** | `student_ananya` | `Password@123` | `ananya.verma@gmail.com` |
| **Student (Complainant)** | `student_priya` | `Password@123` | `priya.nair@gmail.com` |
| **Parent (Complainant)** | `parent_rajesh` | `Password@123` | `rajesh.gupta@yahoo.com` |
| **Faculty (Complainant)** | `faculty_suresh` | `Password@123` | `suresh.kumar@gmail.com` |

> 💡 **Tip:** The Login Page includes **Quick 1-Click Demo Login** buttons at the top of the form to auto-fill credentials for each role instantly.

---

## 🗄️ Database Setup Instructions

### Option A: MySQL Database (Recommended Production / Demo Setup)

1. Open your MySQL client (e.g. MySQL Workbench, phpMyAdmin, or command-line client).
2. Create the database (optional, as Spring Boot connects with `createDatabaseIfNotExist=true`):
   ```sql
   CREATE DATABASE IF NOT EXISTS aicte_grievance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Verify your MySQL username and password in `backend/src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/aicte_grievance_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=root
   ```
4. Spring Data JPA Hibernate will automatically create all tables (`users`, `institutions`, `complaints`, `complaint_updates`, `feedbacks`, `audit_logs`, `attachments`, `notifications`) and seed 50+ complaint cases and 12 technical institutions on first boot.

### Option B: H2 In-Memory Database (Zero-Configuration Offline Evaluation)

If running in a viva presentation or environment where MySQL server is not running:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```
This loads all tables and seed data completely in-memory with zero local configuration!

---

## 🚀 Exact Commands to Start the Application

### 1. Start the Spring Boot Backend

Open a terminal in the `backend/` folder:

```bash
cd backend

# On Windows:
mvn clean spring-boot:run

# Or with Maven Wrapper:
.\mvnw.cmd clean spring-boot:run

# Or with H2 offline profile:
mvn clean spring-boot:run -Dspring-boot.run.profiles=h2
```

The backend server starts on: **`http://localhost:8080`**

### 2. Start the React Frontend

Open a new terminal in the `frontend/` folder:

```bash
cd frontend

# Install node dependencies (first time):
npm install

# Start Vite development server:
npm run dev
```

The frontend application opens on: **`http://localhost:5173`**

---

## 📡 Implemented REST APIs

### 1. Authentication (`/api/auth`)
- `POST /api/auth/login` — Authenticate user and issue JWT bearer token.
- `POST /api/auth/register` — Register a new complainant account.
- `GET /api/auth/me` — Retrieve profile of currently authenticated user.

### 2. Grievance Cases (`/api/complaints`)
- `GET /api/complaints` — Paginated and filtered complaint catalog (supports `status`, `category`, `priority`, `institutionId`, `search`, `sortBy`, `sortDir`, `page`, `size`).
- `POST /api/complaints` — Register a new grievance dossier.
- `GET /api/complaints/{id}` — Retrieve full complaint details, institutional affiliation, and assigned officer.
- `GET /api/complaints/public/track/{complaintNumber}` — Public case tracking endpoint without login requirement.
- `PATCH /api/complaints/{id}/status` — Update case workflow status and record official inquiry remarks.
- `POST /api/complaints/{id}/assign` — Assign case to a designated institutional nodal officer.
- `POST /api/complaints/{id}/reopen` — Reopen an unsatisfactory resolved case for fresh examination.
- `GET /api/complaints/{id}/updates` — Retrieve complete chronological timeline of actions and updates.
- `POST /api/complaints/{id}/feedback` — Submit citizen satisfaction rating (1–5 stars) and comments.
- `GET /api/complaints/{id}/feedback` — Retrieve feedback for a resolved case.
- `GET /api/complaints/escalations` — Retrieve all overdue and critically escalated complaint cases.

### 3. Institutions (`/api/institutions`)
- `GET /api/institutions` — Paginated searchable list of technical institutions.
- `GET /api/institutions/active` — Active institution list for dropdown selection.
- `GET /api/institutions/{id}` — Individual institution dossier with complaint statistics.
- `POST /api/institutions` — Register a new technical institution (Admin only).
- `PUT /api/institutions/{id}` — Update institution information (Admin only).

### 4. Dashboards & Analytics (`/api/dashboard`)
- `GET /api/dashboard/stats` — Overall apex statistics (Total, Pending, Resolved, Closed, Escalated, Overdue, Category breakdown, Status breakdown, Monthly trends, Institution performance league).
- `GET /api/dashboard/institution-stats` — Filtered statistics tailored to a specific institution.
- `GET /api/dashboard/my-stats` — Personal grievance statistics for the logged-in complainant.

### 5. Administration & Auditing (`/api/audit-logs`, `/api/reports`, `/api/users`)
- `GET /api/audit-logs` — Paginated security audit trail.
- `GET /api/audit-logs/entity/{name}/{id}` — Audit trail for a specific entity.
- `GET /api/reports/summary` — Executive audit report compilation.
- `GET /api/users/officers` — Directory of nodal grievance officers.
- `GET /api/users/officers/institution/{institutionId}` — Nodal officers for a specific campus.

---

## 🔮 Roadmap / Modules for Next Phase

The following advanced modules are earmarked for subsequent development iterations:
1. **Automated AI Grievance Categorization & Sentiment Analysis:** Natural Language Processing (NLP) pipeline to automatically detect urgency, categorize text, and identify duplicate grievances.
2. **Automated SMS & WhatsApp Webhook Notifications:** Real-time mobile alerts to complainants upon each status transition.
3. **Multi-File Evidence Attachment Vault:** S3/MinIO cloud storage integration for PDF transcripts, fee receipts, and photo evidence.
4. **GIS Geospatial Grievance Mapping:** Heatmap visualization of institutional grievances across Indian states and union territories.
5. **DigiLocker Integration:** Identity and certificate verification using National Academic Depository (NAD) / DigiLocker APIs.
