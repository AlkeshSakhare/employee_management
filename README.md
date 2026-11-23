# Employee Management REST API

A Spring Boot REST API for managing employee records using Java, Spring Data JPA, Hibernate, and MariaDB.

## Tech Stack

- **Java:** 21
- **Spring Boot:** 4.2.0
- **Build tool:** Maven
- **Database:** MariaDB 10.11.x (tested locally through XAMPP)
- **Persistence:** Spring Data JPA / Hibernate
- **Validation:** Jakarta Bean Validation
- **API testing:** Postman
- **IDE:** IntelliJ IDEA

## Features

### Employee management

- Create, retrieve, update, partially update, and delete employees.
- Use request and response DTOs to separate API payloads from JPA entities.
- Validate incoming data using Jakarta Bean Validation.
- Prevent duplicate employee email addresses.

### Search and filtering

- Search employees by partial name, case-insensitively.
- Filter by department, case-insensitively.
- Filter by inclusive salary range.
- Combine optional filters dynamically using JPA Specifications.

### Pagination and sorting

- Paginate employee results.
- Sort by supported fields: `id`, `name`, `email`, `department`, and `salary`.
- Combine pagination and sorting with salary-range and dynamic filtering.

### Error handling

- Centralized exception handling using `@RestControllerAdvice`.
- Handled errors include validation failures, missing employees, duplicate emails, invalid parameters, and other request errors.

### Request tracing

- Require a client-provided `X-UUID` header containing a canonical UUID.
- Reject missing or invalid `X-UUID` values with HTTP 400.
- Generate a separate server-side `X-Request-Id` for each request.
- Return both identifiers in the response headers.
- Store both identifiers in SLF4J MDC so they appear in application logs.
- Clear MDC values after request processing.

## Project Structure

```text
employee-management/
├── src/
│   ├── main/
│   │   ├── java/com/employee/employee_management/
│   │   │   ├── EmployeeManagementApplication.java
│   │   │   ├── controller/
│   │   │   │   └── EmployeeController.java
│   │   │   ├── dto/
│   │   │   │   ├── EmployeeRequestDTO.java
│   │   │   │   ├── EmployeeResponseDTO.java
│   │   │   │   └── EmployeePatchDTO.java
│   │   │   ├── entity/
│   │   │   │   └── Employee.java
│   │   │   ├── exception/
│   │   │   │   ├── EmployeeNotFoundException.java
│   │   │   │   ├── DuplicateEmailException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   ├── filter/
│   │   │   │   └── RequestIdFilter.java
│   │   │   ├── mapper/
│   │   │   │   └── EmployeeMapper.java
│   │   │   ├── repository/
│   │   │   │   └── EmployeeRepository.java
│   │   │   ├── service/
│   │   │   │   └── EmployeeService.java
│   │   │   └── specification/
│   │   │       └── EmployeeSpecification.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
└── pom.xml
```

## Prerequisites

1. JDK 21.
2. Maven (or the Maven Wrapper, if included in the project).
3. XAMPP with MariaDB running, or another compatible MariaDB/MySQL server.
4. Postman or another HTTP client.

## Database Setup

Start MySQL/MariaDB from XAMPP and create the database:

```sql
CREATE DATABASE employee_management;
```

Configure the connection in `src/main/resources/application.properties`:

```properties
spring.application.name=employee-management
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/employee_management
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MariaDBDialect

server.error.include-stacktrace=never

logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.orm.jdbc.bind=TRACE

logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [UUID: %X{userUUID}] [Request-ID: %X{requestId}] %logger{36} - %msg%n
```

Adjust the JDBC URL, username, and password to match your local database configuration.

The application uses the `employees` table. The email column should have a database-level unique constraint as well as entity-level validation. If it is not already present, and existing data contains no duplicate emails, create it with:

```sql
ALTER TABLE employees
ADD CONSTRAINT uk_employees_email UNIQUE (email);
```

> **Development configuration:** `spring.jpa.hibernate.ddl-auto=update` is convenient locally. For production, use a database migration tool such as Flyway or Liquibase and review schema changes explicitly. SQL bind logging at `TRACE` can expose data values; keep it disabled in production and avoid logging personal information.

## Run the Application

From the project root:

```bash
mvn spring-boot:run
```

Alternatively, run `EmployeeManagementApplication.java` from IntelliJ IDEA.

The API is available at:

```text
http://localhost:8080
```

## API Endpoints

