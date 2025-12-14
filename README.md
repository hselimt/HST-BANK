<div align="center">
  <h1>HST BANK</h1>
  
  <p>
    <b>Bank simulation build with Spring Boot banking REST API and PostgreSQL.</b>
  </p>

https://github.com/user-attachments/assets/2a3c986c-8c76-4fbe-a84e-81ecc0601f37

</div>

## Features

- User registration/login
- Account management (checking/savings)
- Money transfers with ACID transactions
- Card management (credit/debit)
- Transaction history

## Tech Stack

- Java 17
- Spring Boot
- PostgreSQL
- Lombok
- Docker (optional)

## Running Locally

1. Install Java 17 and PostgreSQL
2. Create database: `hstbank`
3. Update `application.properties` with your DB password
4. Run: `./mvnw spring-boot:run`
5. Open: `http://localhost:8080/login.html`

## Test Account

- Email: `admin@hstbank.com`
- Password: `admin123`

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register user |
| POST | /api/auth/login | Login |
| GET | /api/dashboard/user/{id} | Get dashboard |
| POST | /api/accounts | Create account |
| POST | /api/cards | Create card |
| POST | /api/transfers | Transfer money |
| GET | /api/transfers/history/{accountId} | Transaction history |
