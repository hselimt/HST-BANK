# HST Bank API - Annotations

A comprehensive guide to all annotations used in this Spring Boot banking application.

---

## Spring Core

### @SpringBootApplication
Main entry point of the app. Place on the class with `main()` method.
```java
@SpringBootApplication
public class HstBankApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(HstBankApiApplication.class, args);
    }
}
```
Combines three annotations:
- `@Configuration` - marks class as config source
- `@EnableAutoConfiguration` - Spring configures beans automatically
- `@ComponentScan` - scans this package and subpackages for components

---

### @Component
Marks class as a Spring-managed bean. Spring creates and manages the instance automatically.
```java
@Component
public class AddTestValues implements CommandLineRunner { }
```
Use for generic classes that don't fit Service/Repository categories.

---

### @Service
Same as `@Component`, but indicates business logic layer.
```java
@Service
public class UserService { }
```
Makes code intention clear: "this class handles business rules."

---

### @Repository
Same as `@Component`, but for data access layer. Adds database exception translation.
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> { }
```

---

### @RestController
Combines `@Controller` + `@ResponseBody`. Every method returns JSON, not HTML.
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController { }
```

---

### @RequestMapping
Sets base URL path for all endpoints in a controller.
```java
@RequestMapping("/api/users")  // All endpoints start with /api/users
```

---

### @GetMapping / @PostMapping
Handles HTTP GET and POST requests.
```java
@GetMapping("/{id}")      // GET /api/users/5
@PostMapping("/register") // POST /api/auth/register
```

---

### @PathVariable
Extracts value from URL path.
```java
@GetMapping("/{id}")
public User getUser(@PathVariable Long id) { }
// /users/5 → id = 5
```

---

### @RequestBody
Parses JSON request body into Java object.
```java
@PostMapping("/register")
public ResponseEntity register(@RequestBody RegisterRequest request) { }
// {"email": "test@test.com"} → RegisterRequest object
```

---

### @CrossOrigin
Allows requests from other domains (CORS). Without this, browser blocks frontend requests.
```java
@CrossOrigin(origins = "*")  // Allow all domains (use specific domain in production)
```

---

### @Transactional
Wraps method in database transaction. If exception thrown, ALL changes roll back.
```java
@Transactional
public Transaction transfer(Long fromId, Long toId, BigDecimal amount) {
    accountService.withdraw(fromAccount, amount);  // If this succeeds
    accountService.deposit(toAccount, amount);     // But this fails
    // Both operations roll back - money doesn't disappear
}
```
Critical for operations that must succeed or fail together.

---

### @RequiredArgsConstructor
Lombok annotation. Generates constructor with all `final` fields. Enables dependency injection.
```java
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;  // Injected via generated constructor
}
```

---

## JPA / Hibernate (Entity Mapping)

### @Entity
Marks class as database table mapping. Each instance = one row.
```java
@Entity
public class User { }
```

---

### @Table
Specifies exact table name in database.
```java
@Table(name = "users")  // Table name in PostgreSQL
```

---

### @Id
Marks field as primary key. Every entity MUST have this.
```java
@Id
private Long id;
```

---

### @GeneratedValue
Database auto-generates the ID (auto-increment).
```java
@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;
// Don't set ID manually - database assigns it on save
```

---

### @Column
Configures column properties.
```java
@Column(nullable = false)              // NOT NULL
@Column(unique = true)                 // UNIQUE constraint
@Column(name = "email_address")        // Column name in DB
@Column(precision = 10, scale = 2)     // For BigDecimal (10 digits, 2 decimal)
```

---

### @Enumerated
How to store enum in database.
```java
@Enumerated(EnumType.STRING)   // Stores "CHECKING", "SAVINGS" (readable)
@Enumerated(EnumType.ORDINAL)  // Stores 0, 1, 2 (don't use - breaks if order changes)
```

---

### @ManyToOne
Relationship: many of this entity → one of another.
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id")
private User user;
// Many Accounts → One User
```

---

### @JoinColumn
Specifies foreign key column name. Use with `@ManyToOne`.
```java
@JoinColumn(name = "user_id")  // Creates user_id column referencing users table
```

---

### @FetchType.LAZY vs EAGER
```java
@ManyToOne(fetch = FetchType.LAZY)   // Load only when accessed (recommended)
@ManyToOne(fetch = FetchType.EAGER)  // Load immediately (can hurt performance)
```

---

### @PrePersist
Method runs automatically BEFORE entity is saved to database.
```java
@PrePersist
public void setDefaultExpireDate() {
    if (this.expireDate == null) {
        this.expireDate = LocalDate.now().plusYears(7);
    }
}
```

---

### @CreationTimestamp
Hibernate annotation. Automatically sets field to current timestamp when entity is created.
```java
@CreationTimestamp
@Column(nullable = false, updatable = false)
private LocalDateTime createdAt;
// Automatically set to current time on insert, never updated
```
Different from @PrePersist - this is Hibernate-specific and simpler for timestamps.

---

## Lombok (Code Generation)

### @Data
Generates everything: getters, setters, toString, equals, hashCode.
```java
@Data
public class UserResponse {
    private Long id;
    private String email;
}
// Automatically has getId(), setId(), getEmail(), setEmail(), etc.
```
Best for DTOs. Avoid on entities (can cause lazy loading issues).

---

### @Getter / @Setter
Generate only getters or setters.
```java
@Getter
@Setter
public class User {
    private String email;  // → getEmail() and setEmail()
}
```

---

### @NoArgsConstructor
Generates empty constructor. Required by JPA/Hibernate.
```java
@NoArgsConstructor
public class User { }
// Generates: public User() { }
```

---

### @AllArgsConstructor
Generates constructor with ALL fields.
```java
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
}
// Generates: public UserResponse(Long id, String email) { }
```

---

### @EqualsAndHashCode
Generates equals() and hashCode() methods.
```java
@EqualsAndHashCode(of = "id")  // Only compare by id
public class User { }
// Two users are equal if same id
```

---

## Key Patterns

### Constructor Injection
```java
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;  // Spring injects this
}
```

---

### Repository Method Naming
Spring generates SQL from method name automatically.
```java
findByEmail(String email)              // WHERE email = ?
findByUserId(Long userId)              // WHERE user_id = ?
findByUserIdAndCardType(id, type)      // WHERE user_id = ? AND card_type = ?
existsByCardNumber(number)             // Returns true/false
findByUserIdAndIsActiveTrue(userId)    // WHERE user_id = ? AND is_active = true
```

---

### Entity vs DTO
| Entity | DTO |
|--------|-----|
| Maps to database table | Plain data object |
| Has Hibernate proxy attached | No magic, just data |
| Internal use only | Safe to return as JSON |
| Contains all fields including sensitive | Contains only fields you choose |

**Rule:** Always convert Entity → DTO before returning from controller.

```java
// Bad - returning entity
return ResponseEntity.ok(user);

// Good - returning DTO
UserResponse response = new UserResponse(user.getId(), user.getEmail(), ...);
return ResponseEntity.ok(response);
```