Base path: `/api/employees`

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/employees` | Retrieve all employees |
| GET | `/api/employees/{id}` | Retrieve an employee by ID |
| POST | `/api/employees` | Create an employee |
| PUT | `/api/employees/{id}` | Replace/update all employee fields |
| PATCH | `/api/employees/{id}` | Update only supplied employee fields |
| DELETE | `/api/employees/{id}` | Delete an employee |
| GET | `/api/employees/pagination` | Retrieve employees with pagination |
| GET | `/api/employees/sorting` | Retrieve employees with sorting |
| GET | `/api/employees/page-sort` | Retrieve employees with pagination and sorting |
| GET | `/api/employees/search` | Search by employee name |
| GET | `/api/employees/department` | Filter by department |
| GET | `/api/employees/salary-range` | Filter by salary range |
| GET | `/api/employees/salary-range/pagination` | Salary filter with pagination and sorting |
| GET | `/api/employees/filter` | Combine optional filters with pagination and sorting |

### Employee JSON example

Use this request body for `POST` and `PUT`:

```json
{
  "name": "Alex Johnson",
  "email": "alex.johnson@example.com",
  "department": "QA",
  "salary": 75000
}
```

Example response:

```json
{
  "id": 1,
  "name": "Alex Johnson",
  "email": "alex.johnson@example.com",
  "department": "QA",
  "salary": 75000
}
```

The exact response fields follow the current DTO definitions.

### Pagination

```http
GET http://localhost:8080/api/employees/pagination?page=0&size=5
```

- `page` is zero-based.
- `size` controls the number of records per page.
- The current service validation allows page sizes from 1 to 100.

### Sorting

```http
GET http://localhost:8080/api/employees/sorting?sortBy=salary&direction=desc
```

Supported sort fields: `id`, `name`, `email`, `department`, and `salary`.

Supported directions: `asc` and `desc`.

### Name search

```http
GET http://localhost:8080/api/employees/search?name=alex
```

The search uses partial, case-insensitive matching.

### Department filter

```http
GET http://localhost:8080/api/employees/department?department=QA
```

The department match is exact and case-insensitive.

### Salary range

```http
GET http://localhost:8080/api/employees/salary-range?minSalary=50000&maxSalary=100000
```

The salary range includes both boundaries.

### Salary range with pagination and sorting

```http
GET http://localhost:8080/api/employees/salary-range/pagination?minSalary=50000&maxSalary=100000&page=0&size=5&sortBy=salary&direction=desc
```

### Dynamic filtering

All filter fields are optional, but at least one filter must be supplied. Filters are combined using `AND`.

```http
GET http://localhost:8080/api/employees/filter?name=alex&department=QA&minSalary=50000&maxSalary=100000&page=0&size=5&sortBy=salary&direction=desc
```

Supported filters:

- `name`: partial, case-insensitive match.
- `department`: exact, case-insensitive match.
- `minSalary` and `maxSalary`: must be supplied together; inclusive range.

Pagination and sorting parameters:

- `page` (default `0`)
- `size` (default `5`)
- `sortBy` (default `name`)
- `direction` (default `asc`)

## Request ID Headers

Every API request must include a valid UUID in the `X-UUID` request header.

Example request:

```http
GET http://localhost:8080/api/employees
X-UUID: 0643bed0-6b2c-41de-b7b3-1761d3754915
```

The API returns two response headers:

| Header | Meaning |
|---|---|
| `X-UUID` | Echoes the UUID supplied by the client |
| `X-Request-Id` | A new UUID generated by the API for this request |

If `X-UUID` is missing or invalid, the filter rejects the request with HTTP 400 Bad Request.

The filter places both values in MDC. The configured console logging pattern displays them, making it easier to trace application log entries for a request.

## Error Handling

The application centralizes handled exceptions through `GlobalExceptionHandler`.

| HTTP status | Meaning |
|---|---|
| 200 OK | Request completed successfully |
| 201 Created | Employee created |
| 204 No Content | Employee deleted |
| 400 Bad Request | Invalid input, missing required parameter, invalid UUID, or invalid pagination/sorting value |
| 404 Not Found | Employee or requested search result not found |
| 409 Conflict | Duplicate email address |

Actual response fields and messages are defined by the exception handler.

## Testing with Postman

1. Start MariaDB from XAMPP.
2. Run the Spring Boot application.
3. Create a Postman request to `http://localhost:8080`.
4. For every request, add the `X-UUID` header with a valid UUID.
5. For POST, PUT, and PATCH requests, select **Body → raw → JSON** and provide the appropriate DTO fields.
6. Check the response status, body, and the `X-UUID` and `X-Request-Id` response headers.
7. Check the IntelliJ console to confirm both IDs appear in log entries.

Generate a fresh UUID for each request when testing. A UUID can be generated using Java's `UUID.randomUUID()`.

## Current Scope and Future Work

The current implementation covers employee management, validation, search, pagination, sorting, exception handling, and request tracing.

Planned next enhancement:

- Spring Security
- User registration and login
- BCrypt password hashing
- JWT authentication
- ADMIN and USER roles
- Role-based authorization for employee endpoints

These security features are planned and are not part of the current implementation yet.

## Notes

- Keep database credentials out of source control in shared or production environments.
- Do not commit real employee data, passwords, tokens, or other secrets.
- Do not log unnecessary personal information.
- Review validation, database constraints, and error responses before deploying outside local development.
