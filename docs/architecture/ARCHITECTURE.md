# CIVICBUILD – Technical System Architecture & Specification

## 1. Executive System Overview

**CivicBuild** is an enterprise-grade municipal building permit automation portal designed for transparent, multi-departmental workflow orchestration. In modern civic governance, physical and fragmented digital permit requests encounter chronic delays, opaque approvals, repeated document checks, and inter-departmental bottlenecks. CivicBuild provides an end-to-end event-driven architecture powered by **Camunda 8 (Zeebe)** workflow orchestration, **Java 17 / Spring Boot 3** microservices, **PostgreSQL** relational persistence, and a modern **React 18** responsive web portal.

```
+-----------------------------------------------------------------------------------+
|                                CIVICBUILD CLIENTS                                 |
|                                                                                   |
|  [Applicant Portal]      [Officer Tasklist]      [Admin Console]    [Super Admin] |
|   - New Submissions       - Fire Review           - Process Monitor  - System Cfg |
|   - Doc Re-uploads        - Water Review          - Overdue Escal.   - User Roles |
|   - Fee Payment           - Zoning Review         - Audit Logs       - Dept Rules |
|   - Permit Download       - Environment Review    - Metrics                       |
+------------------------------------------+----------------------------------------+
                                           | HTTPS / REST (JWT Bearer Token)
                                           v
+-----------------------------------------------------------------------------------+
|                         SPRING BOOT 3 REST APPLICATION                            |
|                                                                                   |
|  [Controllers]        [Security Filter]     [Service Layer]      [Exception Handler]
|   - AuthController     - JWT Token Filter    - ApplicationService - GlobalAdvice   |
|   - AppController      - Role Authorization  - ReviewService      - ZeebeBpmnError |
|   - ReviewController   - Method Security     - PaymentService                      |
|   - TaskController                           - PermitService                       |
|   - AdminController                          - NotificationService                 |
|                                                                                   |
|  [Zeebe Job Workers]                                                              |
|   - validate-application         - calculate-municipal-fee                        |
|   - verify-document (seq)        - create-payment-request                          |
|   - verify-land-registry         - confirm-payment                                 |
|   - handle-review-timeout        - generate-permit                                 |
|   - send-notification            - update-application-status                       |
+---------------------+-------------------------------------+-----------------------+
                      |                                     |
                      v                                     v
+-----------------------------------+     +-----------------------------------------+
|     CAMUNDA 8 (ZEEBE BROKER)      |     |           POSTGRESQL DATABASE           |
|                                   |     |                                         |
|  [Main Process]                   |     |  - users              - departments     |
|   BuildingPermitApprovalProcess   |     |  - roles              - department_revs |
|                                   |     |  - applications       - revision_reqs   |
|  [Child Processes / Call Acts]    |     |  - properties         - payments        |
|   - DocumentVerificationProcess   |     |  - documents          - permits         |
|   - MunicipalFeeCalculationProcess|     |  - workflow_histories - notifications   |
|   - PermitNotificationProcess     |     |  - audit_logs                           |
|                                   |     +-----------------------------------------+
|  [Embedded Sub-Processes]         |
|   - BlueprintRevisionProcess      |
|                                   |
|  [BPMN Constructs]                |
|   - Parallel Multi-Instance (4)   |
|   - Sequential Multi-Instance     |
|   - Error & Timer Boundary Events |
|   - FEEL Decision Expressions     |
+-----------------------------------+
```

---

## 2. Technology Version Matrix

| Layer / Component | Technology & Library | Version | Compatibility Rationale |
|---|---|---|---|
| **Workflow Engine** | Camunda 8 Zeebe | `8.5.0` | Latest LTS Zeebe engine supporting FEEL expressions, Call Activities, and BPMN 2.0 |
| **Java Runtime** | OpenJDK | `17.0.18` (LTS) | Enterprise baseline for Spring Boot 3 and modern Jakarta EE |
| **Backend Framework**| Spring Boot | `3.2.5` | Native Jakarta namespace, high-throughput WebMVC, robust JPA |
| **Camunda Starter** | `spring-zeebe-starter` | `8.5.0` | Seamless `@JobWorker` autowiring, ZeebeClient lifecycle management |
| **Security** | Spring Security + JJWT | `6.2.4` / `0.12.5` | Stateless JWT authentication, role-based method authorization (`@PreAuthorize`) |
| **ORM / Database** | Spring Data JPA / Hibernate | `6.4.x` | Managed transactions, composite entity relationships, dirty checking |
| **Database Engine** | PostgreSQL | `16-alpine` | ACID-compliant relational persistence, JSONB support for workflow audit trails |
| **Local Fallback DB**| H2 Database | `2.2.224` | In-memory PostgreSQL mode for zero-setup local test execution |
| **PDF Generation** | OpenPDF | `1.3.39` | Standard LGPL PDF engine to generate authenticated municipal permit certificates |
| **Frontend Runtime** | Node.js | `24.x / 20.x` | Modern ECMAScript and high-speed build toolchain |
| **Frontend Engine** | React + Vite | `18.3.1` / `5.2.x` | Fast HMR, optimized production chunks, modular component architecture |
| **Routing** | React Router DOM | `6.23.0` | Declarative client-side routing, protected auth routes, dynamic params |
| **Styling** | Tailwind CSS | `3.4.3` | Utility-first responsive design, municipal theme (Navy Blue, Teal, Slate) |
| **Icons & Charts** | Lucide React + Recharts | `0.378` / `2.12.7` | Accessible SVG icon library and vector analytics for municipal dashboards |
| **Containerization**| Docker & Docker Compose | Compose v2 | Multi-stage production container images with non-root runtime users |

---

## 3. Four User Personas & Permissions Matrix

