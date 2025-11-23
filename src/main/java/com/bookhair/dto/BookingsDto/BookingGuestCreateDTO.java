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
public class BookingGuestCreateDTO {

    @NotBlank(message = "Nome é obrigatório")
    private String name_user;

    @NotBlank(message = "Telefone é obrigatório")
    private String phone_user;

    @NotBlank(message = "Consentimento é obrigatório")
    private boolean consent_terms;

    @NotBlank(message = "ID do funcionário é obrigatório")
    private String employeeId;

    @NotBlank(message = "ID do serviço é obrigatório")
    private String serviceId;

    @NotNull(message = "Data e hora de início são obrigatórias")
    private LocalDateTime startTime;
}
