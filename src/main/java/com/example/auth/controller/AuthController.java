package com.example.auth.controller;

import com.example.auth.dto.JwtResponse;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.MessageResponse;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Create a new user account with username, password, and role")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Username already taken or validation error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            String message = authService.registerUser(registerRequest);
            return ResponseEntity.ok(new MessageResponse(message));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = JwtResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid username or password",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            JwtResponse jwtResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid username or password"));
        }
    }
}
