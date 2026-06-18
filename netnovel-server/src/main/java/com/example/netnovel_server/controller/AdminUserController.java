package com.example.netnovel_server.controller;

import com.example.netnovel_server.dto.AdminUserCreateDTO;
import com.example.netnovel_server.dto.AdminUserUpdateDTO;
import com.example.netnovel_server.dto.UserDTO;
import com.example.netnovel_server.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin Users", description = "Admin-only user management APIs")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Get users")
    public ResponseEntity<Page<UserDTO>> getUsers(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getUsers(pageable));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user by id")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUser(userId));
    }

    @PostMapping
    @Operation(summary = "Create a local user")
    public ResponseEntity<UserDTO> createUser(@RequestBody AdminUserCreateDTO request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "Update a user and optionally replace roles")
    public ResponseEntity<UserDTO> updateUser(
        @PathVariable Long userId,
        @RequestBody AdminUserUpdateDTO request
    ) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, request));
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }
}
