# Loan Management System

A professional, full-stack loan management application built with Spring Boot and React.

## Features

- **Role-Based Access Control**: Admin, Lender, Borrower, and Financial Analyst roles
- **Loan Management**: Create, track, and manage loans throughout their lifecycle
- **Payment Processing**: Automated payment schedules, late fee calculation, and payment tracking
- **Financial Analytics**: Comprehensive dashboards and reports for all user roles
- **JWT Authentication**: Secure, stateless authentication with refresh tokens
- **Swagger API Docs**: Interactive API documentation at `/swagger-ui.html`

## Tech Stack

### Backend
- Spring Boot 3.4.0
- Spring Security with JWT
- Spring Data JPA / Hibernate
- H2 Database (dev) / PostgreSQL (prod)
- Lombok
- Swagger/OpenAPI 3.0

### Frontend
- React 18 with Vite
- React Router DOM
- Axios
- Premium dark theme with glassmorphism

## Prerequisites

- **Java 17 or Java 21.0.2-21.0.5** (⚠️ Java 21.0.9+ has Lombok compatibility issues)
- Maven 3.9+
- Node.js 18+ and npm

> **Known Issue**: Java 21.0.9 and later versions have a Lombok incompatibility. 
> Install Java 17 LTS or use SDKMAN: `sdk install java 17.0.9-tem`

## Quick Start

### Backend
```bash
cd loan-management-backend
mvn spring-boot:run
```
Backend runs at: http://localhost:8080

### Frontend  
```bash
cd loan-management-frontend
npm install
npm run dev
```
Frontend runs at: http://localhost:3000

## Default Users

| Role     | Email                          | Password   |
|----------|--------------------------------|------------|
| Admin    | admin@loanmanagement.com       | admin123   |
| Lender   | lender@loanmanagement.com      | lender123  |
| Borrower | borrower@loanmanagement.com    | borrower123|
| Analyst  | analyst@loanmanagement.com     | analyst123 |

## API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token

### Admin
- `GET /api/admin/dashboard` - Platform overview
- `GET /api/admin/users` - List all users
- `PATCH /api/admin/users/{id}/toggle-status` - Enable/disable user

### Lender
- `POST /api/lender/loans` - Create loan offer
- `GET /api/lender/loans` - View own loans
- `GET /api/lender/applications` - View pending applications
- `POST /api/lender/applications/{id}/approve` - Approve application

### Borrower
- `GET /api/borrower/loan-offers` - Browse available loans
- `POST /api/borrower/applications` - Apply for loan
- `GET /api/borrower/loans` - View own loans
- `POST /api/borrower/payments` - Make payment

### Analyst
- `GET /api/analyst/reports/loans` - Loan analytics
- `GET /api/analyst/reports/risk` - Risk assessment
- `GET /api/analyst/overdue-payments` - Overdue payment report

## Project Structure

```
loan-management-backend/
├── src/main/java/com/loanmanagement/
│   ├── config/          # Security, Swagger, CORS config
│   ├── controller/      # REST API controllers
│   ├── dto/             # Request/Response DTOs
│   ├── entity/          # JPA entities
│   ├── exception/       # Custom exceptions
│   ├── repository/      # Spring Data repositories
│   ├── security/        # JWT authentication
│   └── service/         # Business logic

loan-management-frontend/
├── src/
│   ├── components/      # Reusable React components
│   ├── context/         # Auth context provider
│   ├── pages/           # Page components
│   ├── services/        # API service layer
│   └── index.css        # Global styles
```

## License

MIT
