<div align="center">
  <h1>HST BANK</h1>
  
  <p>
    <b>Bank simulation build with Spring Boot banking REST API and PostgreSQL.</b>
  </p>

https://github.com/user-attachments/assets/2a3c986c-8c76-4fbe-a84e-81ecc0601f37

## Architecture
```mermaid
classDiagram

    %% ============ APPLICATION ENTRY ============
    class HstBankApiApplication {
        +main(String[] args)$ void
    }

    class AddTestValues {
        -UserService userService
        -AccountService accountService
        +run(String... args) void
    }

    %% ============ CONTROLLER LAYER ============
    class AuthController {
        -UserService userService
        +register(RegisterRequest) ResponseEntity~UserResponse~
        +login(LoginRequest) ResponseEntity~UserResponse~
    }

    class AccountController {
        -AccountService accountService
        -UserService userService
        +createAccount(CreateAccountRequest) ResponseEntity
    }

    class CardController {
        -CardService cardService
        +createCard(CardRequest) ResponseEntity
    }

    class TransferController {
        -TransactionService transactionService
        +transfer(TransferRequest) ResponseEntity
        +getTransactionHistory(Long) ResponseEntity~List~TransactionResponse~~
        +getTransaction(Long) ResponseEntity~TransactionResponse~
    }

    class DashboardController {
        -DashboardService dashboardService
        +getDashboard(Long) ResponseEntity~DashboardResponse~
    }

    %% ============ SERVICE LAYER ============
    class UserService {
        -UserRepository userRepository
        +createUser(String, String, String, String) User
        -regValidation(String, String, String, String) void
        +findByEmail(String) Optional~User~
        +findById(Long) Optional~User~
        +getAllUsers() List~User~
    }

    class AccountService {
        -AccountRepository accountRepository
        +createAccount(User, AccountType, BigDecimal, String) Account
        +deposit(Account, BigDecimal) Account
        +withdraw(Account, BigDecimal) Account
        +getAccountById(Long) Optional~Account~
        +getAccountByAccountNumber(String) Optional~Account~
        +getAccountsByUser(User) List~Account~
    }

    class CardService {
        -CardRepository cardRepository
        -UserRepository userRepository
        -AccountRepository accountRepository
        +createCard(Long, CardRequest) Card
        +getUserCards(Long) List~Card~
        +deleteCard(Long) void
    }

    class TransactionService {
        -TransactionRepository transactionRepository
        -AccountService accountService
        +transfer(Long, Long, BigDecimal, String) Transaction
        +getTransactionHistory(Long) List~Transaction~
        +getTransactionById(Long) Optional~Transaction~
    }

    class DashboardService {
        -UserRepository userRepository
        -AccountRepository accountRepository
        -CardRepository cardRepository
        +getUserDashboard(Long) DashboardResponse
    }

    %% ============ REPOSITORY LAYER ============
    class JpaRepository~T, ID~ {
        <<interface>>
        +save(T entity) T
        +findById(ID id) Optional~T~
        +findAll() List~T~
        +deleteById(ID id) void
        +existsById(ID id) boolean
    }

    class UserRepository {
        <<interface>>
        +findByEmail(String) Optional~User~
    }

    class AccountRepository {
        <<interface>>
        +findByUser(User) List~Account~
        +findByUserId(Long) List~Account~
        +findByAccountNumber(String) Optional~Account~
    }

    class CardRepository {
        <<interface>>
        +findByUserId(Long) List~Card~
        +existsByCardNumber(String) boolean
        +findByCardNumber(String) Optional~Card~
        +findByUserIdAndCardType(Long, CardType) List~Card~
        +findByUserIdAndIsActiveTrue(Long) List~Card~
    }

    class TransactionRepository {
        <<interface>>
        +findByFromAccount(Account) List~Transaction~
        +findByToAccount(Account) List~Transaction~
    }

    %% ============ ENTITY LAYER ============
    class User {
        -Long id
        -String email
        -String password
        -String firstName
        -String lastName
        -LocalDateTime createdAt
    }

    class Account {
        -Long id
        -String accountNumber
        -AccountType accountType
        -BigDecimal balance
        -String currency
        -boolean isActive
        -User user
        -LocalDateTime createdAt
    }

    class Card {
        -Long id
        -String cardNumber
        -String cardHolderName
        -LocalDate expireDate
        -CardType cardType
        -CardBrand cardBrand
        -BigDecimal cardBalance
        -BigDecimal creditLimit
        -boolean isActive
        -User user
        -Account account
        +setDefaultExpireDate() void
    }

    class Transaction {
        -Long id
        -Account fromAccount
        -Account toAccount
        -BigDecimal amount
        -String currency
        -TransactionStatus status
        -String description
        -LocalDateTime createdAt
    }

    %% ============ ENUMS ============
    class AccountType {
        <<enumeration>>
        CHECKING
        SAVINGS
    }

    class CardType {
        <<enumeration>>
        DEBIT
        CREDIT
    }

    class CardBrand {
        <<enumeration>>
        VISA
        MASTERCARD
    }

    class TransactionStatus {
        <<enumeration>>
        PENDING
        SUCCESS
        COMPLETED
        FAILED
        CANCELLED
    }

    %% ============ REQUEST DTOs ============
    class RegisterRequest {
        -String email
        -String password
        -String firstName
        -String lastName
    }

    class LoginRequest {
        -String email
        -String password
    }

    class CreateAccountRequest {
        -Long userId
        -AccountType accountType
        -BigDecimal initialBalance
        -String currency
    }

    class CardRequest {
        -Long userId
        -String cardNumber
        -String cardHolderName
        -CardType cardType
        -CardBrand cardBrand
        -BigDecimal creditLimit
        -Long accountId
    }

    class TransferRequest {
        -Long fromAccountId
        -Long toAccountId
        -BigDecimal amount
        -String description
    }

    %% ============ RESPONSE DTOs ============
    class UserResponse {
        -Long id
        -String email
        -String firstName
        -String lastName
    }

    class AuthResponse {
        -String token
        -Long userId
        -String email
        -String firstName
        -String lastName
    }

    class AccountResponse {
        -Long id
        -Long userId
        -String accountNumber
        -String currency
        -AccountType accountType
        -BigDecimal balance
    }

    class CardResponse {
        -Long id
        -Long userId
        -CardType cardType
        -BigDecimal cardBalance
    }

    class TransactionResponse {
        -Long id
        -BigDecimal amount
        -String description
        -LocalDateTime transactionDate
        -Long fromAccountId
        -Long toAccountId
    }

    class DashboardResponse {
        -String userName
        -int totalAccounts
        -int totalCards
        -BigDecimal totalBalance
        -BigDecimal totalCreditAvailable
        -List~Account~ accounts
        -List~Card~ cards
    }

    %% ============ STARTUP DEPENDENCIES ============
    AddTestValues --> UserService
    AddTestValues --> AccountService

    %% ============ CONTROLLER -> SERVICE ============
    AuthController --> UserService
    AccountController --> AccountService
    AccountController --> UserService
    CardController --> CardService
    TransferController --> TransactionService
    DashboardController --> DashboardService

    %% ============ SERVICE -> REPOSITORY ============
    UserService --> UserRepository
    AccountService --> AccountRepository
    CardService --> CardRepository
    CardService --> UserRepository
    CardService --> AccountRepository
    TransactionService --> TransactionRepository
    TransactionService --> AccountService
    DashboardService --> UserRepository
    DashboardService --> AccountRepository
    DashboardService --> CardRepository

    %% ============ REPOSITORY INHERITANCE ============
    UserRepository --|> JpaRepository~T, ID~
    AccountRepository --|> JpaRepository~T, ID~
    CardRepository --|> JpaRepository~T, ID~
    TransactionRepository --|> JpaRepository~T, ID~

    %% ============ REPOSITORY -> ENTITY ============
    UserRepository ..> User : manages
    AccountRepository ..> Account : manages
    CardRepository ..> Card : manages
    TransactionRepository ..> Transaction : manages

    %% ============ ENTITY RELATIONSHIPS ============
    User "1" --o "*" Account : owns
    User "1" --o "*" Card : owns
    Account "1" --o "0..1" Card : linked
    Account "1" --o "*" Transaction : sends
    Account "1" --o "*" Transaction : receives

    %% ============ ENTITY -> ENUM ============
    Account --> AccountType
    Card --> CardType
    Card --> CardBrand
    Transaction --> TransactionStatus

    %% ============ DTO -> ENUM ============
    CreateAccountRequest --> AccountType
    AccountResponse --> AccountType
    CardRequest --> CardType
    CardRequest --> CardBrand
    CardResponse --> CardType
```

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
