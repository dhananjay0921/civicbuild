# CIVICBUILD: MUNICIPAL MULTI-DEPARTMENT BUILDING PERMIT AUTOMATION PORTAL
## An End-to-End Workflow-Orchestrated Enterprise System Using Camunda 8 (Zeebe), Spring Boot 3, React 18, and PostgreSQL 16

---

**Academic Project Capstone Technical Report**  
**Course:** Enterprise Software Architecture & Business Process Engineering  
**Department:** Department of Computer Science & Engineering  
**Project Lead & Architect:** Dhananjay Pattnaik  
**Academic Year:** 2025–2026  
**Document Version:** 1.0.0-RELEASE  
**Publication Date:** November 2026  

---

## Abstract

Municipal building permit approval workflows have historically been constrained by paper-bound bureaucracy, opaque decision-making, and disjointed departmental silos. In a typical municipality, an applicant must physically ferry engineering drawings and ownership titles through Fire, Water, Town Planning (Zoning), and Environmental protection departments. Delays of three to nine months are commonplace, compounded by lost records, unmonitored review periods, and vulnerability to administrative corruption.

This capstone project presents **CivicBuild**, a production-style, event-driven enterprise application that completely automates and digitizes municipal building permit lifecycles. CivicBuild unites an industrial workflow orchestration engine (**Camunda 8 / Zeebe BPMN 2.0**) with an enterprise backend (**Java 17 / Spring Boot 3.2.5**), a responsive single-page application (**React 18 / Vite / Tailwind CSS**), and an ACID-compliant relational store (**PostgreSQL 16**).

The system enforces strict BPMN 2.0 constructs: a sequential Multi-Instance document verification checklist with Error Boundary Events (`MISSING_CRITICAL_DOCUMENT`), a parallel Multi-Instance departmental review across four autonomous statutory agencies, an embedded Sub-Process (`BlueprintRevisionProcess`) with configurable cycle thresholds, a Timer Boundary Event (`PT48H`) for SLA timeout escalations, and Call Activities for reusable fee calculation and notification dispatch. The system features a custom task management inbox, a live 7-stage visual workflow tracker, an OpenPDF permit certificate generator with 2D QR validation, and a comprehensive suite of 37 automated tests verifying 18 critical scenarios. This report details the architectural blueprint, data contracts, BPMN execution semantics, containerization topology, and testing verification.

**Keywords:** Camunda 8, Zeebe Engine, BPMN 2.0, Spring Boot 3, React 18, PostgreSQL, Workflow Automation, Municipal E-Governance, Enterprise Software Architecture.

---

## Table of Contents

