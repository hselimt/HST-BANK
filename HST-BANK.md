# HST Bank API - Banking REST API

## Overview
| Item | Value |
|------|-------|
| Type | REST API |
| Framework | Spring Boot 3.5.6 |
| Language | Java 17 |
| Database | PostgreSQL |
| Architecture | Layered (Controller → Service → Repository) |

---

## Tech Stack

| Technology | Purpose |
|------------|---------|
| Spring Boot | Backend framework |
| Spring Data JPA | Database ORM |
| PostgreSQL | Relational database |
| Lombok | Reduces boilerplate code |
| Maven | Build tool / dependency management |
---

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT                                  │
│                   (Browser / Frontend)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ HTTP Request (JSON)
┌─────────────────────────────────────────────────────────────────┐
│                       CONTROLLER                                │
│         Handles HTTP requests, returns responses                │
│    AuthController, AccountController, TransferController...     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Method calls
┌─────────────────────────────────────────────────────────────────┐
│                        SERVICE                                  │
│              Business logic, validation                         │
│      UserService, AccountService, TransactionService...         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ Method calls
┌─────────────────────────────────────────────────────────────────┐
│                       REPOSITORY                                │
│            Database operations (CRUD)                           │
│    UserRepository, AccountRepository, CardRepository...         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼ SQL Queries
┌─────────────────────────────────────────────────────────────────┐
│                       DATABASE                                  │
│                      PostgreSQL                                 │
│           users, accounts, cards, transactions                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
src/main/java/com/hstbank_api/
├── HstBankApiApplication.java    # Entry point
├── AddTestValues.java            # Creates test data on startup
├── controller/                   # HTTP endpoints
│   ├── AuthController.java       # Login/Register
│   ├── AccountController.java    # Account CRUD
│   ├── CardController.java       # Card CRUD
│   ├── DashboardController.java  # User dashboard
│   └── TransferController.java   # Money transfers
├── service/                      # Business logic
│   ├── UserService.java
│   ├── AccountService.java
│   ├── CardService.java
│   ├── DashboardService.java
│   └── TransactionService.java
├── repository/                   # Database access
│   ├── UserRepository.java
│   ├── AccountRepository.java
│   ├── CardRepository.java
│   └── TransactionRepository.java
├── model/                        # Database entities
│   ├── User.java
│   ├── Account.java
│   ├── Card.java
│   ├── Transaction.java
│   └── enums (AccountType, CardType, etc.)
└── dto/                          # Data Transfer Objects
    ├── LoginRequest.java
    ├── RegisterRequest.java
    ├── UserResponse.java
    └── ...
```

---

## Layers Explained

### 1. Controller Layer
- Receives HTTP requests
- Parses JSON to Java objects (`@RequestBody`)
- Calls Service methods
- Returns JSON responses
- Does NOT contain business logic

```
HTTP Request → Controller → Service → Repository → Database
HTTP Response ← Controller ← Service ← Repository ← Database
```

### 2. Service Layer
- Contains business logic
- Validation rules
- Transaction management (`@Transactional`)
- Coordinates between repositories

### 3. Repository Layer
- Database operations
- Extends `JpaRepository<Entity, ID>`
- Spring auto-generates SQL from method names

```java
findByEmail(String email)        → SELECT * FROM users WHERE email = ?
findByUserId(Long userId)        → SELECT * FROM accounts WHERE user_id = ?
existsByCardNumber(String num)   → SELECT EXISTS(... WHERE card_number = ?)
```

### 4. Entity Layer (Model)
- Java classes mapped to database tables
- Each instance = one row
- Uses JPA annotations (`@Entity`, `@Table`, `@Column`)

### 5. DTO Layer
- Plain data objects for API request/response
- No database magic attached
- Safe to convert to JSON

---

## Database Schema

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    users     │       │   accounts   │       │    cards     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │──┐    │ id (PK)      │──┐    │ id (PK)      │
│ email        │  │    │ account_num  │  │    │ card_number  │
│ password     │  │    │ account_type │  │    │ card_type    │
│ first_name   │  │    │ balance      │  │    │ card_brand   │
│ last_name    │  │    │ currency     │  │    │ card_balance │
│ created_at   │  │    │ is_active    │  │    │ credit_limit │
└──────────────┘  │    │ user_id (FK) │◄─┘    │ user_id (FK) │◄─┐
                  │    │ created_at   │       │ account_id   │  │
                  │    └──────────────┘       │ expire_date  │  │
                  │           │               └──────────────┘  │
                  │           │                                 │
                  └───────────┴─────────────────────────────────┘

┌──────────────────┐
│   transactions   │
├──────────────────┤
│ id (PK)          │
│ from_account (FK)│──► accounts
│ to_account (FK)  │──► accounts
│ amount           │
│ currency         │
│ status           │
│ description      │
│ created_at       │
└──────────────────┘
```

---

## Entity vs DTO

| Entity | DTO |
|--------|-----|
| Maps to database table | Plain data object |
| Has Hibernate proxy | No magic, just data |
| Contains all fields | Only needed fields |
| Internal use | Safe for JSON response |

**Rule:** Always convert Entity → DTO before returning from Controller.

```java
// Bad - Hibernate proxy issues
return ResponseEntity.ok(user);

// Good - Clean JSON
UserResponse response = new UserResponse(user.getId(), user.getEmail()...);
return ResponseEntity.ok(response);
```

---

## Request Flow Example

### Transfer Money Flow

