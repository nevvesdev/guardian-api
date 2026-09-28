package com.nevvesdev.guardianapi.controller;

import com.nevvesdev.guardianapi.dto.request.AssignRoleRequest;
import com.nevvesdev.guardianapi.dto.response.UserResponse;
import com.nevvesdev.guardianapi.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> findById(@PathVariable String id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<UserResponse> assignRole(@PathVariable String id,
                                                   @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(userService.assignRole(id, request));
    }

    @DeleteMapping("/{id}/roles/{roleName}")
    public ResponseEntity<UserResponse> removeRole(@PathVariable String id,
                                                   @PathVariable String roleName) {
        return ResponseEntity.ok(userService.removeRole(id, roleName));
    }
}