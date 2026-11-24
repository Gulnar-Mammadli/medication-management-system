
# Medication Management System

A RESTful API built with **Spring Boot** for managing users and medication-related data. The application provides JWT-based authentication, role-based authorization, and secure user management.

## Tech Stack

- Java
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- JWT (JSON Web Tokens)
- Maven
- JUnit 5
- Mockito

## Features

### Authentication and Authorization

- User registration
- User login using email and password
- JWT-based stateless authentication
- Secure logout with server-side JWT revocation
- Password hashing
- Role-based access control
- Protected REST API endpoints
- Automatic cleanup of expired revoked tokens

### User Management

- Retrieve all users (Admin only)
- Retrieve user by ID (Admin only)
- Retrieve current user profile
- Delete users
- Prevent deletion of the last administrator

## Authentication API

Base URL: `/api/v1/auth`

| Method | Endpoint  | Description        |
|--------|-----------|--------------------|
| POST   | `/login`  | Authenticate user  |
| POST   | `/logout` | Revoke current JWT |

### Login

**Endpoint:** `POST /api/v1/auth/login`

Request:

```json
{
  "email": "test@example.com",
  "password": "SecurePassword123!"
}
```

Successful response:

```json
{
  "token": "your-jwt-token",
  "tokenType": "Bearer",
  "expiresIn": 3600000
}
```

### Logout

**Endpoint:** `POST /api/v1/auth/logout`

Include the JWT in the Authorization header:

```http
Authorization: Bearer your-jwt-token
```

Successful response: `204 No Content`

When a user logs out, the JWT is revoked and its SHA-256 hash is stored in the database. Subsequent requests using that token cannot authenticate.

Expired revoked tokens are automatically removed by a scheduled cleanup task.

## User Management API

Base URL: `/api/v1/users`

| Method | Endpoint    | Description           | Access        |
|--------|-------------|-----------------------|---------------|
| POST   | `/register` | Register a new user   | Public        |
| GET    | `/`         | Retrieve all users    | Admin         |
| GET    | `/{id}`     | Retrieve user by ID   | Admin         |
| GET    | `/me`       | Retrieve current user | Authenticated |
| DELETE | `/{id}`     | Delete user           | Admin         |

## Role-Based Authorization

The application supports two roles:

| Role         | Permissions                                      |
|--------------|--------------------------------------------------|
| `ROLE_USER`  | Access authorized user endpoints                 |
| `ROLE_ADMIN` | Access administrative endpoints and manage users |

The application enforces the last administrator protection rule. An administrator cannot be deleted if they are the only remaining administrator.

## JWT Authentication Flow

1. The user submits login credentials.
2. Spring Security authenticates the user.
3. `JwtService` generates a signed JWT.
4. The client receives the JWT.
5. The client includes the JWT in the Authorization header for protected requests.
6. `JwtAuthenticationFilter` validates the token and checks whether it has been revoked.
7. If the token is valid and not revoked, the request is authenticated.

### Token Revocation

The application uses a database-backed JWT revocation mechanism.

- Revoked tokens are stored as SHA-256 hashes.
- Original JWT values are not stored in the revocation table.
- Revoked tokens cannot authenticate subsequent requests.
- Expired revoked tokens are periodically removed.

## Project Structure

```text
src/
├── main/
│   ├── java/com/medication/management/
│   │   ├── auth/
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── entity/
│   │   │   ├── jwt/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   ├── common/
│   │   │   └── exception/
│   │   ├── config/
│   │   └── user/
│   │       ├── controller/
│   │       ├── dto/
│   │       ├── entity/
│   │       ├── repository/
│   │       └── service/
│   └── resources/
│       ├── db/migration/
│       └── application.yml
└── test/
    └── java/
```

## Database Migrations

The project uses **Flyway** to manage database schema changes.

Migration files are located in:

`src/main/resources/db/migration/`

Example migration files:

```text
V1__create_users_table.sql
V2__add_user_roles.sql
V3__create_revoked_tokens_table.sql
```

Flyway automatically executes pending migrations when the application starts.

## Configuration

Configure the database and JWT properties using environment variables.

Example `application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: validate

  flyway:
    enabled: true

jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION}
```

Never commit database credentials or JWT secrets to version control.

## Getting Started

### Prerequisites

- Java
- Maven
- PostgreSQL

### 1. Clone the repository

```bash
git clone <repository-url>
cd medication-management
```

### 2. Configure environment variables

Set the following environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
JWT_SECRET
JWT_EXPIRATION
```

### 3. Run the application

```bash
mvn clean install
mvn spring-boot:run
```

Flyway will automatically apply pending database migrations.

## Running Tests

Execute all tests:

```bash
mvn test
```

The test suite covers:

- User registration
- Authentication and login
- JWT generation and validation
- JWT token revocation
- User management
- Role-based authorization
- Last administrator protection
- Exception handling

## Security

The application implements several security measures:

- Password hashing
- Signed JWT authentication
- Server-side token revocation
- Role-based access control
- Stateless session management
- Protected API endpoints
- Secure handling of application secrets

## Future Improvements

- Change password functionality
- Forgot password and password reset
- Refresh token mechanism
- Login rate limiting
- Audit logging
- Medication management
- Medication schedules and reminders
- Prescription management

## License

This project is currently under development.
