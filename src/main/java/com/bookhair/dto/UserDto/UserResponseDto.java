package com.bookhair.dto.UserDto;

import java.time.LocalDateTime;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private UserRole userRole;
    private StatusUser statusUser;
    private String photoUrl;
    private boolean hasPhoto;
    private LocalDateTime createdAt;
}