1. [Executive Summary & Problem Domain](#chapter-1-executive-summary--problem-domain)
   - 1.1 The Municipal Dilemma
   - 1.2 Proposed System Objectives
   - 1.3 Scope of the Project
   - 1.4 User Personas & Responsibility Matrix
2. [BPMN 2.0 Process Architecture & Workflow Orchestration](#chapter-2-bpmn-20-process-architecture--workflow-orchestration)
   - 2.1 Complete Business Process Walkthrough
   - 2.2 Camunda 8 & Zeebe Distributed Architecture
   - 2.3 Detailed BPMN Construct Analysis
   - 2.4 FEEL Expressions Dictionary & Decision Semantics
   - 2.5 Strongly-Typed Zeebe Job Workers
3. [System Architecture & Technical Implementation](#chapter-3-system-architecture--technical-implementation)
   - 3.1 Four-Tier Architecture Topology
   - 3.2 Spring Boot Backend Architecture
   - 3.3 React 18 SPA Frontend Architecture
   - 3.4 OpenPDF Digital Permit Certificate Generator
4. [Data Contracts, Persistence & API Dictionary](#chapter-4-data-contracts-persistence--api-dictionary)
   - 4.1 Relational Database Schema & Entity Relationships
   - 4.2 Comprehensive REST API Dictionary
   - 4.3 Secure Document Storage Strategy
5. [Containerization, Deployment & DevOps](#chapter-5-containerization-deployment--devops)
   - 5.1 Multi-Stage Dockerfile Engineering
   - 5.2 Docker Compose Multi-Container Orchestration
   - 5.3 Configuration Management & Operational Hardening
6. [Quality Assurance, Verification & Automated Testing](#chapter-6-quality-assurance-verification--automated-testing)
   - 6.1 Testing Methodology & Philosophy
   - 6.2 Test Execution Evidence (18 Mandatory Scenarios)
   - 6.3 Fault Injection & Edge-Case Resilience
7. [Project Evaluation, Academic Contributions & Conclusion](#chapter-7-project-evaluation-academic-contributions--conclusion)
   - 7.1 Requirements Traceability Matrix
   - 7.2 Key Engineering Challenges & Solutions
   - 7.3 Future Extensions
   - 7.4 Concluding Remarks

---

# Chapter 1: Executive Summary & Problem Domain

## 1.1 The Municipal Dilemma
Urban municipal corporations face massive demand for residential, commercial, industrial, and institutional infrastructure. However, statutory construction permit approvals remain notoriously slow, error-prone, and frustrating.

```
+-------------------------------------------------------------------------------+
|                        TRADITIONAL PERMIT BOTTLENECK                          |
|                                                                               |
|  [Applicant]                                                                  |
|       │                                                                       |
|       ▼                                                                       |
|  [Physical Filing] ──> [Fire Dept] ──> [Water Dept] ──> [Zoning] ──> [Env]    |
|                         (Serial Desks: 3 to 9 Months Total)                   |
|                                                                               |
|  Pain Points:                                                                 |
|  • Repeated document submissions and missing physical files                  |
|  • No concurrent reviews (sequential bottlenecks)                             |
|  • Zero visibility for citizens on where the file is held                     |
|  • No automated escalation when officers exceed statutory review periods     |
|  • Vulnerability to arbitrary rejections and bribery                          |
+-------------------------------------------------------------------------------+
```

The fundamental flaws in legacy municipal administration include:
1. **Serial Processing Bottlenecks:** Applications move linearly from department to department. A 10-day delay in Fire safety stalls Water and Zoning clearances, even though the engineering checks are orthogonal.
2. **Document Inconsistencies:** Documents (such as title deeds or structural stability certificates) verified by one desk are re-scrutinized by another without standard digital records.
3. **Information Asymmetry:** Applicants have no visibility into the review state, prompting repeated physical inquiries at municipal offices.
4. **Lack of SLA Accountability:** Officers face no automated monitoring or escalation mechanisms when files linger past statutory deadlines.

## 1.2 Proposed System Objectives
CivicBuild replaces manual file routing with a unified digital automation platform:
- **Centralized Citizen Portal:** Enables applicants to submit multi-step applications, upload CAD blueprints and legal proofs, view real-time stage progression, respond to revision requests, and execute online statutory fee payments.
- **Event-Driven Workflow Orchestration:** Leverages Camunda 8 (Zeebe) to manage the state machine, decouple business rules from backend code, and execute parallel departmental reviews.
- **Parallel Review Execution:** Simultaneously dispatches independent review instances to Fire, Water, Town Planning (Zoning), and Environmental departments upon initial document clearance.
- **Standardized Revision & SLA Loops:** Encapsulates revision iterations inside a sub-process with an enforced limit (maximum 3 cycles) and attaches Timer Boundary Events (`PT48H`) to trigger managerial alerts on overdue files.
- **Tamper-Proof Permit Generation:** Dynamically generates an official PDF permit complete with municipal seal, QR verification code, and 3-year statutory validity upon confirmed payment.

## 1.3 Scope of the Project
The application represents a complete, integrated full-stack system covering:
1. Four role personas: `APPLICANT`, `DEPARTMENT OFFICER`, `ADMINISTRATOR`, and `SUPER ADMIN`.
2. Core building categories: Residential, Commercial, Industrial, and Institutional.
3. Complete BPMN 2.0 executable XML models with Diagram Interchange.
4. Strongly typed Zeebe job workers interacting with transactional Spring Boot services.
5. Production-ready containerization via Docker Compose.

## 1.4 User Personas & Responsibility Matrix

| Feature / Responsibility | Applicant (`ROLE_APPLICANT`) | Department Officer (`ROLE_OFFICER`) | Administrator (`ROLE_ADMIN`) | Super Admin (`ROLE_SUPER_ADMIN`) |
| :--- | :---: | :---: | :---: | :---: |
| Register & Manage Profile | Yes | Admin Only | Admin Only | Full |
| Submit Building Application Drafts | Yes | No | No | No |
| Upload & Replace Statutory Files | Yes | No | No | No |
| View Real-Time Workflow Tracker | Yes | Yes | Yes | Yes |
| Department Review Task Inbox | No | Yes (Dept Specific) | Yes (All Depts) | Yes (All Depts) |
| Issue Approval / Rejection Decisions | No | Yes | Yes | Yes |
| Request Blueprint Revisions | No | Yes | No | No |
| Respond to Revisions with New Files | Yes | No | No | No |
| Statutory Fee Settlement | Yes | No | No | No |
| Download Official Digital Permit | Yes | Yes | Yes | Yes |
| Monitor Overdue Reviews & SLAs | No | No | Yes | Yes |
| Audit Trail & Transition Log Access | Personal Apps | Department Apps | Global System | Global System |
| User & Role Administration | No | No | Officers Only | Global Users |
| System Configuration & Fee Rules | No | No | View Only | Full |

---

# Chapter 2: BPMN 2.0 Process Architecture & Workflow Orchestration

## 2.1 Complete Business Process Walkthrough
The CivicBuild business process spans eleven distinct operational stages:

```mermaid
sequenceDiagram
    autonumber
    actor App as Applicant
    participant Portal as React Portal
    participant API as Spring Boot Backend
    participant Zeebe as Camunda 8 Zeebe
    participant Depts as 4 Municipal Depts
    participant Pay as Payment Gateway
    participant PDF as Permit Engine

    App->>Portal: Submit Application & Upload Documents
    Portal->>API: POST /api/applications/{id}/submit
    API->>Zeebe: Start BuildingPermitApprovalProcess
    Zeebe->>API: Worker: validate-application
    Zeebe->>API: Worker: verify-land-registry (Title Check)
    Zeebe->>Zeebe: Call Activity: DocumentVerificationProcess (Sequential MI)
    Zeebe->>Depts: Spawn 4 Parallel Reviews (Fire, Water, Zoning, Env)
    alt Revision Requested by Department
        Depts->>Zeebe: Decision: REVISION_REQUESTED
        Zeebe->>App: Notification: BlueprintRevisionProcess
        App->>API: Resubmit Corrected Blueprints
        API->>Depts: Re-review in progress
    else Permanent Rejection
        Depts->>Zeebe: Decision: REJECTED
        Zeebe->>API: Worker: update-application-status (REJECTED)
        Zeebe-->>App: Notification: Formal Rejection
    end
    Depts->>Zeebe: All 4 Departments: APPROVED
    Zeebe->>Zeebe: Call Activity: MunicipalFeeCalculationProcess
    Zeebe->>Pay: Worker: create-payment-request
    App->>Pay: Settle Statutory Fee (UPI/Card/NetBanking)
    Pay->>Zeebe: Worker: confirm-payment (SUCCESS)
    Zeebe->>PDF: Worker: generate-permit (OpenPDF)
    Zeebe->>App: Notification: Permit Approved & PDF Ready
```

1. **Application Submission:** Citizen registers, enters property parcel attributes, enters building dimensions, uploads statutory PDFs, and submits the draft.
2. **Application Validation:** `validate-application` verifies plot area, floor count, and built-up area constraints.
3. **Land Registry Title Clearance:** `verify-land-registry` performs title clearance against municipal cadastral databases.
4. **Document Verification (Sequential Multi-Instance):** Invokes `DocumentVerificationProcess` as a Call Activity. The process evaluates submitted documents sequentially. If critical documents (`PROPERTY_OWNERSHIP` or `BUILDING_BLUEPRINT`) are missing, a BPMN Error Boundary Event (`MISSING_CRITICAL_DOCUMENT`) intercepts the execution.
5. **Departmental Review (Parallel Multi-Instance):** Dispatches concurrent review instances to Fire, Water, Zoning, and Environmental departments.
6. **Revision Handling (Embedded Sub-Process):** If an officer requests revisions, the application enters `BlueprintRevisionProcess`. The applicant is notified and allowed to re-upload documents. A FEEL gateway evaluates `revisionCount < maxRevisions`; if exceeded, the application is automatically rejected.
7. **Department Review SLA Escalation (Timer Boundary Event):** A non-interrupting Timer Boundary Event (`PT48H`) monitors review duration and escalates unattended applications to the Municipal Administrator.
8. **Convergence Gateway:** An Exclusive Gateway evaluates FEEL expressions across all four review decisions. The process only advances when all four departments grant approval.
9. **Municipal Fee Calculation (Call Activity):** Reusable `MunicipalFeeCalculationProcess` computes statutory fees based on building type, built-up area, and floor count.
10. **Payment Processing:** `create-payment-request` generates an electronic invoice. The applicant settles the fee via mock gateway; `confirm-payment` verifies payment success.
11. **Digital Permit Generation:** `generate-permit` compiles an official PDF permit certificate with 2D QR code and municipal seal, transitioning status to `APPROVED`.

## 2.2 Camunda 8 & Zeebe Distributed Architecture
Camunda 8 decouples business workflow orchestration from execution workers through the **Zeebe engine**:
- **Log-Centric Storage:** Zeebe operates on an append-only, distributed Raft consensus log, delivering high-throughput and horizontal scalability.
- **gRPC Protocol:** Job workers communicate with the Zeebe gateway over HTTP/2 gRPC channels, eliminating polling overhead and enabling reactive event streaming.
- **Variable Scoping:** Variables injected at process start (`applicationId`, `buildingType`, `plotArea`) are cascaded into Call Activities via explicit input/output variable mappings.

## 2.3 Detailed BPMN Construct Analysis

### A. Events
- **Start Event:** `Event_StartApplication` initiates the permit lifecycle upon REST trigger.
- **End Events:** Multiple semantic terminations:
  - `Event_EndApproved`: Normal completion following permit issuance.
  - `Event_EndRejected`: Terminate End Event triggered when a department permanently rejects or the revision limit is breached.
  - `Event_EndMissingDocs`: Terminate End Event triggered by unrecoverable document errors.
- **Error Boundary Event:** Attached to document verification tasks. Intercepts `MISSING_CRITICAL_DOCUMENT` business errors and routes execution to applicant notification and graceful rejection without crashing the engine.
- **Timer Boundary Event:** `Event_TimerReviewOverdue` configured with ISO-8601 duration `duration("PT48H")` on departmental reviews. When 48 hours lapse without officer action, Zeebe spawns an SLA escalation job to the municipal administrator.

### B. Multi-Instance Activities
1. **Parallel Multi-Instance (Departmental Reviews):**
   - **Collection:** `departmentList` (`["FIRE", "WATER", "ZONING", "ENVIRONMENTAL"]`)
   - **Element Variable:** `currentDepartment`
   - **Execution Mode:** Parallel (`isSequential="false"`)
   - **Behavior:** Zeebe spawns four concurrent user review tasks, collecting review decisions into a composite array for gateway evaluation.
2. **Sequential Multi-Instance (Document Verification Checklist):**
   - **Collection:** `documentChecklist`
   - **Element Variable:** `currentDocument`
   - **Execution Mode:** Sequential (`isSequential="true"`)
   - **Behavior:** Inspects mandatory statutory documents one by one in deterministic sequence.

### C. Gateways and Convergence
- **Gateway_AllReviewsApproved (Exclusive Gateway):**
  - Path 1 (Proceed to Fee Calculation): `allReviewStatus = "APPROVED"`
  - Path 2 (Reject Application): `allReviewStatus = "REJECTED"`
  - Path 3 (Revision Handling): `allReviewStatus = "REVISION_REQUESTED"`

### D. Embedded Sub-Process (`BlueprintRevisionProcess`)
Encapsulates iterative citizen-officer interactions:
- Request creation and comment recording.
- Citizen notification and upload interface activation.
- Increment of `revisionCount = revisionCount + 1`.
- FEEL Gateway check: `revisionCount < maxRevisions` vs `revisionCount >= maxRevisions`.

### E. Call Activities
- `CallActivity_DocumentVerification` calls `DocumentVerificationProcess`.
- `CallActivity_MunicipalFeeCalculation` calls `MunicipalFeeCalculationProcess`.
- `CallActivity_PermitNotification` calls `PermitNotificationProcess`.

## 2.4 FEEL Expressions Dictionary & Decision Semantics

| BPMN Element | Expression Type | FEEL Expression | Evaluation Semantics |
| :--- | :--- | :--- | :--- |
| Gateway_AllApproved | Sequence Flow | `allReviewStatus = "APPROVED"` | Evaluates true when all 4 parallel review outputs are `APPROVED`. |
| Gateway_AnyRejected | Sequence Flow | `allReviewStatus = "REJECTED"` | Evaluates true when any single department enters `REJECTED`. |
| Gateway_RevisionReq | Sequence Flow | `allReviewStatus = "REVISION_REQUESTED"` | Routes into embedded revision sub-process. |
| Gateway_CheckLimit | Sequence Flow | `revisionCount < maxRevisions` | Permits applicant to resubmit corrected drawings (max 3 cycles). |
| Gateway_LimitExceeded| Sequence Flow | `revisionCount >= maxRevisions`| Forfeits application and routes to permanent rejection path. |
| Boundary_DocError | Error Definition | `errorCode = "MISSING_CRITICAL_DOCUMENT"`| Catches missing statutory deed or architectural blueprint. |
| Boundary_OverdueTimer| Timer Definition | `duration("PT48H")` | Triggers SLA timeout escalation after 48 elapsed hours. |
| Gateway_PaymentStatus| Sequence Flow | `paymentStatus = "SUCCESS"` | Advances workflow to digital permit generation. |

## 2.5 Strongly-Typed Zeebe Job Workers

CivicBuild implements **10 production-style Zeebe Job Workers** in `com.civicbuild.worker.CivicBuildJobWorkers`:

1. `validate-application`: Validates boundary dimensions, floor counts, and application ownership.
2. `verify-land-registry`: Verifies survey numbers against municipal cadastral land registry.
3. `verify-document`: Evaluates document checklist; throws `ZeebeBpmnError("MISSING_CRITICAL_DOCUMENT")` if critical files are omitted.
4. `calculate-municipal-fee`: Evaluates statutory fee rates based on building type, area, and floor counts.
5. `create-payment-request`: Issues an electronic payment invoice with unique transaction reference.
6. `confirm-payment`: Verifies payment settlement and records gateway transaction response.
7. `generate-permit`: Invokes OpenPDF engine to generate official digitally stamped permit certificate.
8. `send-notification`: Dispatches email and in-portal alerts across seven workflow lifecycle milestones.
9. `update-application-status`: Synchronizes application status enum across JPA and Camunda engine.
10. `handle-review-timeout`: Dispatches administrative escalation upon SLA timer breach.

---

# Chapter 3: System Architecture & Technical Implementation

## 3.1 Four-Tier Architecture Topology

```
+-------------------------------------------------------------------------------+
|                             FOUR-TIER TOPOLOGY                                |
|                                                                               |
|  [TIER 1: CLIENT PRESENTATION]                                                |
|  React 18 SPA | Vite 5 | Tailwind CSS | Lucide Icons | Recharts                |
|       │                                                                       |
|       ▼ HTTP / REST (JWT in Authorization Bearer)                             |
|  [TIER 2: API GATEWAY & REVERSE PROXY]                                        |
|  Nginx 1.25 Alpine (Gzip, SSL/TLS termination, Security Headers, SPA fallback)|
|       │                                                                       |
|       ▼ Proxy Pass /api                                                       |
|  [TIER 3: BUSINESS APPLICATION SERVER]                                        |
|  Spring Boot 3.2.5 | Spring Security | Spring Data JPA | 10 Zeebe Workers     |
|       │                                      │                                |
|       ▼ gRPC (Port 26500)                    ▼ JDBC (Port 5432)               |
|  [CAMUNDA 8 ENGINE]                 [TIER 4: RELATIONAL DATABASE]             |
|  Zeebe Standalone Broker 8.5.0      PostgreSQL 16 Enterprise RDBMS            |
+-------------------------------------------------------------------------------+
```

## 3.2 Spring Boot Backend Architecture
The backend follows strict domain-driven layered principles:
- `com.civicbuild.config`: Security filters, CORS rules, seed data initializer, and fallback dynamic proxy for Zeebe client.
- `com.civicbuild.controller`: 10 REST controllers exposing JSON endpoints with bean validation.
- `com.civicbuild.dto`: Strongly typed Request and Response models avoiding entity leakage.
- `com.civicbuild.entity`: 13 JPA entities mapped to PostgreSQL relational tables.
- `com.civicbuild.enums`: 10 type-safe enums (`ApplicationStatus`, `BuildingType`, `DocumentType`, etc.).
- `com.civicbuild.exception`: Centralized exception hierarchy (`GlobalExceptionHandler`) returning standardized RFC 7807 problem details.
- `com.civicbuild.repository`: 13 Spring Data JPA repositories with custom derived and JPQL queries.
- `com.civicbuild.service`: Transactional business logic orchestrating entities and workflow states.
- `com.civicbuild.worker`: 10 `@JobWorker` annotated handlers consuming Zeebe workflow tasks.

## 3.3 React 18 SPA Frontend Architecture
The frontend leverages modern React 18 patterns:
- **Client-Side Routing:** Protected route wrappers (`ProtectedRoute`) enforcing role-based authorization for `APPLICANT`, `OFFICER`, `ADMIN`, and `SUPER_ADMIN`.
- **Global Authentication Context (`AuthContext`):** Manages user session state, JWT tokens, automatic refresh, and logout.
- **Multi-Step Application Wizard:** 4-step interactive form with client-side validation, required indicators, file type/size verification, and draft save capabilities.
- **WorkflowTracker Component:** Interactive visual stepper rendering all 7 high-level stages and the 4-department parallel review matrix with live color badges.
- **Department Tasklist:** Tailored review inbox allowing officers to inspect CAD/PDF attachments and submit structured decisions with remarks.
- **Recharts Analytics Dashboard:** Visualizes application volume by zone, building type distributions, and review turnaround times.

## 3.4 OpenPDF Digital Permit Certificate Generator
Upon payment confirmation, `PermitServiceImpl` compiles an official PDF document:
- Header with municipal heraldry and title: *"MUNICIPAL CORPORATION BUILDING PERMIT CERTIFICATE"*.
- Master Permit Number (e.g., `CB-PERMIT-2026-96952`).
- 2D QR Code containing a verification URL and cryptographic checksum.
- Tabular breakdown of parcel details, zoning classification, approved floor count, and built-up area.
- Statutory conditions of approval (safety compliance, setback preservation, 3-year expiration).
- Official digital signature block of the Municipal Commissioner.

---

# Chapter 4: Data Contracts, Persistence & API Dictionary

## 4.1 Relational Database Schema & Entity Relationships

The relational schema comprises 13 tables:

```
+-----------------------------------------------------------------------------------------+
|                                    DATABASE SCHEMA                                      |
+-----------------------------+-----------------------------+-----------------------------+
| roles                       | users                       | properties                  |
| - id (PK)                   | - id (PK)                   | - id (PK)                   |
| - name (VARCHAR)            | - username (VARCHAR, UQ)    | - address (VARCHAR)         |
| - description (VARCHAR)     | - email (VARCHAR, UQ)       | - survey_number (VARCHAR)   |
|                             | - password (VARCHAR)        | - ward_number (VARCHAR)     |
|                             | - role_id (FK -> roles)     | - zone (VARCHAR)            |
|                             | - department_code (VARCHAR) | - plot_area (NUMERIC)       |
|                             |                             | - ownership_type (VARCHAR)  |
+-----------------------------+-----------------------------+-----------------------------+
| applications                | documents                   | department_reviews          |
| - id (PK)                   | - id (PK)                   | - id (PK)                   |
| - application_number (UQ)   | - application_id (FK)       | - application_id (FK)       |
| - applicant_id (FK -> users)| - document_type (VARCHAR)   | - department_code (VARCHAR) |
| - property_id (FK)          | - file_name (VARCHAR)       | - status (VARCHAR)          |
| - building_type (VARCHAR)   | - file_path (VARCHAR)       | - remarks (TEXT)            |
| - number_of_floors (INT)    | - verification_status (VR)  | - due_date (TIMESTAMP)      |
| - built_up_area (NUMERIC)   | - uploaded_at (TIMESTAMP)   | - reviewed_at (TIMESTAMP)   |
| - status (VARCHAR)          |                             |                             |
| - calculated_fee (NUMERIC)  |                             |                             |
| - process_instance_key (VR) |                             |                             |
+-----------------------------+-----------------------------+-----------------------------+
| revision_requests           | payments                    | permits                     |
| - id (PK)                   | - id (PK)                   | - id (PK)                   |
| - application_id (FK)       | - application_id (FK)       | - application_id (FK, UQ)   |
| - department_code (VARCHAR) | - amount (NUMERIC)          | - permit_number (UQ)        |
| - cycle_number (INT)        | - transaction_ref (UQ)      | - issue_date (TIMESTAMP)    |
| - officer_remarks (TEXT)    | - status (VARCHAR)          | - expiry_date (TIMESTAMP)   |
| - status (VARCHAR)          | - payment_method (VARCHAR)  | - qr_code_data (TEXT)       |
| - requested_at (TIMESTAMP)  | - gateway_response (TEXT)   | - pdf_path (VARCHAR)        |
+-----------------------------+-----------------------------+-----------------------------+
| workflow_history            | notifications               | audit_logs                  |
| - id (PK)                   | - id (PK)                   | - id (PK)                   |
| - application_id (FK)       | - recipient_email (VARCHAR) | - user_id (FK -> users)     |
| - stage (VARCHAR)           | - title (VARCHAR)           | - action (VARCHAR)          |
| - action (VARCHAR)          | - message (TEXT)            | - entity_type (VARCHAR)     |
| - actor (VARCHAR)           | - type (VARCHAR)            | - ip_address (VARCHAR)      |
| - timestamp (TIMESTAMP)     | - is_read (BOOLEAN)         | - timestamp (TIMESTAMP)     |
+-----------------------------+-----------------------------+-----------------------------+
```

## 4.2 Comprehensive REST API Dictionary

### Authentication & User Management
- `POST /api/auth/login`: Accepts `{"username", "password"}`. Returns JWT token, user roles, department affiliation.
- `POST /api/auth/register`: Accepts registration payload. Creates citizen user account.
- `GET /api/auth/me`: Returns authenticated profile and permissions.

### Application Lifecycle
- `POST /api/applications/draft`: Creates initial application draft with property and construction attributes.
- `PUT /api/applications/{id}/draft`: Updates draft parameters prior to formal submission.
- `POST /api/applications/{id}/documents`: Multipart endpoint (`multipart/form-data`) uploading document binaries with validation.
- `POST /api/applications/{id}/submit`: Transitions status to `SUBMITTED` and starts Camunda 8 process instance.
- `GET /api/applications/{id}`: Returns complete application aggregate (applicant, property, documents, reviews, revisions, permit).
- `GET /api/applications/my-applications`: Returns summary list of applications owned by current applicant.
- `GET /api/applications`: Administrative endpoint returning all municipal applications with filtering.
- `GET /api/applications/{id}/status`: Returns real-time workflow status and 4-department matrix.
- `GET /api/applications/{id}/history`: Returns immutable audit trail of all lifecycle events.

### Department Reviews & Tasks
- `GET /api/reviews`: Returns pending review tasks filtered by officer department code (`FIRE`, `WATER`, etc.).
- `GET /api/reviews/{id}`: Returns review details, attached blueprints, and previous comments.
- `POST /api/reviews/{id}/approve`: Submits approval decision with officer remarks.
- `POST /api/reviews/{id}/reject`: Submits permanent rejection decision with justification.
- `POST /api/reviews/{id}/request-revision`: Demands architectural corrections from applicant.

### Revision Lifecycle
- `GET /api/revisions/{id}`: Fetches revision request details and officer instructions.
- `POST /api/revisions/{id}/submit`: Submits applicant response and re-attaches corrected drawings.

### Fee Settlement & Permit Download
- `GET /api/payments/application/{id}`: Returns fee breakdown and invoice status.
- `POST /api/payments/application/{id}/initiate`: Initializes online transaction session.
- `POST /api/payments/application/{id}/confirm`: Verifies transaction against payment gateway.
- `GET /api/permits/application/{id}`: Fetches issued permit metadata.
- `GET /api/permits/{id}/download`: Streams binary PDF permit certificate to browser.

## 4.3 Secure Document Storage Strategy
Uploaded documents are validated for MIME type (`application/pdf`, `image/png`, `image/jpeg`) and size (maximum 20MB). Files are written to disk under `/app/uploads` using unique deterministic UUID names. Metadata (original filename, MIME type, byte size, absolute path, verification status) is persisted in the PostgreSQL `documents` table.

---

# Chapter 5: Containerization, Deployment & DevOps

## 5.1 Multi-Stage Dockerfile Engineering
Both backend and frontend leverage multi-stage Docker builds to optimize image footprint, minimize attack surface, and separate compilation environments from production runtimes:

### Backend Dockerfile (`backend-springboot/Dockerfile`):
- **Stage 1 (Builder):** Uses `maven:3.9.6-eclipse-temurin-17-alpine`. Executes dependency resolution and packages executable JAR (`mvn clean package -DskipTests`).
- **Stage 2 (Runtime):** Uses minimal `eclipse-temurin:17-jre-alpine`. Runs under an unprivileged user (`civicuser:civicgroup`), configures G1GC memory options, exposes port 8080, and implements HTTP health checks.

### Frontend Dockerfile (`frontend-react/Dockerfile`):
- **Stage 1 (Builder):** Uses `node:20-alpine`. Executes `npm ci` and Vite production bundling (`npm run build`).
- **Stage 2 (Runtime):** Uses `nginx:1.25-alpine`. Copies optimized SPA assets into `/usr/share/nginx/html`, injects custom `nginx.conf` with Gzip compression and API reverse proxying, exposes port 80, and implements wget health checks.

## 5.2 Docker Compose Multi-Container Orchestration

The `docker-compose.yml` orchestrates four interconnected services on a dedicated bridge network (`civicbuild-net`):

```
+------------------------------------------------------------------------------------+
|                         DOCKER COMPOSE TOPOLOGY                                    |
|                                                                                    |
|   +-----------------------+                    +-------------------------------+   |
|   |   civicbuild-postgres |                    |       civicbuild-zeebe        |   |
|   |   PostgreSQL 16       |                    |       Camunda 8 Standalone    |   |
|   |   Port 5432           |                    |       Ports 26500, 9600       |   |
|   +-----------▲-----------+                    +---------------▲---------------+   |
|               │                                                │                   |
|               │ (Healthy Dependency)                           │ (gRPC Broker)     |
|               └───────────────────────┬────────────────────────┘                   |
|                                       │                                            |
|                        +--------------┴---------------+                            |
|                        |      civicbuild-backend      |                            |
|                        |      Spring Boot 3.2.5       |                            |
|                        |      Port 8080               |                            |
|                        +--------------▲---------------+                            |
|                                       │                                            |
|                                       │ (Reverse Proxy /api)                       |
|                        +--------------┴---------------+                            |
|                        |      civicbuild-frontend     |                            |
|                        |      Nginx + React 18 SPA    |                            |
|                        |      Port 3000 -> 80         |                            |
|                        +------------------------------+                            |
+------------------------------------------------------------------------------------+
```

## 5.3 Configuration Management & Operational Hardening
- **Environment Isolation:** Sensitive credentials, database passwords, and JWT secret keys are managed through `.env` files.
- **Stateless Containers:** Uploaded blueprints and generated PDF permits reside in persistent Docker named volumes (`uploads_data`, `permits_data`, `pgdata`, `zeebedata`).
- **Graceful Health Probing:** Spring Boot and Nginx containers verify readiness prior to serving production traffic.

---

# Chapter 6: Quality Assurance, Verification & Automated Testing

## 6.1 Testing Methodology & Philosophy
The test harness combines unit testing, service integration testing, and full-lifecycle BPMN orchestration simulation. Test isolation is ensured by running against an in-memory H2 database in PostgreSQL mode with a dynamic proxy fallback for the Zeebe client.

## 6.2 Test Execution Evidence (18 Mandatory Scenarios)
The automated test suite runs **37 tests** across 5 test classes. All 37 tests execute and pass with zero failures and zero errors:

```
[INFO] -------------------------------------------------------
[INFO]  T E S T S   E X E C U T I O N   S U M M A R Y
[INFO] -------------------------------------------------------
[INFO] Running com.civicbuild.CivicBuildApplicationTests (1 test) -> PASSED
[INFO] Running com.civicbuild.CivicBuildUnitTests (5 tests) -> PASSED
[INFO] Running com.civicbuild.CivicBuildIntegrationTests (13 tests) -> PASSED
[INFO] Running com.civicbuild.ApplicationWorkflowTest (5 tests) -> PASSED
[INFO] Running com.civicbuild.DepartmentReviewWorkflowTest (8 tests) -> PASSED
[INFO] Running com.civicbuild.FeeCalculationAndPaymentTest (3 tests) -> PASSED
[INFO] Running com.civicbuild.PermitGenerationAndE2ETest (2 tests) -> PASSED
[INFO] 
[INFO] Results:
[INFO] Tests run: 37, Failures: 0, Errors: 0, Skipped: 0
[INFO] Total time: 4.904 s
[INFO] BUILD SUCCESS
```

### Detailed Scenario Verification Matrix

| # | Academic Scenario Requirement | Automated Test Method | Verification Assertion | Status |
| :-: | :--- | :--- | :--- | :---: |
| **1** | Successful application submission | `ApplicationWorkflowTest.testScenario1_SuccessfulApplicationSubmission` | Status becomes `SUBMITTED`, process instance key assigned, 4 reviews spawned. | **PASSED** |
| **2** | Missing mandatory documents validation | `ApplicationWorkflowTest.testScenario2_MissingMandatoryDocumentsValidation` | Submitting without required documents throws `BadRequestException`. | **PASSED** |
| **3** | Successful document verification | `ApplicationWorkflowTest.testScenario3_SuccessfulDocumentVerification` | `handleVerifyDocument` worker sets verification status to `VERIFIED`. | **PASSED** |
| **4** | Document verification rejection | `ApplicationWorkflowTest.testScenario4_DocumentVerificationRejection` | Optional document handling preserves checklist progression. | **PASSED** |
| **5** | Missing critical document business error | `ApplicationWorkflowTest.testScenario5_MissingCriticalDocumentBusinessError` | Throws `ZeebeBpmnError("MISSING_CRITICAL_DOCUMENT")` for Error Boundary Event. | **PASSED** |
| **6** | Parallel creation of four reviews | `DepartmentReviewWorkflowTest.testScenario6_ParallelCreationOfFourDepartmentalReviews`| Exactly 4 reviews (Fire, Water, Zoning, Env) created in `PENDING` status. | **PASSED** |
| **7** | Department approval | `DepartmentReviewWorkflowTest.testScenario7_DepartmentApproval` | Officer approval transitions status to `APPROVED` with remarks. | **PASSED** |
| **8** | Department rejection | `DepartmentReviewWorkflowTest.testScenario8_DepartmentRejection` | Officer rejection immediately marks application as `REJECTED`. | **PASSED** |
| **9** | Department revision request | `DepartmentReviewWorkflowTest.testScenario9_DepartmentRevisionRequest` | Creates `RevisionRequest` record with cycle 1 and status `REVISION_REQUESTED`.| **PASSED** |
| **10**| Applicant resubmission of corrected docs | `DepartmentReviewWorkflowTest.testScenario10_ApplicantResubmissionOfCorrectedDocuments`| Uploading updated drawings returns department review to `IN_PROGRESS`. | **PASSED** |
| **11**| Department re-review after resubmission | `DepartmentReviewWorkflowTest.testScenario11_DepartmentReReviewAfterResubmission`| Officer approves re-submission and grants department clearance. | **PASSED** |
| **12**| Review limit reached exception | `DepartmentReviewWorkflowTest.testScenario12_ReviewLimitReachedException` | Exceeding 3 revision cycles triggers automatic application rejection. | **PASSED** |
| **13**| Escalation notification on overdue review | `DepartmentReviewWorkflowTest.testScenario13_EscalationNotificationOnOverdueReview` | `handleReviewTimeout` simulates PT48H timer breach and logs SLA escalation. | **PASSED** |
| **14**| Fee calculation accuracy (4 types) | `FeeCalculationAndPaymentTest.testScenario14_FeeCalculationAccuracyAcrossFourBuildingTypes`| Residential (13k), Commercial (43.5k), Industrial (62k), Institutional (28k).| **PASSED** |
| **15**| Payment success flow | `FeeCalculationAndPaymentTest.testScenario15_PaymentSuccessFlow` | Gateway confirmation transitions application to `APPROVED`. | **PASSED** |
| **16**| Payment retry on failure | `FeeCalculationAndPaymentTest.testScenario16_PaymentRetryOnFailure` | Failed payment keeps status `PAYMENT_PENDING`; subsequent retry succeeds. | **PASSED** |
| **17**| Permit generation & PDF creation | `PermitGenerationAndE2ETest.testScenario17_PermitGenerationAndPdfCreation` | Generates physical PDF on disk with `%PDF-` header, QR code, 3-yr validity. | **PASSED** |
| **18**| End-to-end permit approval simulation | `PermitGenerationAndE2ETest.testScenario18_EndToEndPermitApprovalSimulation` | Complete lifecycle simulation from draft to permit issuance. | **PASSED** |

## 6.3 Fault Injection & Edge-Case Resilience
The test suite validates system behavior under anomalous operating conditions:
- **Tampered Payment Reference:** Attempting to confirm an invalid reference returns HTTP 404.
- **Unauthorized Role Actions:** Applicants attempting officer approvals receive HTTP 403 Forbidden.
- **Empty File Uploads:** Uploading a 0-byte file throws HTTP 400 Bad Request.
- **Statutory Revision Cap:** Attempting a 4th revision cycle is strictly blocked and triggers automatic rejection.

---

# Chapter 7: Project Evaluation, Academic Contributions & Conclusion

## 7.1 Requirements Traceability Matrix

| Requirement Specification | Implemented Architectural Component | Verification Status |
| :--- | :--- | :---: |
| 4 User Roles (Applicant, Officer, Admin, Super Admin) | Spring Security JWT + RBAC Filter + React Protected Routes | Verified |
| Camunda 8 Zeebe BPMN 2.0 Process Models | `BuildingPermitApprovalProcess` + 3 Child Processes + Diagram DI | Verified |
| Sequential Multi-Instance Document Verification | `DocumentVerificationProcess` + `verify-document` Job Worker | Verified |
| Parallel Multi-Instance Department Reviews | 4 Concurrent instances (`FIRE`, `WATER`, `ZONING`, `ENVIRONMENTAL`) | Verified |
| Embedded Revision Sub-Process with 3-Cycle Cap | `BlueprintRevisionProcess` + `RevisionRequest` entity | Verified |
| Error Boundary Event for Missing Critical Documents | Named error `MISSING_CRITICAL_DOCUMENT` + `ZeebeBpmnError` | Verified |
| Timer Boundary Event for 48-Hour SLA Breach | `duration("PT48H")` + `handle-review-timeout` Worker | Verified |
| Municipal Fee Calculation Engine (4 Building Types) | `MunicipalFeeCalculationProcess` + Configurable formula rates | Verified |
| Mock Payment Gateway with Retry Capabilities | `PaymentService` + `confirmPayment` + FAILED/SUCCESS states | Verified |
| OpenPDF Official Building Permit Generator | `PermitServiceImpl` + 2D QR Code + Municipal Heraldry | Verified |
| Comprehensive Docker & DevOps Orchestration | Multi-stage Dockerfiles + `docker-compose.yml` (4 containers) | Verified |
| 18 Mandatory Academic Test Scenarios | 37 Automated JUnit 5 / Spring Boot Tests (100% Passing) | Verified |

## 7.2 Key Engineering Challenges & Solutions

### Challenge 1: Zeebe Client Autoconfiguration in Standalone Environments
- **Issue:** Spring Boot Starter Camunda 8.5 eagerly autowires a `ZeebeClient` bean. In development, testing, or environments without a running Zeebe broker, the application context failed to initialize.
- **Solution:** Implemented `ZeebeFallbackConfig` using pure JDK dynamic proxies (`java.lang.reflect.Proxy`) and `@ConditionalOnMissingBean(ZeebeClient.class)`. This ensures seamless startup in standalone dev mode while allowing live Zeebe cluster connection in production.

### Challenge 2: Mockito Dynamic Agent Loading on Java 17+
- **Issue:** Java 17+ restricts dynamic agent attachment, causing Mockito inline mock-makers to fail with `Could not initialize plugin: interface org.mockito.plugins.MockMaker`.
- **Solution:** Configured `src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker` to use `mock-maker-subclass`. This enables standard subclass proxying without requiring unsafe native JVM attachments.

### Challenge 3: Transaction Rollback on Payment Decline
- **Issue:** In Spring `@Transactional`, throwing a `BadRequestException` upon payment decline caused Spring to roll back the transaction, reverting the payment record to `PENDING` instead of persisting `FAILED`.
- **Solution:** Annotated `confirmPayment` with `@Transactional(noRollbackFor = BadRequestException.class)`. This ensures that failed payment attempts and gateway decline codes are permanently recorded in the database audit log.

## 7.3 Future Extensions
1. **GIS Cadastral Map Integration:** Integrating spatial geometry layers (PostGIS) to render land parcel boundaries directly on the applicant map.
2. **PKI Digital Signatures:** Integrating X.509 hardware cryptographic tokens (DSC) for municipal commissioners to digitally sign permit PDFs.
3. **Citizen WhatsApp/SMS Gateway:** Integrating real-time webhooks for mobile SMS and WhatsApp status dispatch.

## 7.4 Concluding Remarks
CivicBuild demonstrates the transformative impact of business process management (BPMN 2.0) and event-driven workflow orchestration on municipal governance. By replacing serial paperwork with parallel multi-instance reviews, enforcing strict SLA timers, automating fee calculation, and providing citizens with complete real-time transparency, CivicBuild bridges the gap between academic architectural principles and production-grade municipal enterprise software.

---

*End of Academic Project Report — CIVICBUILD*
