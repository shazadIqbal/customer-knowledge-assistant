package com.example.auth.controller;

import com.example.auth.dto.CreateUserRequest;
import com.example.auth.dto.PagedResponse;
import com.example.auth.dto.UpdateUserRequest;
import com.example.auth.dto.UserDetailResponse;
import com.example.auth.service.UserManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/management/users")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "User Management", description = "CRUD operations for users with pagination")
@SecurityRequirement(name = "bearerAuth")
public class UserManagementController {

    @Autowired
    private UserManagementService userManagementService;

    @PostMapping
    @Operation(summary = "Create a new user",
            description = "Creates a user with authentication credentials and links them to a role (User + User_Role) and optionally to a project (User_Project). " +
                    "The appSource field (CHATBOT or ADMINPANEL) determines which application the user can access. " +
                    "Note: ROLE_USER users can only log in via CHATBOT.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error or duplicate username", content = @Content),
            @ApiResponse(responseCode = "404", description = "Role or Project not found", content = @Content)
    })
    public ResponseEntity<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserDetailResponse response = userManagementService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get all users with pagination",
            description = "Returns a paginated list of all users with their roles, application source, and assigned projects. " +
                    "Supports sorting and pagination parameters.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class)))
    public ResponseEntity<PagedResponse<UserDetailResponse>> getAllUsers(
            @Parameter(description = "Zero-based page index (default: 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (default: 10)") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Field to sort by (default: id)") @RequestParam(defaultValue = "id") String sortBy,
            @Parameter(description = "Sort direction: 'asc' or 'desc' (default: asc)") @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<UserDetailResponse> response = userManagementService.getAllUsers(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID",
            description = "Retrieve a specific user's details including their role, application source, and assigned projects by user ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<UserDetailResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserDetailResponse response = userManagementService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user",
            description = "Partially updates user fields (fullname, jobTitle, email, status, appSource). " +
                    "Also updates User_Role and User_Project if role/project IDs are provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserDetailResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content),
            @ApiResponse(responseCode = "404", description = "User, Role, or Project not found", content = @Content)
    })
    public ResponseEntity<UserDetailResponse> updateUser(
            @Parameter(description = "User ID") @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        UserDetailResponse response = userManagementService.updateUser(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user",
            description = "Deletes the user and all associated records (User_Role and User_Project entries) from the database.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "User ID") @PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
