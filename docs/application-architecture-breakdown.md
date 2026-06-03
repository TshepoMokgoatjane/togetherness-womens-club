# Togetherness Women's Club — Application Architecture Breakdown

## Overview

This document provides a comprehensive breakdown of all features in the Togetherness Women's Club
Spring Boot application, mapped to the Entity → Repository → Service → Controller → Thymeleaf
layered architecture pattern.

---

## Package Structure

```
za.co.twc.togetherness.womens.club
├── aspect/          (AOP - audit logging)
├── config/          (Security, AOP, Async, Web configs)
├── controller/      (15 controllers)
├── domain/          (8 entities + 5 enums + 2 form DTOs + 1 converter)
├── event/           (Application events)
├── exception/       (15 custom exceptions)
├── filter/          (Rate limiting filter)
├── listener/        (Event listeners)
├── repository/      (8 JPA repositories)
├── scheduler/       (Cron-based schedulers)
├── service/         (14 service classes)
├── utilities/       (RequestUtils, SaIdUtils)
└── validation/      (Custom @ValidSAId annotation + validator)
```

---

## Feature Modules

### 1. Members Module

| Layer | Class |
|-------|-------|
| Entity | `Member.java` (memberNumber, firstName, lastName, idNumber, email, physicalAddress, phoneNumber, alternativePhoneNumber, joinDate, status, deleted) |
| Enum | `MemberStatus` (ACTIVE, INACTIVE, SUSPENDED, DECEASED) |
| Repository | `MemberRepository.java` |
| Service | `MemberService.java` (create, update, soft-delete, change status, search/filter) |
| Controller | `MemberController.java` (`/members`) |
| Templates | `member/list.html`, `member/form.html`, `member/view.html` |

**Key business rules:**
- Soft delete (never hard-deleted)
- Auto-generates member number
- Custom SA ID validation (`@ValidSAId`)
- Pagination and search/filter support

---

### 2. Dependents Module

| Layer | Class |
|-------|-------|
| Entity | `Dependent.java` (firstName, lastName, idNumber, email, phoneNumber, relationship, member) |
| Enum | `RelationshipType` (CHILD, SPOUSE, PARENT, SIBLING, OTHER) |
| Repository | `DependentRepository.java` |
| Service | `DependentService.java` (create with reactivation logic, soft-delete) |
| Controller | `DependentController.java` (`/members/{id}/dependents`) + `MyController.java` (`/my/dependents`) |
| Templates | `dependent/list.html`, `dependent/form.html`, `my/dependents.html`, `my/dependent-form.html` |

**Key business rules:**
- Soft delete with reactivation (if same ID number re-added)
- Transient fields: dateOfBirth and age (derived from SA ID)
- Members can self-service add dependents via `/my/dependents`

---

### 3. Contributions Module

| Layer | Class |
|-------|-------|
| Entity | `Contribution.java` (amount, paymentDate, reference, status, member, contributionMonth) |
| Enum | `ContributionStatus` (PAID, PENDING, MISSED) |
| Converter | `YearMonthAttributeConverter.java` |
| Repository | `ContributionRepository.java` |
| Service | `ContributionService.java` (create/update, generate monthly, mark missed, reporting) |
| Scheduler | `ContributionScheduler.java` (cron jobs) |
| Controller | `ContributionController.java` (`/contributions`, `/members/{id}/contributions`) + `MyController.java` (`/my/contributions`) |
| Templates | `contribution/all.html`, `contribution/new.html`, `contribution/form.html`, `contribution/list.html`, `my/contributions.html` |

**Key business rules:**
- Cron job on 1st of month: generates PENDING contributions for all active members
- Admin records payment: updates PENDING → PAID
- Cron job on last day of month: marks remaining PENDING as MISSED
- Duplicate protection (cannot pay twice for same month)
- Proof of payment document upload

---

### 4. Burial Claims Module

| Layer | Class |
|-------|-------|
| Entity | `BurialClaim.java` (deceasedName, claimDate, claimAmount, status, member) |
| Enum | `ClaimStatus` (APPROVED, DECLINED, PENDING) |
| Repository | `BurialClaimRepository.java` |
| Service | `BurialClaimService.java` (create, approve, decline, eligibility check) |
| Event | `ClaimStatusChangedEvent.java` |
| Listener | `ClaimStatusChangedListener.java` (async email notifications) |
| Controller | `BurialClaimController.java` (`/claims`) + `MyController.java` (`/my/claims`) |
| Templates | `claim/list.html`, `claim/new.html`, `claim/form.html`, `my/claims.html`, `my/claim-form.html` |

**Key business rules:**
- Eligibility: member must have PAID contributions for last 3 consecutive months
- Approve/Decline triggers Spring Application Event
- Async email notification sent to member on status change
- Supporting document upload (death certificate, etc.)

---

### 5. Documents Module

| Layer | Class |
|-------|-------|
| Entity | `Document.java` (fileName, contentType, fileSize, data [LONGBLOB], documentType, claimId, contributionId) |
| Repository | `DocumentRepository.java` |
| Service | `DocumentService.java` (upload for claim/contribution, download, validate) |
| Controller | `DocumentController.java` (`/documents/{id}/download`) |
| Templates | _(inline download — no dedicated page)_ |

