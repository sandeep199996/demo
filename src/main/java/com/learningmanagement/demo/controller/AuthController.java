package com.learningmanagement.demo.controller;



import com.learningmanagement.demo.dto.AuthRequest;
import com.learningmanagement.demo.dto.AuthResponse;
import com.learningmanagement.demo.dto.RegisterRequest;
import com.learningmanagement.demo.entity.User;
import com.learningmanagement.demo.security.JwtUtil;
import com.learningmanagement.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Register new user
            User user = userService.registerUser(request);

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Build response
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .message("User registered successfully")
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            AuthResponse response = AuthResponse.builder()
                    .message(e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Get user details
            User user = userService.findByEmail(request.getEmail());

            // Generate JWT token
            String token = jwtUtil.generateToken(user.getEmail());

            // Build response
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .message("Login successful")
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            AuthResponse response = AuthResponse.builder()
                    .message("Invalid email or password")
                    .build();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
