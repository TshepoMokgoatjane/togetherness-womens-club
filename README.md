# Togetherness Women's Club

A member management system built for the Togetherness Women's Club. This web application allows administrators to manage club members and their dependents through a clean, responsive interface.

## Features

- **Member Management** — Create, view, edit, and soft-delete members
- **Dependent Management** — Link dependents to members with full CRUD operations
- **SA ID Validation** — Custom validator for South African ID numbers with automatic date of birth and age extraction
- **Role-Based Access** — Spring Security with ADMIN and TREASURER roles
- **Soft Deletes** — Records are never permanently removed; they are flagged as deleted
- **Duplicate Detection** — Prevents duplicate ID numbers and email addresses with graceful error handling
- **Responsive UI** — Bootstrap 5 with Bootstrap Icons via CDN

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 21                             |
| Framework    | Spring Boot 4.0.6                   |
| Web          | Spring MVC + Thymeleaf              |
| Security     | Spring Security (form login)        |
| Persistence  | Spring Data JPA + Hibernate         |
| Database     | MySQL                               |
| Validation   | Jakarta Bean Validation + custom    |
| Build        | Maven (with Maven Wrapper)          |
| Frontend     | Bootstrap 5.3.3 CDN + Bootstrap Icons |

## Prerequisites

- Java 21+
- MySQL 8.0+
- Maven 3.9+ (or use the included Maven Wrapper)

## Getting Started

### 1. Create the database

```sql
CREATE DATABASE togetherness_womens_club;
CREATE USER 'webtogetherness'@'localhost' IDENTIFIED BY 'webtogetherness';
GRANT ALL PRIVILEGES ON togetherness_womens_club.* TO 'webtogetherness'@'localhost';
FLUSH PRIVILEGES;
```

### 2. Configure the application

Database settings are in `src/main/resources/application.properties`. Update the credentials if yours differ from the defaults.

### 3. Build and run

```bash
# Compile
./mvnw compile

# Run
./mvnw spring-boot:run
```

On Windows:
```cmd
.\mvnw.cmd spring-boot:run
```

### 4. Access the application

Open [http://localhost:8080/members](http://localhost:8080/members) in your browser.

## Project Structure

```
src/main/java/za/co/twc/togetherness/womens/club/
├── config/             # Security configuration
├── controller/         # MemberController, DependentController
├── domain/             # JPA entities (Member, Dependent, enums)
├── exception/          # Custom exceptions
├── repository/         # Spring Data JPA repositories
├── service/            # Business logic layer
├── utilities/          # SA ID utilities (DOB extraction)
└── validation/         # Custom validators (@ValidSAId)

src/main/resources/
├── templates/
│   ├── layout/         # Shared layout (main.html)
│   ├── member/         # Member templates (list, form, view)
│   ├── dependent/      # Dependent templates (list, form)
│   └── error/          # Error pages (404)
└── application.properties
```

## Member Statuses

| Status      | Description                              |
|-------------|------------------------------------------|
| ACTIVE      | Current active member                    |
| INACTIVE    | Membership paused                        |
| SUSPENDED   | Membership suspended                     |
| DECEASED    | Member has passed away                   |

## Relationship Types (Dependents)

CHILD, SPOUSE, PARENT, SIBLING, OTHER

## Key Design Decisions

- **Soft deletes** — All deletions set a `deleted` flag rather than removing rows. This preserves history and allows reactivation.
- **SA ID as source of truth** — Date of birth and age are computed from the ID number rather than stored separately, avoiding data inconsistency.
- **Thymeleaf layout pattern** — A shared `layout/main.html` provides the navbar, footer, alerts, and CDN links. Child pages only define their content fragment.
- **CSP headers** — Content Security Policy is configured to allow Bootstrap CDN while maintaining security.

## License

Private — All rights reserved.