**Key business rules:**
- Max file size: 5MB
- Allowed types: PDF, JPEG, PNG
- Linked to either a claim OR a contribution

---

### 6. Users & Admin Module

| Layer | Class |
|-------|-------|
| Entity | `User.java` (username, password, role, email, memberId) |
| Repository | `UserRepository.java` |
| Service | `CustomUserDetailsService.java` |
| Controller | `UserManagementController.java` (`/admin/users`) |
| Templates | `admin/users.html` |

**Key business rules:**
- Roles: ADMIN, TREASURER, USER
- Link/unlink user accounts to member records
- Delete only unlinked users (safety guard)
- Admin-only access

---

### 7. Audit Trail Module

| Layer | Class |
|-------|-------|
| Entity | `AuditLog.java` (action, entityName, entityId, username, timestamp, details) |
| Enum | `AuditAction` (CREATED, UPDATED, DELETED) |
| Repository | `AuditLogRepository.java` |
| Service | `AuditService.java` |
| Aspect | `AuditAspect.java` (intercepts service methods via AOP) |
| Controller | `AuditController.java` (`/admin/audit`) |
| Templates | `admin/audit.html` |

**Key business rules:**
- Automatically logs all create/approve/decline/delete service operations
- Uses AOP (@AfterReturning pointcuts) — no manual audit code in services
- Admin-only access

---

### 8. Authentication / Login

| Layer | Class |
|-------|-------|
| Service | `CustomUserDetailsService.java` (Spring Security UserDetailsService) |
| Controller | `LoginController.java` (`/login`, `/login-success`, `/access-denied`) |
| Templates | `login.html`, `error/403.html` |

**Key features:**
- Custom login page
- Role-based redirect after login (ADMIN/TREASURER → `/home`, USER → `/my`)
- Session management (max 1 session, fixation protection)
- Remember-me (7 days)
- Rate limiting on login attempts (5/min per IP)

---

### 9. Registration

| Layer | Class |
|-------|-------|
| DTO | `RegistrationForm.java` (username, email, password, confirmPassword) |
| Controller | `RegistrationController.java` (`/register`) |
| Templates | `register.html` |

**Key features:**
- Bean Validation (`@NotBlank`, `@Email`, `@Size`)
- Duplicate username and email check
- Rate limiting (3 registrations/min per IP)
- Creates user with USER role

---

### 10. Forgot Password / Reset

| Layer | Class |
|-------|-------|
| Entity | `PasswordResetToken.java` (token, expiryDate, user) |
| DTO | `ResetPasswordForm.java` |
| Repository | `PasswordResetTokenRepository.java` |
| Service | `PasswordResetService.java` (create token, validate, reset, send email) |
| Controller | `ForgotPasswordController.java` (`/forgot-password`, `/reset-password`) |
| Templates | `forgot-password.html`, `reset-password.html` |

**Key features:**
- Token-based password reset via email link
- Token expiry validation
- Uses EmailService abstraction (Resend in prod, SMTP locally)
- Rate limited (3 requests/min per IP)

---

### 11. Profile / Change Password

| Layer | Class |
|-------|-------|
| Controller | `ProfileController.java` (`/profile`, `/profile/change-password`) |
| Templates | `profile/view.html`, `profile/change-password.html` |

**Key features:**
- View profile information
- Change password (validates current password first)
- Available to all authenticated users

---

### 12. Reports

| Layer | Class |
|-------|-------|
| Service | `PdfExportService.java` (iText), `ExcelExportService.java` (Apache POI) |
| Controller | `ReportController.java` (`/reports/contributions`, `/reports/export/pdf`, `/reports/export/excel`) |
| Templates | `report/contribution-report.html` |

**Key features:**
- Contribution dashboard with charts (Chart.js)
- Export to PDF and Excel
- ADMIN/TREASURER access only

---

### 13. Member Self-Service Portal

| Layer | Class |
|-------|-------|
| Controller | `MyController.java` (`/my`) |
| Templates | `my/dashboard.html`, `my/not-linked.html`, `my/dependents.html`, `my/dependent-form.html`, `my/contributions.html`, `my/claims.html`, `my/claim-form.html` |

**Key features:**
- Dashboard showing member info, dependents, contributions
- Self-service: add dependents, view contributions, submit claims
- Requires user account linked to member record

---

### 14. Public Landing Pages

| Layer | Class |
|-------|-------|
| Controller | `LandingController.java` (`/`, `/about`, `/gallery`, `/contact`) |
| Templates | `public/landing.html`, `public/about.html`, `public/gallery.html`, `public/contact.html` |

**Key features:**
- Public-facing marketing pages
- Contact form with email (uses EmailService)
- SEO-optimized meta tags

---

### 15. Home Dashboard (Admin/Treasurer)

| Layer | Class |
|-------|-------|
| Controller | `HomeController.java` (`/home`) |
| Templates | `home.html` |

**Key features:**
- Summary statistics (total members, contributions, pending claims)
- Quick access to admin functions