```
1. Client sends POST /api/transfers
   {
     "fromAccountId": 1,
     "toAccountId": 2,
     "amount": 100.00,
     "description": "Payment"
   }
           │
           ▼
2. TransferController.transfer()
   - Receives request
   - Calls transactionService.transfer()
           │
           ▼
3. TransactionService.transfer() [@Transactional]
   - Validates amount > 0
   - Gets fromAccount from DB
   - Gets toAccount from DB
   - Checks sufficient balance
   - Checks currency match
   - accountService.withdraw(from, amount)
   - accountService.deposit(to, amount)
   - Creates Transaction record
   - Saves to DB
           │
           ▼
4. AccountService.withdraw() / deposit()
   - Updates balance
   - Saves account
           │
           ▼
5. TransactionRepository.save()
   - Inserts transaction into DB
           │
           ▼
6. Response returns to client
   {
     "id": 1,
     "amount": 100.00,
     "description": "Payment",
     "transactionDate": "2025-01-03T12:00:00",
     "fromAccountId": 1,
     "toAccountId": 2
   }
```

---

## @Transactional Explained

```java
@Transactional
public Transaction transfer(Long fromId, Long toId, BigDecimal amount) {
    accountService.withdraw(fromAccount, amount);  // Step 1
    accountService.deposit(toAccount, amount);     // Step 2
    transactionRepository.save(transaction);       // Step 3
}
```

If Step 2 fails:
- **Without @Transactional:** Step 1 already saved, money disappears
- **With @Transactional:** All steps roll back, no money lost

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | /api/auth/register | Register new user |
| POST | /api/auth/login | Login |
| GET | /api/dashboard/user/{id} | Get user dashboard |
| POST | /api/accounts | Create account |
| POST | /api/cards | Create card |
| POST | /api/transfers | Transfer money |
| GET | /api/transfers/history/{accountId} | Transaction history |
| GET | /api/transfers/{transactionId} | Get single transaction |

---

## Key Annotations

### Spring Core
| Annotation | Purpose |
|------------|---------|
| `@SpringBootApplication` | Entry point, enables auto-config |
| `@RestController` | REST API controller, returns JSON |
| `@Service` | Business logic layer |
| `@Repository` | Data access layer |
| `@Component` | Generic Spring-managed bean |

### HTTP Mapping
| Annotation | Purpose |
|------------|---------|
| `@RequestMapping` | Base URL path |
| `@GetMapping` | Handle GET requests |
| `@PostMapping` | Handle POST requests |
| `@PathVariable` | Extract from URL: `/users/{id}` |
| `@RequestBody` | Parse JSON body to object |

### JPA / Database
| Annotation | Purpose |
|------------|---------|
| `@Entity` | Class maps to table |
| `@Table` | Specify table name |
| `@Id` | Primary key |
| `@GeneratedValue` | Auto-increment |
| `@Column` | Column config (nullable, unique) |
| `@ManyToOne` | Relationship: many → one |
| `@JoinColumn` | Foreign key column |
| `@Enumerated` | Store enum as STRING or ORDINAL |
| `@CreationTimestamp` | Auto-set timestamp on insert |
| `@PrePersist` | Run before entity saved |

### Lombok
| Annotation | Generates |
|------------|-----------|
| `@Data` | Getters, setters, toString, equals, hashCode |
| `@Getter/@Setter` | Only getters/setters |
| `@NoArgsConstructor` | Empty constructor |
| `@AllArgsConstructor` | Constructor with all fields |
| `@RequiredArgsConstructor` | Constructor with final fields (DI) |

---

## Dependency Injection

```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;  // Spring injects this
}
```

- `@RequiredArgsConstructor` generates constructor
- Spring sees constructor parameter, injects matching bean
- Field must be `final`

---

## Optional Type

```java
public Optional<User> findByEmail(String email);
```

- Wrapper that may or may not contain value
- Avoids NullPointerException
- Usage:

```java
// Old way (null check)
User user = userRepository.findByEmail(email);
if (user == null) throw new Exception("Not found");

// Optional way
User user = userRepository.findByEmail(email)
    .orElseThrow(() -> new RuntimeException("Not found"));
```

---

## Stream API

```java
// Sum all account balances
BigDecimal total = accounts.stream()
    .map(Account::getBalance)      // Extract balance from each
    .reduce(BigDecimal.ZERO, BigDecimal::add);  // Sum all

// Filter only credit cards
List<Card> creditCards = cards.stream()
    .filter(card -> card.getCreditLimit() != null)
    .collect(Collectors.toList());
```

| Method | Purpose |
|--------|---------|
| `.stream()` | Convert list to stream |
| `.map()` | Transform each element |
| `.filter()` | Keep elements matching condition |
| `.reduce()` | Combine all into single value |
| `.collect()` | Convert back to list |

---

## Running the Application

### Local
```bash
# 1. Start PostgreSQL, create database 'hstbank'
# 2. Update password in application.properties
# 3. Run
./mvnw spring-boot:run
# 4. Open http://localhost:8080/login.html
```

---

## Test Account
| Field | Value |
|-------|-------|
| Email | admin@hstbank.com |
| Password | admin123 |

---

## Configuration

### application.properties
```properties
# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/hstbank
spring.datasource.username=postgres
spring.datasource.password=1111

# JPA
spring.jpa.hibernate.ddl-auto=update  # Auto-create/update tables
spring.jpa.show-sql=true              # Print SQL to console
```

### ddl-auto Options
| Value | Behavior |
|-------|----------|
| `create` | Drop + create tables on startup |
| `update` | Modify existing tables |
| `validate` | Only check, no changes |
| `none` | Do nothing |
