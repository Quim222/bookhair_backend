package com.bookhair.dto.UserDto;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.UserRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    private String name;
    private String email;
    private UserRole userRole;
    private StatusUser statusUser;

}
