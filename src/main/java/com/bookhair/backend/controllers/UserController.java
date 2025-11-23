package com.bookhair.backend.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.User;
import com.bookhair.backend.model.UserRole;
import com.bookhair.backend.services.UserService;
import com.bookhair.dto.UserDto.UserCreateDto;
import com.bookhair.dto.UserDto.UserResponseDto;
import com.bookhair.dto.UserDto.UserUpdateDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/me")
    public UserResponseDto me(@AuthenticationPrincipal(expression = "username") String email) {
        if (email == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Não autenticado");
        }
        return userService.getUserByEmail(email);
    }

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody @Valid UserCreateDto dto) {
        UserResponseDto created = userService.createUser(dto);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/status/{statusUser}")
    public ResponseEntity<List<UserResponseDto>> getUsersByStatus(@PathVariable("statusUser") StatusUser statusUser) {
        return ResponseEntity.ok(userService.getUsersByStatus(statusUser));
    }

    @GetMapping("/employees")
    public ResponseEntity<List<UserResponseDto>> getEmployees() {
        return ResponseEntity.ok(userService.getUsersByRole(UserRole.FUNCIONARIO));
    }

    @GetMapping("/clients")
    public ResponseEntity<List<UserResponseDto>> getClients() {
        return ResponseEntity.ok(userService.getUsersByRole(UserRole.CLIENTE));
    }

    @PutMapping
    public ResponseEntity<UserResponseDto> updateUser(@RequestBody @Valid UserUpdateDto user) {
        UserResponseDto updateUser = userService.updateUser(user);
        return ResponseEntity.ok(updateUser);
    }

    @PutMapping("/status/{userId}/{statusUser}")
    public ResponseEntity<UserResponseDto> updateUserStatus(@PathVariable("userId") String userId,
            @PathVariable("statusUser") StatusUser statusUser) {
        return ResponseEntity.ok(userService.updateUserStatus(userId, statusUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Boolean> deleteUser(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(userService.deleteUser(userId));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponseDto> getUserByEmail(@PathVariable("email") String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping("/exists/{email}")
    public ResponseEntity<Boolean> userExistsByEmail(@PathVariable("email") String email) {
        // Cria no service um método existsByEmail, que usa o repo
        // existsByEmailIgnoreCase
        return ResponseEntity.ok(userService.existsByEmail(email));
    }

}
