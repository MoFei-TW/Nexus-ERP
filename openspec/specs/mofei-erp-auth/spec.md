# mofei-erp-auth Specification

## Purpose

Defines authentication, session security, and role-based access control (RBAC) mechanisms for the MOFEI ERP system.

## Requirements

### Requirement: User Authentication and Default Administrator
The system SHALL provide `/login` and `/logout` endpoints and views, authenticating credentials against the database where passwords and salt values MUST be securely hashed and encrypted (never stored in plain text). The system SHALL provide a default administrator account `admin` with password `admin` securely initialized.

#### Scenario: Successful administrator login
- **WHEN** a user enters valid credentials `admin` / `admin` on the `/login` page
- **THEN** the system authenticates the user, generates a secure session, and redirects the user to the dashboard with administrator privileges

#### Scenario: User logout
- **WHEN** an authenticated user clicks the logout button
- **THEN** the system terminates the session, clears authentication cookies, and redirects the user to `/login`

### Requirement: Unauthenticated Read-Only View Restriction
The system SHALL permit unauthenticated visitors to inspect frontend UI layouts and simulate button interactions, but SHALL strictly block all backend CRUD RESTful API executions when unauthenticated.

#### Scenario: Unauthenticated user attempts CRUD operation
- **WHEN** an unauthenticated visitor triggers an action that calls a backend CRUD mutation endpoint
- **THEN** the system rejects the request with an HTTP 401 Unauthorized response and prevents database mutation

### Requirement: Strict Cookie Security Attributes
The system SHALL enforce the three pillars of cookie security on all authentication cookies: `HttpOnly=true`, `Secure=true`, and `SameSite=Strict`.

#### Scenario: Cookie generation on authentication
- **WHEN** the system issues an authentication cookie to a client
- **THEN** the cookie header contains `HttpOnly`, `Secure`, and `SameSite=Strict` flags to prevent XSS theft and CSRF attacks

### Requirement: Hierarchical Role-Based Access Control (RBAC)
The system SHALL enforce hierarchical RBAC policies where `Admin` role holds full access across `admin > employee > product > customer > order` modules, and `Employee` role holds access to `employee > product > customer > order` modules.

#### Scenario: Employee attempts admin module access
- **WHEN** a user with only `Employee` role attempts to access an administrative configuration endpoint
- **THEN** the system denies access with an HTTP 403 Forbidden response
