Objective: 
Develop a REST API with multiple resources: User, Account, and Transaction.

Key Resources

1. User: Create, Read (Get), Update, Delete (CRUD operations)My Springboot Java project has three resources, "user", "accounts" and associated "transactions". 
Generate springboot JAVA controllers and supporting classes for REST APIs for a given open api yaml file

2. Account: Create, Read, Update, Delete (CRUD operations)

3. Transaction: Create, Read, Fetch transaction history, and other operations tied to an Account.

Key Considerations
1. URL Structure to follow URI versioning pattern eg: /v1/users
2. We will use REST API and HTTP Methods to design the APIs eg: POST for create, PATCH for update.
3. We need an authentication mechanism to protect all APIs except creating of User and authenticating a user.
4. We will add a filter which will by default expect a JWT token to extract userId to ensure only authneticated user is able to make requests. There will be a configurable exception resource paths.


Steps Taken
1. Create Data Model to store as entity as per openApi.yaml
2. Create Request Objects as per openApi.yaml
3. Create Controllers for the required operations using request objects and returning data models.
4. Controllers will interact with Repository to persist data in memory.
5. Create AuthenticateController to do standalone activity for authenticating users and returning JWT. For the purpose of demo this will only authenticate users with userID as userId-1 / userId-2.
6. Create a Filter that runs for every request and verifies that the incoming request if is authenticated then is appropriate for the user id present in the JWT token if the token is valid.
7. Create a custom access check component that ensures the requested resource is available to the authenticated user.


In Memory Database Portal
http://localhost:8080/h2-console/login.jsp
JDBC URL jdbc:h2:mem:testdb

Key Endpoints

1. **User APIs**
   - **Create User**
     - `POST /v1/users`
     - Request Body:
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
   - **Fetch User by ID**
     - `GET /v1/users/{userId}`
   - **Update User by ID**
     - `PATCH /v1/users/{userId}`
     - Request Body:
       ```json
       {
         "name": "Updated Name",
         "address": {
           "line1": "456 New St",
           "line2": "",
           "line3": "",
           "town": "Manchester",
           "county": "Greater Manchester",
           "postcode": "M1 2AB"
         },
         "phoneNumber": "+441234567891",
         "email": "updated@example.com"
       }
       ```
   - **Delete User by ID**
     - `DELETE /v1/users/{userId}`

2. **Account APIs**
   - **Create Account**
     - `POST /v1/accounts`
     - Request Body:
       ```json
       {
         "name": "Personal Bank Account",
         "accountType": "personal"
       }
       ```
   - **List Accounts**
     - `GET /v1/accounts`
   - **Fetch Account by Account Number**
     - `GET /v1/accounts/{accountNumber}`
   - **Update Account by Account Number**
     - `PATCH /v1/accounts/{accountNumber}`
     - Request Body:
       ```json
       {
         "name": "Updated Account Name",
         "accountType": "personal"
       }
       ```
   - **Delete Account by Account Number**
     - `DELETE /v1/accounts/{accountNumber}`

3. **Transaction APIs**
   - **Create Transaction**
     - `POST /v1/accounts/{accountNumber}/transactions`
     - Request Body:
       ```json
       {
         "amount": 100.00,
         "currency": "GBP",
         "type": "deposit",
         "reference": "Initial deposit"
       }
       ```
   - **List Transactions for an Account**
     - `GET /v1/accounts/{accountNumber}/transactions`
   - **Fetch Transaction by ID**
     - `GET /v1/accounts/{accountNumber}/transactions/{transactionId}`

4. **Authentication API**
   - **Authenticate User**
     - `POST /v1/authenticate`
     - Request Body:
       ```json
       {
         "username": "userId-1"
       }
       ```



Requirements Points for discussion
1. Handle UserId differently rather forming them as part of request.
2. Error Messages and status code need to be made vague rather making them obvious for not found and forbidden criteria.
3. when validation fails use error codes rather error messages for field names.
4. observability tools
5. authenticate endpoint for non existing users should return 401 or 403?
6. if account number can only belong to authenticated user then why do we even
need it in the URL we can remove it and ensure transaction id belong to the user.



Testing
Sc 1 - Create a new User
Sc 2 - Create User should have validations, status 400
Sc 3 - Auth an existing user, Get that users details, details should come back.
Sc 4 - Auth an existing user, Get another existing user details should fail with 403.
Sc 5 - Auth an existing user, Get non-existing user details, should fail with not found 404
Sc 6 - Auth an existing user, update that users details. Details come back and success.
Sc 7- Auth an existing user, update another existing users details. should fail with 403.
Sc 8- Auth an existing user, update non existing users details. should come back with 404
Sc 9 - Auth an existing user, delete that users details. no content success.
Sc 10- Auth an existing user, delete another existing users details. should fail with 403.
Sc 11- Auth an existing user, delete non existing users details. should come back with 404
Sc 12 - Auth an existing user with account, delete that existing user, should come back with 409

Accounts 
Sc 13 - Auth an existing user, create a new account.
Sc 14 - Auth an existing user, create a new account with invalid details, validations should kick in with status 400.
Sc 15 - Auth an existing user, list all accounts. should come back with accounts if there are accounts.
Sc 16 - Auth an existing user, fetch account for that user by account number. Success
Sc 17 - Auth an existing user, fetch existing account for another user. Should fail with 403.
Sc 18 - Auth an existing user, fetch non-existing account. Should fail with 404.
Sc 19 - Auth an existing user, update account for that user by account number. Success
Sc 20 - Auth an existing user, update existing account for another user. Should fail with 403.
Sc 21 - Auth an existing user, update non-existing account. Should fail with 404.
Sc 22 - Auth an existing user, delete account for that user by account number. Success
Sc 23 - Auth an existing user, delete existing account for another user. Should fail with 403.
Sc 24 - Auth an existing user, delete non-existing account. Should fail with 404.



test2 1c3ad5b1-8640-4973-85d8-d28db21ebd91