1. **APPLICANT (`ROLE_APPLICANT`)**:
   - Access: Register, Log In, Profile.
   - Operations: Draft and submit building permit applications; upload mandatory documents (blueprint, ownership proof, stability certificate, etc.); view live 7-stage workflow tracker; review revision requests; resubmit corrected blueprints; initiate & confirm fee payment; download official PDF permits.
   - Boundaries: Strictly scoped to user's own applications (`applicant_id == principal.id`).

2. **DEPARTMENT OFFICER (`ROLE_OFFICER`)**:
   - Access: Department Dashboard, Assigned Task Inbox.
   - Departments: Fire Department (`FIRE`), Water & Sewerage Department (`WATER`), Town Planning & Zoning (`ZONING`), Environmental Protection (`ENVIRONMENTAL`).
   - Operations: Inspect submitted documents; enter review comments; issue definitive decisions: `APPROVED`, `REJECTED`, or `REVISION_REQUESTED`.
   - Boundaries: Scoped to their assigned department's tasks.

3. **ADMINISTRATOR (`ROLE_ADMIN`)**:
   - Access: Unified Admin Dashboard, All Applications Monitor, Department Management, Overdue Review Escalations, Audit Logs.
   - Operations: Monitor SLA compliance; inspect stuck or timed-out process instances; view cross-departmental bottlenecks.

4. **SUPER ADMIN (`ROLE_SUPER_ADMIN`)**:
   - Access: Full System Console, User & Role Management, System Configuration, Workflow Engine Health.
   - Operations: Provision officer credentials; configure municipal fee calculation multipliers; inspect engine status.

---

## 4. End-to-End Workflow & BPMN Interaction Flow

1. **Submission (`POST /api/applications/{id}/submit`)**:
   - Applicant submits draft application with mandatory documents.
   - Spring Boot validates payload completeness, assigns unique application number (`CB-YYYY-XXXX`), and initiates Camunda 8 `BuildingPermitApprovalProcess` with variables:
     - `applicationId`: Long
     - `applicationNumber`: String
     - `buildingType`: String (RESIDENTIAL / COMMERCIAL / INDUSTRIAL / INSTITUTIONAL)
     - `plotArea`: Double
     - `builtUpArea`: Double
     - `numberOfFloors`: Integer
     - `documents`: List<DocumentVerificationItem>
     - `departmentList`: `["FIRE", "WATER", "ZONING", "ENVIRONMENTAL"]`
     - `revisionCount`: 0
     - `maxRevisions`: 3

2. **Validation & Land Registry Service Tasks**:
   - Service task `validate-application` executes formal schema and regulatory bounds check.
   - Service task `verify-land-registry` contacts mock municipal land records REST endpoint verifying survey number and ownership credentials.

3. **Document Verification (Sequential Multi-Instance)**:
   - Call Activity `DocumentVerificationProcess` iterates over each document sequentially.
   - Worker `verify-document` audits document existence, file signatures, and validity.
   - If a mandatory document is corrupted or missing, a `ZeebeBpmnError("MISSING_CRITICAL_DOCUMENT")` is thrown, caught by an **Error Boundary Event** that routes the process to application rejection or applicant notification.

4. **Department Review (Parallel Multi-Instance)**:
   - Parallel Multi-Instance activity spawns 4 concurrent sub-paths for `["FIRE", "WATER", "ZONING", "ENVIRONMENTAL"]`.
   - Each review instance registers a user task in the Tasklist.
   - A **Timer Boundary Event** (`PT48H` in production, configurable) monitors SLA; if overdue, worker `handle-review-timeout` alerts the Administrator.
   - If a department requests revisions, the embedded **`BlueprintRevisionProcess`** sub-process is triggered:
     - Worker `update-application-status` sets status to `REVISION_REQUESTED`.
     - Worker `send-notification` alerts the applicant.
     - Applicant uploads revised documents via `POST /api/revisions/{id}/submit`.
     - Review is reopened for that department.
     - Revision counter increments; if `revisionCount > maxRevisions`, auto-rejected.

5. **Approval Gateway & Convergence**:
   - Once all 4 parallel instances finish, an Exclusive Gateway evaluates:
     `every r in departmentReviews satisfies r.status = "APPROVED"`
   - If any department permanently rejects: application status updated to `REJECTED`, notification sent, workflow terminates.
   - If all 4 approve: status becomes `FEE_PENDING`, routes to fee calculation.

6. **Municipal Fee Calculation (Call Activity)**:
   - Call Activity `MunicipalFeeCalculationProcess` invokes worker `calculate-municipal-fee`.
   - Formula:
     - Residential: Base ₹5,000 + ₹2.5 per sq.ft built-up area + (₹1,500 * floors)
     - Commercial: Base ₹15,000 + ₹6.0 per sq.ft built-up area + (₹3,500 * floors)
     - Industrial: Base ₹25,000 + ₹8.0 per sq.ft built-up area + (₹5,000 * floors)
     - Institutional: Base ₹10,000 + ₹4.0 per sq.ft built-up area + (₹2,000 * floors)
   - Calculated fee and line items stored into database and process variables.

7. **Payment Processing**:
   - Worker `create-payment-request` initializes a pending transaction.
   - Applicant navigates to Payment page and completes checkout via mock payment gateway (`POST /api/payments/{applicationId}/confirm`).
   - Worker `confirm-payment` verifies payment token and receipt.

8. **Permit Generation & Notification (Call Activity)**:
   - Worker `generate-permit` allocates permanent permit number `CB-PERMIT-2026-XXXX`, stamps digital signature and QR verification payload, compiles PDF document via OpenPDF, and archives permit metadata.
   - Call Activity `PermitNotificationProcess` invokes worker `send-notification` emailing the applicant and archiving in-app notification.
   - Process completes at End Event: `Permit Issued Successfully`.
