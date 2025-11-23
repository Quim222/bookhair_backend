package com.bookhair.dto.ServicesDto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceCreateDto {

    @NotBlank(message = "Nome é obrigatório")
    private String name;

    @NotBlank(message = "Descrição é obrigatória")
    private String description;

    @NotNull(message = "Duração é obrigatória")
    private int duration;

    @NotNull(message = "Preço é obrigatório")
    private BigDecimal price;

    @NotBlank(message = "Cor é obrigatória")
    private String color;
}
