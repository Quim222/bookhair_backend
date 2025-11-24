package com.bookhair.dto.BookingsDto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreateDto {
    @NotBlank(message = "User ID é obrigatório")
    private String userId;

    @NotBlank(message = "Employer ID é obrigatório")
    private String employeeId;

    @NotBlank(message = "Service ID é obrigatório")
    private String serviceId;

    @NotNull(message = "Start Time é obrigatório")
    private LocalDateTime startTime;
}
