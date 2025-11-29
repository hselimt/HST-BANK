package com.hstbank_api.controller;

import com.hstbank_api.dto.LoginRequest;
import com.hstbank_api.dto.RegisterRequest;
import com.hstbank_api.dto.UserResponse;
import com.hstbank_api.model.User;
import com.hstbank_api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User user = userService.createUser(
                request.getEmail().trim().toLowerCase(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName()
        );

        // Entity = class mapped to database table, has hidden Hibernate stuff attached
        // DTO = plain data object, safe to send as JSON response
        // Never return entity directly - causes serialization errors
        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        String email = request.getEmail().toLowerCase();

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        UserResponse response = new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName()
        );

        return ResponseEntity.ok(response);
    }
}