Objective: 
## Project Approach

### Develop a REST API with Multiple Resources

This project involves building a Spring Boot Java REST API with three main resources: **User**, **Account**, and **Transaction**. The API is generated based on an OpenAPI YAML file.

---

### Key Resources

1. **User**
  - CRUD operations: Create, Read, Update, Delete
2. **Account**
  - CRUD operations: Create, Read, Update, Delete
3. **Transaction**
  - Operations: Create, Read, Fetch transaction history, and other account-related actions

---

### Key Considerations

- **URL Structure:** Use URI versioning (e.g., `/v1/users`)
- **HTTP Methods:** Use RESTful conventions (e.g., `POST` for create, `PATCH` for update)
- **Authentication:** All APIs require authentication except user creation and authentication endpoints
- **JWT Filter:** A filter checks for a valid JWT token and extracts the userId, allowing only authenticated users to access resources. Exception paths are configurable.

---

### Design Considerations

1. **Data Model:** Define entities as per `openApi.yaml`
2. **Request Objects:** Create request DTOs as per `openApi.yaml`
3. **Controllers:** Implement REST controllers using request objects and returning data models
4. **Repository:** Controllers interact with an in-memory repository for data persistence
5. **Authentication:** Implement a custom JWT Service to protect non public endpoints to use JWT token.
6. **JWT Filter:** Validate JWT on each request, ensuring resource access matches authenticated user.
7. **Controllers:** Where the customer needs to be authorised to perform an action, add a custom PreAuthorise annotation to help provide forbidden response to unauthorised access.
8. **Validations:** Added validations to prevent bad data being processed.
9. **Security** Preauthorizations
10. **WebMVC** E2E Test

---

### In-Memory Database

- **H2 Console:** [http://localhost:8080/h2-console/login.jsp](http://localhost:8080/h2-console/login.jsp)
- **JDBC URL:** `jdbc:h2:mem:testdb`

---

### Key Endpoints

#### 1. User APIs

- **Create User:** `POST /v1/users`
  ```json
  {
   "name": "Test User",
   "address": {
    "line1": "123 Main St",
    "line2": "Apt 4B",
    "line3": "",
    "town": "London",
    "county": "Greater London",
    "postcode": "E1 6AN"
   },
   "phoneNumber": "+441234567890",
   "email": "test@example.com"
  }
  ```
- **Fetch User by ID:** `GET /v1/users/{userId}`
- **Update User by ID:** `PATCH /v1/users/{userId}`
- **Delete User by ID:** `DELETE /v1/users/{userId}`

#### 2. Account APIs

- **Create Account:** `POST /v1/accounts`
  ```json
  {
   "name": "Personal Bank Account",
   "accountType": "personal"
  }
  ```
- **List Accounts:** `GET /v1/accounts`
- **Fetch Account:** `GET /v1/accounts/{accountNumber}`
- **Update Account:** `PATCH /v1/accounts/{accountNumber}`
- **Delete Account:** `DELETE /v1/accounts/{accountNumber}`

#### 3. Transaction APIs

- **Create Transaction:** `POST /v1/accounts/{accountNumber}/transactions`
  ```json
  {
   "amount": 100.00,
   "currency": "GBP",
   "type": "deposit",
   "reference": "Initial deposit"
  }
  ```
- **List Transactions:** `GET /v1/accounts/{accountNumber}/transactions`
- **Fetch Transaction:** `GET /v1/accounts/{accountNumber}/transactions/{transactionId}`

#### 4. Authentication API

- **Authenticate User:** `POST /v1/authenticate`
  ```json
  {
   "username": "userId-1"
  }
  ```

---

---

### Testing Scenarios

The following scenarios are covered by automated E2E tests:

#### User

- **Sc 1:** Create a new user with valid data. ✅
- **Sc 2:** Validate user creation (400 on invalid). ✅
- **Sc 3:** Authenticate and fetch own details (success). ✅
- **Sc 4:** Authenticate and fetch another user's details (403). ✅
- **Sc 5:** Authenticate and fetch non-existing user (404). ✅
- **Sc 6:** Authenticate and update own details (success). ✅
- **Sc 7:** Authenticate and update another user's details (403). ✅
- **Sc 8:** Authenticate and update non-existing user (404). ✅
- **Sc 9:** Authenticate and delete own details (204). ✅
- **Sc 10:** Authenticate and delete another user's details (403). ✅
- **Sc 11:** Authenticate and delete non-existing user (404). ✅
- **Sc 12:** Delete user with existing account (409). ✅

#### Account

- **Sc 13:** Create a new account. ✅
- **Sc 14:** Validate account creation (400 on invalid). ✅
- **Sc 15:** List all accounts (success). ✅
- **Sc 16:** Fetch own account by account number (success). ✅
- **Sc 17:** Fetch another user's account (403). ✅
- **Sc 18:** Fetch non-existing account (404). ✅
- **Sc 19:** Update own account (success). ✅
- **Sc 20:** Update another user's account (403). ✅
- **Sc 21:** Update non-existing account (404). ✅
- **Sc 22:** Delete own account (success). ✅
- **Sc 23:** Delete another user's account (403). ✅
- **Sc 24:** Delete non-existing account (404). ✅

#### Transactions

- **Sc 25:** Create deposit transaction updates balance and is listed. ✅
- **Sc 26:** Create withdraw transaction with sufficient balance is successful, balance updated, and transaction is listed. ✅
- **Sc 27:** Create withdraw transaction with insufficient balance returns 429, balance not updated, transaction not listed. ✅
- **Sc 28:** Create transaction on someone else's account returns 403. ✅
- **Sc 29:** Create transaction on non-existing account returns 404. ✅
- **Sc 30:** Create transaction with incorrect details returns 400. ✅
- **Sc 31:** List more than 1 transaction for own account. ✅
- **Sc 32:** List transactions for someone else's account returns 403. ✅
- **Sc 33:** List transactions for non-existent account returns 404. ✅
- **Sc 34:** Fetch transaction by ID for own account returns transaction details. ✅
- **Sc 35:** Fetch transaction by ID for someone else's account returns 403. ✅
- **Sc 36:** Fetch transaction by ID for non-existent account returns 404. ✅
- **Sc 37:** Fetch transaction by ID for own account but non-existent transaction id returns 404. ✅
- **Sc 38:** Fetch transaction by ID for own account but transaction id from another account returns 404. ✅

> ✅ indicates there is automated E2E test coverage for this scenario.

### Requirements & Discussion Points

1. Handle `userId` internally, not as part of request.
2. Use vague error messages and status codes for not found/forbidden.
3. On validation failure, return error codes instead of field names
4. Integrate observability tools
5. Decide: Should authentication for non-existing users return 401 or 403?
6. If account numbers are user-specific, consider removing them from URLs
7. Transactions serve statement purpose, we can consider pagination, filtering and ordering in responses.
8. Data Consistency is important and we should consider wrapping "transaction" using thread synchronisation to think about data integrity while updating balance for transactions. 
9. Error Handling is a key element.
10. De Sensitisation of the data.