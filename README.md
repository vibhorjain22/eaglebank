# Eagle Bank API

Spring Boot Server

## Overview

This repository implements a RESTful API for Eagle Bank, generated from an OpenAPI specification and built with Spring Boot.  
It provides endpoints for managing Users, Accounts, and Transactions, enforcing authentication and authorization using JWT.

### Purpose

The purpose of this repo is to provide a secure, standards-based backend for a digital banking platform.  
It supports:

- **User management**: Register, authenticate, update, and delete users.
- **Account management**: Create, view, update, and delete bank accounts for authenticated users.
- **Transaction management**: (If implemented) Record and fetch transactions for accounts.

### How to Start

1. **Build the project**  
   From the project root, run:
   ```sh
   ./gradlew build
   ```

2. **Run the server**  
   ```sh
   ./gradlew bootRun
   ```
   The server will start on [http://localhost:8080](http://localhost:8080) by default.

3. **API Documentation**  
   - OpenAPI spec: [http://localhost:8080/v3/api-docs/](http://localhost:8080/v3/api-docs/)
   - Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

4. **Change default port**  
   Edit `src/main/resources/application.properties` and set:
   ```
   server.port=YOUR_PORT
   ```

### High-Level Request Flow

#### User Resource

- **Register**:  
  `POST /v1/users`  
  Creates a new user. Validates input and ensures unique email.

- **Authenticate**:  
  `POST /v1/authenticate`  
  Returns a JWT token for valid credentials.

- **Get/Update/Delete User**:  
  `GET|PATCH|DELETE /v1/users/{userId}`  
  Requires JWT. Only the user themselves can access or modify their data.

#### Account Resource

- **Create Account**:  
  `POST /v1/accounts`  
  Requires JWT. Creates a new account for the authenticated user. Account number is auto-generated and unique.

- **Get/List Accounts**:  
  `GET /v1/accounts/{accountNumber}`  
  `GET /v1/accounts`  
  Requires JWT. Only the account owner can access their accounts.

- **Update/Delete Account**:  
  `PATCH|DELETE /v1/accounts/{accountNumber}`  
  Requires JWT. Only the account owner can update or delete.

#### Transaction Resource

- **(If implemented)**  
  `POST /v1/accounts/{accountNumber}/transactions`  
  `GET /v1/accounts/{accountNumber}/transactions`  
  Requires JWT. Only the account owner can create or view transactions.

### Security

- All endpoints (except registration and authentication) require a valid JWT token.
- Authorization checks ensure users can only access their own resources.
- Attempts to access or modify another user's data return `403 Forbidden`.

---

**For more details, see the OpenAPI documentation or Swagger UI.**