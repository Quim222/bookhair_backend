package com.bookhair.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.User;
import com.bookhair.backend.model.UserRole;
//import com.bookhair.backend.repositories.UserPhotoRepository;
import com.bookhair.backend.repositories.UserRepository;
import com.bookhair.dto.UserDto.UserCreateDto;
import com.bookhair.dto.UserDto.UserResponseDto;
import com.bookhair.dto.UserDto.UserUpdateDto;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    // private final UserPhotoRepository userPhotoRepository;

    // CREATE
    public UserResponseDto createUser(UserCreateDto dto) {
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new IllegalStateException("Email já está em uso: " + dto.getEmail());
        }

        UUID userId = UUID.randomUUID();
        String encryptedPassword = new BCryptPasswordEncoder().encode(dto.getPassword());

        User user = new User();
        user.setUserId(userId.toString());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setPassword(encryptedPassword);
        user.setUserRole(dto.getUserRole());
        if (dto.getStatusUser() != null) {
            user.setStatusUser(dto.getStatusUser());
        } else {
            user.setStatusUser(dto.getUserRole() == UserRole.ADMIN ? StatusUser.ATIVO : StatusUser.PENDENTE);
        }
        User saved = userRepository.save(user);
        return toResponseDto(saved);
    }

    // READ
    @Transactional(readOnly = true)
    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilizador não encontrado: " + userId));
    }

    public Boolean updateStatusUserToActive(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilizador não encontrado: " + userId));

        if (user.getStatusUser() == StatusUser.ATIVO) {
            return false; // já está ativo
        }

        user.setStatusUser(StatusUser.ATIVO);
        userRepository.save(user);
        return true;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        User user = userRepository.findByEmailIgnoreCase(email);
        return toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAllByOrderByUserRoleAsc().stream()
                .map(this::toResponseDto)
                .toList();
    }

    public User findByEmailIgnoreCase(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByRole(UserRole userRole) {
        if (userRole.equals(UserRole.FUNCIONARIO)) {
            return userRepository.findUsersWithPhoto(userRole);
        } else {
            return userRepository.findByUserRole(userRole).stream()
                    .map(this::toResponseDto)
                    .toList();
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsersByStatus(StatusUser statusUser) {
        return userRepository.findByStatusUser(statusUser).stream()
                .map(this::toResponseDto)
                .toList();
    }

    public UserResponseDto updateUserStatus(String userId, StatusUser statusUser) {
        User existing = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Utilizador não encontrado: " + userId));

        if (existing.getStatusUser() == statusUser) {
            return toResponseDto(existing); // sem alterações
        }

        existing.setStatusUser(statusUser);
        return toResponseDto(userRepository.save(existing));
    }

    // UPDATE (exemplo simples; podes criar um DTO próprio para update)
    public UserResponseDto updateUser(UserUpdateDto updatedUser) {
        User existing = userRepository.findByEmailIgnoreCase(updatedUser.getEmail());

        if (existing == null) {
            throw new EntityNotFoundException("Utilizador não encontrado: " + updatedUser.getEmail());
        }

        if (existing.getStatusUser() == StatusUser.PENDENTE) {
            throw new IllegalStateException("Utilizador ainda está pendente: " + updatedUser.getName());
        }

        if (updatedUser.getUserRole() != existing.getUserRole()) {
            existing.setUserRole(updatedUser.getUserRole());
        }

        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setStatusUser(updatedUser.getStatusUser());

        return toResponseDto(userRepository.save(existing));
    }

    // CHECK EXISTENCE
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmailIgnoreCase(email);
    }

    // DELETE
    public boolean deleteUser(String userId) {
        if (!userRepository.existsById(userId))
            return false;
        userRepository.deleteById(userId);
        return !userRepository.existsById(userId);
    }

    // HELPER MAPPER
    private UserResponseDto toResponseDto(User u) {

        return new UserResponseDto(
                u.getUserId(),
                u.getName(),
                u.getEmail(),
                u.getPhone(),
                u.getUserRole() != null ? u.getUserRole() : null,
                u.getStatusUser(),
                null,
                false,
                u.getCreatedAt() != null ? u.getCreatedAt() : LocalDateTime.now());
    }

}
