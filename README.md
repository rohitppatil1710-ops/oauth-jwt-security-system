Enterprise-Grade OAuth2 & JWT Security System

Project Overview

A production-grade authentication and authorization system built using Spring Boot, Spring Security, JWT, MySQL, and Redis.

The system provides secure authentication, role-based access control (RBAC), permission-based authorization, refresh token support, multi-tenancy, rate limiting, audit logging, and token revocation.

---

Architecture Diagram

                    +-------------------+
                    |      Client       |
                    | Postman/Swagger   |
                    +---------+---------+
                              |
                              | HTTPS
                              v
                 +------------------------+
                 | Spring Boot Application|
                 +-----------+------------+
                             |
      ------------------------------------------------
      |                      |                       |
      v                      v                       v

+-------------+     +----------------+     +----------------+
| JWT Filter  |     | Rate Limiter   |     | Audit Logging  |
+-------------+     +----------------+     +----------------+

                             |
                             v

                 +------------------------+
                 |      Auth Service      |
                 +-----------+------------+
                             |
               ---------------------------
               |                         |
               v                         v

      +----------------+      +------------------+
      | MySQL Database |      | Redis Blacklist  |
      +----------------+      +------------------+

---

Technology Stack

Technology| Version
Java| 17
Spring Boot| 3.3.5
Spring Security| 6
MySQL| 8
Redis (Memurai)| Latest
JWT (jjwt)| 0.11.5
Spring Data JPA| Latest
Swagger OpenAPI| 2.3.0
Maven| 3.x

---

Features

Authentication & Authorization

- JWT Access Token Authentication
- Refresh Token Support
- Stateless Authentication
- Role-Based Access Control (RBAC)
- Permission-Based Authorization
- Custom JWT Claims

Multi-Tenancy

Each user belongs to a specific tenant.

JWT contains:

Claim| Description
tenant_id| Tenant Identifier
roles| User Roles
permissions| User Permissions

Security Features

- BCrypt Password Encryption
- JWT Validation
- Redis Token Revocation
- HTTPS Support
- Secure Headers
- Global Exception Handling
- SQL Injection Protection using JPA
- Stateless Session Management

Rate Limiting

Feature| Value
Login Attempts| 5
Window| 1 Minute
Scope| Per IP Address

Audit Logging

Tracks:

- Login Events
- Logout Events
- Failed Login Attempts
- IP Address
- Timestamp
- Success/Failure Status

---

Project Structure

src/main/java/com/security

├── config
├── controller
├── service
├── repository
├── entity
├── dto
├── filter
├── exception
└── util

---

Database Design

Tables

Table Name| Purpose
users| User Information
roles| System Roles
permissions| Permissions
user_roles| User Role Mapping
role_permissions| Role Permission Mapping
tenants| Multi-Tenant Support
refresh_tokens| Refresh Token Storage
audit_logs| Activity Tracking

---

Default Users

Username| Password| Role| Tenant
admin| admin123| ADMIN| TenantA
user1| user123| USER| TenantA
manager1| manager123| MANAGER| TenantB

---

Roles & Permissions

Role| Permissions
ADMIN| READ, WRITE, DELETE, ADMIN
MANAGER| READ, WRITE, DELETE
USER| READ, WRITE

---

API Endpoints

Authentication APIs

Method| Endpoint| Description
POST| /auth/login| Generate Access & Refresh Token
POST| /auth/refresh| Generate New Access Token
POST| /auth/logout| Revoke Token

---

User APIs

Method| Endpoint| Access
POST| /users| ADMIN
GET| /users| ADMIN, MANAGER
GET| /users/{id}| ADMIN, MANAGER
PUT| /users/{id}| ADMIN
DELETE| /users/{id}| ADMIN

---

Admin APIs

Method| Endpoint| Access
GET| /admin/dashboard| ADMIN
GET| /admin/audit-logs| ADMIN
GET| /admin/audit-logs/{username}| ADMIN

---

Setup Guide

Step 1 - Create Database

CREATE DATABASE oauth_jwt_db;

---

Step 2 - Configure Application

spring.datasource.url=jdbc:mysql://localhost:3306/oauth_jwt_db
spring.datasource.username=root
spring.datasource.password=your_password

---

Step 3 - Start Redis

memurai-cli ping

Expected Output:

PONG

---

Step 4 - Build Project

.\mvnw.cmd clean install -DskipTests

---

Step 5 - Run Application

.\mvnw.cmd spring-boot:run

---

Swagger Documentation

HTTP

http://localhost:8080/swagger-ui/index.html

HTTPS

https://localhost:8080/swagger-ui/index.html

---

JWT Configuration

Property| Value
Access Token Expiry| 24 Hours
Refresh Token Expiry| 7 Days
Algorithm| HS256
Claims| tenant_id, roles, permissions

---

Edge Cases Handled

Scenario| Response
Expired Token| 401 Unauthorized
Revoked Token| 401 Unauthorized
Invalid Credentials| 401 Unauthorized
Access Denied| 403 Forbidden
Duplicate Username| 400 Bad Request
User Not Found| 404 Not Found
Rate Limit Exceeded| 429 Too Many Requests

---

Deliverables

✅ Source Code

✅ Swagger Documentation

✅ Postman Collection

✅ Architecture Diagram

✅ README Documentation

---

Author

Rohit Patil

Enterprise OAuth2 JWT Security System