package com.bookhair.dto.UserDto;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateDto {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email inválido")
    private String email;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone;

    @NotBlank(message = "Password é obrigatória")
    @Size(min = 6, message = "Password deve ter no minimo 6 caracteres")
    private String password;

    @NotNull(message = "Cargo é obrigatório")
    private UserRole userRole;

    private StatusUser statusUser;

    public UserCreateDto(String name, String email, String password, UserRole userRole) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.userRole = userRole;
    }
}