---

## Cross-Cutting Concerns

### Security (Spring Security)

| Component | Details |
|-----------|---------|
| Config | `SecurityConfig.java` |
| Password | BCrypt encoding |
| Access Control | Role-based: ADMIN, TREASURER, USER |
| Session | Max 1 session, fixation protection, 30min timeout |
| Remember-Me | 7-day token |
| Headers | Content Security Policy, cache control |
| Filter | `RateLimitingFilter` (before auth filter) |

### Rate Limiting (Bucket4j)

| Endpoint | Limit |
|----------|-------|
| POST `/login` | 5 per minute per IP |
| POST `/register` | 3 per minute per IP |
| POST `/forgot-password` | 3 per minute per IP |

### AOP (Aspect-Oriented Programming)

- **Config:** `AopConfig.java` (`@EnableAspectJAutoProxy`)
- **Aspect:** `AuditAspect.java`
  - Intercepts: `service.create*()`, `service.approve*()`, `service.decline*()`, `service.delete*()`
  - Logs to `AuditLog` entity automatically

### Scheduling (@Scheduled)

| Cron Expression | Job |
|-----------------|-----|
| `0 0 0 1 * ?` | Generate PENDING contributions (1st of month) |
| `0 0 23 L * ?` | Mark missed contributions (last day of month) |

### Async Processing

- **Config:** `AsyncConfig.java` (`@EnableAsync`)
- **Usage:** `ClaimStatusChangedListener` sends email notifications asynchronously

### Event-Driven Architecture

- **Event:** `ClaimStatusChangedEvent` (published on claim approve/decline)
- **Listener:** `ClaimStatusChangedListener` (`@Async @EventListener`)

### Email

| Profile | Implementation | Transport |
|---------|---------------|-----------|
| Local (`!prod`) | `SmtpEmailService` | JavaMailSender (SMTP) |
| Production (`prod`) | `ResendEmailService` | Resend HTTP API (port 443) |

### Custom Validation

- **Annotation:** `@ValidSAId`
- **Validator:** `SouthAfricanIdValidator.java`
- **Validates:** South African ID number format, checksum, date of birth

---

## Test Coverage

| Test Class | Layer |
|------------|-------|
| `BurialClaimServiceTest.java` | Service |
| `ContributionServiceTest.java` | Service |
| `MemberServiceTest.java` | Service |
| `RateLimiterServiceTest.java` | Service |
| `RateLimitingFilterTest.java` | Filter |
| `UserManagementControllerTest.java` | Controller |
| `TogethernessWomensClubApplicationTests.java` | Integration |

---

## Shared Layouts & Templates

| Template | Purpose |
|----------|---------|
| `layout/main.html` | Authenticated pages layout |
| `layout/public.html` | Public pages layout |
| `fragments/pagination.html` | Reusable pagination component |
| `error.html` | Generic error page |
| `error/403.html` | Access denied |
| `error/404.html` | Not found |

---

## Custom Exceptions (15)

- `MemberNotFoundException`, `MemberInactiveException`, `MemberDeceasedException`
- `MemberHasDependentsException`, `MemberCannotAddDependentsException`
- `DependentNotFoundException`, `DuplicateDependentException`
- `DuplicateEmailAddressException`
- `DuplicateMonthlyContributionException`, `InvalidContributionAmountException`, `NonActiveMemberContributionException`
- `MemberMissedLastThreeConsecutiveMonthsException`
- `InvalidEmailTokenException`, `EmailTokenExpiredException`, `FailedToSendPasswordResetEmailException`

---

## Recommended Udemy Courses

### Currently Taking
- **Chad Darby — Spring Boot 4: Learn Spring 7, Spring Core, Spring REST, Spring Security, JPA, Hibernate, Swagger, Spring MVC, MySQL**
  - Covers: Spring Core, MVC, JPA/Hibernate, Security basics, REST APIs

### Recommended Next

| Course | What it covers from your app |
|--------|------------------------------|
| **"Spring Security 6 Zero to Master"** — Eazy Bytes (Madan Reddy) | Role-based access, filters, CSRF, session management, remember-me, rate limiting |
| **"Spring Framework 6: Beginner to Guru"** — John Thompson | Events, async, AOP, profiles, testing, production patterns |
| **"Testing Spring Boot: Beginner to Guru"** — John Thompson | JUnit 5, Mockito, @WebMvcTest, @DataJpaTest, integration tests |
| **"Thymeleaf with Spring Boot"** — Dan Vega / free docs | Layout dialect, fragments, forms, security integration |

### Topics best learned from documentation/blogs (not Udemy)

- **Bucket4j rate limiting** — Official docs + Baeldung
- **iText PDF generation** — Official iText docs
- **Apache POI Excel** — Official docs + tutorials
- **Resend Email API** — Resend documentation
- **Railway deployment** — Railway docs

---

_Generated: June 2026_
_Application: Togetherness Women's Club v0.0.1-SNAPSHOT_
_Stack: Java 21, Spring Boot 4.0.6, MySQL, Thymeleaf, Bootstrap 5_
