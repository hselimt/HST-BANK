package com.hstbank_api.service;

import com.hstbank_api.model.User;
import com.hstbank_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User createUser(String email, String password, String firstName, String lastName) {
        regValidation(email, password, firstName, lastName);

        User user = new User();
        user.setEmail(email.trim());
        user.setPassword(password); // Plain text - use BCrypt for production
        user.setFirstName(firstName.toUpperCase());
        user.setLastName(lastName.toUpperCase());
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    private void regValidation(String email, String password, String firstName, String lastName) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User with " + email + " already exists");
        }

        if (!email.contains("@") || !email.contains(".")) {
            throw new RuntimeException("Invalid email address");
        }

        if (password.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters long");
        }

        // .matches() = regex check
        boolean hasLetter = password.matches(".*[a-zA-Z]+.*");
        boolean hasDigit = password.matches(".*\\d+.*");

        if (!hasLetter || !hasDigit) {
            throw new RuntimeException("Password must contain at least one letter and one number");
        }

        if (firstName == null || firstName.trim().length() < 2) {
            throw new RuntimeException("First name must be at least 2 characters long");
        }

        if (lastName == null || lastName.trim().length() < 2) {
            throw new RuntimeException("Last name must be at least 2 characters long");
        }
    }

    // Optional = nullable
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(); // Built-in JPA method
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id); // Built-in JPA method
    }
}