# Advanced Task 3: Enterprise-Grade OAuth2 & JWT Security System

## Objective
Build a production-grade authentication and authorization system using Spring Boot and Spring Security with multi-tenant architecture, scalable authentication, and secure API access.

## Tech Stack
- Java 17
- Spring Boot 3.5.15
- Spring Security 6
- MySQL 8
- JWT (jjwt 0.11.5)
- SpringDoc OpenAPI / Swagger UI 2.3.0
- Maven 3

## Project Structure
oauth-jwt-security/
├── src/main/java/com/security/
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── SwaggerConfig.java
│   │   └── DataInitializer.java
│   ├── controller/
│   │   ├── AuthController.java
│   │   ├── UserController.java
│   │   └── AdminController.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── UserService.java
│   │   ├── CustomUserDetailsService.java
│   │   ├── TokenBlacklistService.java
│   │   ├── RateLimitService.java
│   │   └── AuditLogService.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── PermissionRepository.java
│   │   ├── TenantRepository.java
│   │   ├── RefreshTokenRepository.java
│   │   └── AuditLogRepository.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── Tenant.java
│   │   ├── RefreshToken.java
│   │   └── AuditLog.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── UserRequest.java
│   │   ├── UserResponse.java
│   │   └── ApiResponse.java
│   ├── filter/
│   │   ├── JwtAuthFilter.java
│   │   └── RateLimitFilter.java
│   ├── exception/
│   │   ├── CustomException.java
│   │   └── GlobalExceptionHandler.java
│   └── util/
│       └── JwtUtil.java
├── src/main/resources/
│   └── application.properties
├── README.md
└── pom.xml

## Database Tables
- users (id, username, password, email, enabled, tenant_id)
- roles (id, role_name)
- permissions (id, permission_name)
- user_roles (user_id, role_id)
- role_permissions (role_id, permission_id)
- tenants (id, tenant_name)
- refresh_tokens (id, token, user_id, expiry_date)
- audit_logs (id, username, action, ip_address, success, timestamp)

## Default Users
| Username | Password   | Role    | Tenant  |
|----------|------------|---------|---------|
| admin    | admin123   | ADMIN   | TenantA |
| user1    | user123    | USER    | TenantA |
| manager1 | manager123 | MANAGER | TenantB |

## Roles and Permissions
| Role    | Permissions                |
|---------|----------------------------|
| ADMIN   | READ, WRITE, DELETE, ADMIN |
| MANAGER | READ, WRITE, DELETE        |
| USER    | READ, WRITE                |

## API Endpoints

### Auth
| Method | Endpoint      | Description                    | Auth |
|--------|---------------|--------------------------------|------|
| POST   | /auth/login   | Login, get access+refresh token | No  |
| POST   | /auth/refresh | Refresh access token           | No   |
| POST   | /auth/logout  | Logout, revoke token           | Yes  |

### Users
| Method | Endpoint    | Description     | Role           |
|--------|-------------|-----------------|----------------|
| POST   | /users      | Create user     | ADMIN          |
| GET    | /users      | Get all users   | ADMIN, MANAGER |
| GET    | /users/{id} | Get user by ID  | ADMIN, MANAGER |
| PUT    | /users/{id} | Update user     | ADMIN          |
| DELETE | /users/{id} | Delete user     | ADMIN          |

### Admin
| Method | Endpoint                     | Description          | Role  |
|--------|------------------------------|----------------------|-------|
| GET    | /admin/dashboard             | Admin dashboard      | ADMIN |
| GET    | /admin/audit-logs            | All audit logs       | ADMIN |
| GET    | /admin/audit-logs/{username} | Logs by username     | ADMIN |

## Setup Instructions

### Step 1: Create Database
Open MySQL Workbench and run:
CREATE DATABASE oauth_jwt_db;

### Step 2: Update application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/oauth_jwt_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password

### Step 3: Run Application
.\mvnw.cmd spring-boot:run

### Step 4: Swagger UI
http://localhost:8080/swagger-ui/index.html

### Step 5: Test Login
POST /auth/login
Body:
{
  "username": "admin",
  "password": "admin123",
  "tenantName": "TenantA"
}
Copy accessToken from response.
Click Authorize in Swagger and enter: Bearer your_access_token

## Security Features
- BCrypt password hashing
- JWT access token: 24 hour expiry
- JWT refresh token: 7 day expiry
- Custom JWT claims: tenant_id, roles, permissions
- Token blacklisting on logout (in-memory)
- Rate limiting: 5 login attempts per IP per minute
- Stateless sessions (no server-side session)
- CSRF disabled for REST API
- SQL injection protection via JPA
- Global exception handling
- Multi-tenancy: isolated users per tenant
- Audit logging: login, logout, IP, timestamp

## Edge Cases Handled
- Expired token: 401
- Revoked token after logout: 401
- Wrong credentials: 401
- No token: 403
- Wrong role: 403
- Duplicate username/email: 400
- User not found: 404
- Rate limit exceeded: 429

## Deliverables
1. Source code
2. Swagger UI: http://localhost:8080/swagger-ui/index.html
3. Postman collection: oauth-jwt-security.postman_collection.json
4. README.md

## Estimated Time
4-6 days for a strong developer with backend experience.