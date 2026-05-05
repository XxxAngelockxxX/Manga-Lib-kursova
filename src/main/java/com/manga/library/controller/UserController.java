package com.manga.library.controller;

import com.manga.library.model.Role;
import com.manga.library.model.User;
import com.manga.library.repository.RoleRepository;
import com.manga.library.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Керування користувачами (тільки для ADMIN)")
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Operation(summary = "Отримати всіх користувачів", description = "US-3.1: Перегляд списку користувачів")
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Operation(summary = "Змінити роль користувача", description = "US-3.1: Наприклад, підвищити USER до AUTHOR")
    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<String> updateUserRole(@PathVariable Long userId, @RequestParam String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Користувача не знайдено"));

        Role newRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Роль не знайдена"));

        user.setRole(newRole);
        userRepository.save(user);

        return ResponseEntity.ok("Роль користувача " + user.getUsername() + " змінено на " + roleName);
    }
}