# TestForge

TestForge is a machine-test application built with **Java, Spring Boot, Spring Cloud Gateway, PostgreSQL, and Angular**. It provides user registration with OTP verification, JWT-based authentication, role-based access control, and protected product-management APIs.

> **Project status:** The Maven multi-module build and the currently configured automated tests have passed locally. Run the verification steps below in your environment before submitting.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Database Setup](#database-setup)
- [Configuration](#configuration)
- [Build and Test](#build-and-test)
- [Run the Application](#run-the-application)
- [API Documentation](#api-documentation)
- [API Overview](#api-overview)
- [Authentication Flow](#authentication-flow)
- [Testing with Postman](#testing-with-postman)
- [Troubleshooting](#troubleshooting)
- [Security Notes](#security-notes)

## Features

### Authentication and authorization
- User signup with request validation.
- OTP verification to complete registration.
- OTP expiration and a limit on failed verification attempts.
- Password hashing using Spring Security's `PasswordEncoder`.
- JWT-based login and access to protected endpoints.
- Logout through JWT revocation.
- Role-based access to the administrator Settings endpoint.
- Country-code restrictions for signup, configured by the backend.

### Product management
- Product create, read, update, and delete operations.
- Request validation and structured error responses.
- Pagination, sorting, and search/filter support where exposed by the product API.
- Protected product endpoints.

### Developer experience
- Maven multi-module build.
- Springdoc OpenAPI / Swagger UI for the services.
- Unit tests for authentication, JWT, OTP, and product business logic.

## Architecture

```text
Angular Frontend (http://localhost:4200)
                 |
                 v
       API Gateway (port 8080)
          /               \
         v                 v
 Auth Service          Product Service
   port 8081              port 8082
         |                 |
         +--------+--------+
                  |
             PostgreSQL
```

The API Gateway routes authentication and user endpoints to Auth Service and product endpoints to Product Service. Both backend services should be configured with the same JWT signing secret so tokens issued by Auth Service can be validated by Product Service.

## Technology Stack

| Area | Technology |
|---|---|
| Language | Java |
| Backend framework | Spring Boot |
| Service routing | Spring Cloud Gateway |
| Authentication | Spring Security, JWT |
| Password storage | Password hashing through `PasswordEncoder` |
| Database | PostgreSQL |
| API documentation | Springdoc OpenAPI / Swagger UI |
| Frontend | Angular |
| Build | Maven |
| Testing | JUnit and the test libraries configured in the Maven modules |

Use the Java version configured in the project POMs. Java 21 is recommended if that is the JDK configured for this checkout.

## Prerequisites

Install and configure:

- JDK compatible with the project POMs (Java 21 recommended).
- Apache Maven.
- PostgreSQL.
- Node.js and npm compatible with the Angular project.
- Angular CLI (optional if the project scripts use the local CLI).
- Git.
- An IDE such as Eclipse or IntelliJ IDEA.

## Database Setup

Create a PostgreSQL database for TestForge. For example, using `psql` or pgAdmin:

```sql
CREATE DATABASE testforge;
```

If you use a dedicated database account, create it and grant it appropriate privileges. Replace the example values below with your own local credentials; do not commit real passwords to source control.

```sql
CREATE USER testforge_user WITH PASSWORD 'replace-with-a-local-password';
GRANT ALL PRIVILEGES ON DATABASE testforge TO testforge_user;
```

Depending on your PostgreSQL version and the schema permissions used by the application, you may also need to grant privileges on the `public` schema after connecting to the `testforge` database:

```sql
GRANT USAGE, CREATE ON SCHEMA public TO testforge_user;
```

If you already have a working PostgreSQL user, you can use that account instead.

## Configuration

The backend reads environment variables for database credentials and the JWT secret. Configure these variables for **each backend service** in your IDE's Run Configuration or in the terminal environment used to launch it.

| Variable | Example / purpose |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/testforge` |
| `DB_USERNAME` | Your PostgreSQL username |
| `DB_PASSWORD` | Your PostgreSQL password |
| `JWT_SECRET` | A private, sufficiently long random signing secret |
| `AUTH_SERVICE_URL` | Optional gateway override; default `http://localhost:8081` |
| `PRODUCT_SERVICE_URL` | Optional gateway override; default `http://localhost:8082` |

The application may also use service-specific configuration such as the blocked-country list and JWT expiration. Keep those values aligned with the relevant `application.yml` files.

### Eclipse setup

1. In **Package Explorer**, locate the Auth Service application class.
2. Open **Run As → Run Configurations…**.
3. Select the Spring Boot application configuration.
4. Open the **Environment** tab and add `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET`.
5. Repeat for Product Service, using the same database configuration and the **same JWT secret**.
6. Start the API Gateway with the appropriate environment variables if its configuration requires any.
7. Do not put actual credentials into `application.yml`, the README, or committed `.env` files.

Generate a private random secret for local development and keep it out of Git. Do not reuse the sample values shown in documentation or commit a real secret.

## Build and Test

Run these commands from the repository root, where the parent `pom.xml` is located.

Build all Maven modules:

```bash
mvn clean package
```

Run all configured tests:

```bash
mvn clean test
```

If you only want to test a single backend module:

```bash
mvn -pl auth-service test
mvn -pl product-service test
```

A successful Maven reactor should report `SUCCESS` for the parent project, `auth-service`, `product-service`, and `api-gateway`.

## Run the Application

Start PostgreSQL first and ensure the environment variables are configured.

Start the services in separate terminals or IDE run configurations, in this order:

1. **Auth Service** — port `8081`.
2. **Product Service** — port `8082`.
3. **API Gateway** — port `8080`.
4. **Angular frontend** — port `4200`.

For each Spring Boot service, run from its module directory:

```bash
mvn spring-boot:run
```

If the root project is configured as a parent-only POM, run that command from each executable service module rather than from the repository root.

For Angular, open the frontend directory (the directory containing `package.json`), install dependencies, and start the development server using the project's scripts:

```bash
npm install
npm start
```

If the project does not define an `npm start` script, use the script listed in `package.json` (for example, `ng serve`). Keep the frontend API base URL aligned with the gateway URL.

## API Documentation

With the services running, open:

- **Auth Service Swagger UI:** http://localhost:8081/swagger-ui/index.html
- **Product Service Swagger UI:** http://localhost:8082/swagger-ui/index.html
- **Auth Service OpenAPI JSON:** http://localhost:8081/v3/api-docs
- **Product Service OpenAPI JSON:** http://localhost:8082/v3/api-docs

The API Gateway is configured for the application APIs on port `8080`. The direct service URLs above are useful for service-level debugging and Swagger.

## API Overview

The API Gateway routes these endpoint groups:

| Method / path | Purpose | Access |
|---|---|---|
| `POST /api/auth/signup` | Submit registration details and initiate OTP verification | Public |
| `POST /api/auth/verify-otp` | Verify the registration OTP | Public |
| `POST /api/auth/login` | Authenticate credentials and receive a JWT | Public |
| `POST /api/auth/logout` | Revoke the supplied JWT | Authenticated |
| `GET /api/dashboard` | Return the authenticated user's welcome message | Authenticated |
| `GET /api/settings` | Administrator-only settings endpoint | Administrator |
| `/api/products/**` | Product management endpoints | Authenticated, subject to configured authorization |

Check the controllers and Swagger documentation for exact request schemas, response bodies, product subpaths, and query parameter names. Send the JWT in the HTTP `Authorization` header for protected endpoints:

```http
Authorization: Bearer <access-token>
```

## Authentication Flow

1. Submit signup details to `POST /api/auth/signup`.
2. Retrieve the development OTP from the Auth Service console when running locally.
3. Submit the email and OTP to `POST /api/auth/verify-otp`.
4. Sign in through `POST /api/auth/login`.
5. Copy the returned JWT.
6. Send the token as `Authorization: Bearer <access-token>` when calling protected endpoints.
7. Call `POST /api/auth/logout` with the token to revoke it. Requests using a revoked token should be rejected.

**OTP note:** Printing an OTP to the service console is suitable only for local development or a controlled machine-test demonstration. A deployed application should deliver OTPs through a configured email/SMS provider and should not expose them in logs.

## Testing with Postman

Suggested end-to-end sequence:

1. Signup with a valid username, email, password, role, and allowed country code.
2. Read the development OTP from the Auth Service console.
3. Verify the OTP.
4. Login with the newly registered account.
5. Save the returned token in a Postman environment variable, for example `accessToken`.
6. For protected requests, configure the Authorization tab as **Bearer Token** and use `{{accessToken}}`.
7. Verify that a Standard User receives `403 Forbidden` when accessing Settings.
8. Sign in as an Administrator and verify access to Settings.
9. Create, list, update, and delete products.
10. Logout and confirm that the revoked token cannot be reused.

Expected security checks include invalid credentials, missing/invalid JWTs, expired/reused tokens, invalid input, incorrect/expired OTPs, and prohibited country codes.

## Troubleshooting

### PostgreSQL connection failure
- Confirm PostgreSQL is running.
- Verify `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`.
- Confirm the `testforge` database exists and the configured user has database/schema privileges.

### JWT rejected by Product Service
- Confirm Auth Service and Product Service use the same `JWT_SECRET`.
- Ensure the token has not expired or been revoked.
- Check that the request uses `Authorization: Bearer <token>`.

### Swagger UI returns 401/403
- Confirm the correct service and port are being used.
- Check that Swagger/OpenAPI paths are permitted by that service's security configuration and excluded from custom JWT filtering.
- Rebuild and restart the affected service after changing its configuration.

### Gateway returns 503
- Confirm Auth Service is running on port `8081` and Product Service on port `8082`, or update `AUTH_SERVICE_URL` / `PRODUCT_SERVICE_URL`.
- Check service startup logs for database or configuration errors.

### Maven tests fail
Run the failing module's tests separately and inspect the Surefire reports under that module's `target/surefire-reports` directory.

## Security Notes

- Never commit database passwords, JWT signing secrets, real access tokens, or private environment files.
- Use hashed passwords; never store plaintext passwords.
- Use HTTPS in deployed environments.
- Keep OTPs out of production logs and deliver them through an appropriate provider.
- Keep JWT lifetimes short enough for the application's risk profile and ensure revocation is enforced wherever tokens are accepted.
- Restrict administrator-only operations on the server, not only in the Angular UI.
- Review all environment-specific settings before deploying outside local development.

## License

No license has been specified. Unless a license is added to the repository, rights to reuse and redistribute the code remain subject to applicable copyright law.
