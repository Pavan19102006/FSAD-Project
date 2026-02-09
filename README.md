# Loan Management System

A professional loan management web application built with **Spring Boot 3.4.0** featuring role-based access control for Admin, Lender, Borrower, and Financial Analyst users.

## 🚀 Features

### Role-Based Access Control
- **Admin**: Oversee platform operations, manage user accounts, and ensure data security
- **Lender**: Create loan offers, track payments, and manage borrower interactions
- **Borrower**: Apply for loans, track payment schedules, and manage loan details
- **Financial Analyst**: Analyze loan data, assess risks, and generate financial reports

### Core Functionality
- User authentication with JWT tokens
- Loan creation and management
- Loan application workflow
- Payment schedule generation with amortization
- Payment processing with late fee calculation
- Transaction audit trail
- Dashboard and analytics for each role
- Risk assessment reports

## 🛠️ Technology Stack

| Component | Technology |
|-----------|------------|
| Backend Framework | Spring Boot 3.4.0 |
| Java Version | 21 (LTS) |
| Security | Spring Security 6 with JWT |
| Database | H2 (dev) / PostgreSQL (prod) |
| API Documentation | Swagger / OpenAPI 3.0 |
| Build Tool | Maven |

## 📦 Project Structure

```
loan-management-backend/
├── src/main/java/com/loanmanagement/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST API controllers
│   ├── dto/             # Data Transfer Objects
│   ├── entity/          # JPA Entities
│   ├── exception/       # Custom exceptions
│   ├── repository/      # JPA Repositories
│   ├── security/        # JWT & Security components
│   └── service/         # Business logic
└── src/main/resources/
    └── application.yml  # Application configuration
```

## 🏃 Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.9+

### Running the Application

1. Navigate to the backend directory:
   ```bash
   cd loan-management-backend
   ```

2. Build the project:
   ```bash
   ./mvnw clean install
   ```

3. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

4. Access the application:
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console: http://localhost:8080/h2-console

## 🔐 Default Users

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@loanmanagement.com | admin123 |
| Lender | lender@loanmanagement.com | lender123 |
| Borrower | borrower@loanmanagement.com | borrower123 |
| Analyst | analyst@loanmanagement.com | analyst123 |

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### Admin APIs
- `GET /api/admin/dashboard` - Platform statistics
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}` - Update user
- `DELETE /api/admin/users/{id}` - Delete user

### Lender APIs
- `POST /api/lender/loans` - Create loan offer
- `GET /api/lender/loans` - List lender's loans
- `GET /api/lender/applications` - View pending applications
- `POST /api/lender/applications/{id}/approve` - Approve application
- `POST /api/lender/applications/{id}/reject` - Reject application

### Borrower APIs
- `POST /api/borrower/applications` - Apply for loan
- `GET /api/borrower/loans` - View my loans
- `GET /api/borrower/loans/{id}/schedule` - Payment schedule
- `POST /api/borrower/payments` - Make payment

### Analyst APIs
- `GET /api/analyst/reports/loans` - Loan analytics
- `GET /api/analyst/reports/risk` - Risk assessment
- `GET /api/analyst/reports/payments` - Payment analytics

## 🧪 Testing

Run tests:
```bash
./mvnw test
```

## 📄 License

This project is licensed under the MIT License.
