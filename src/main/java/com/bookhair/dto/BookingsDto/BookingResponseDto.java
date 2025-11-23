package com.bookhair.dto.BookingsDto;

import java.time.LocalDateTime;

import com.bookhair.backend.model.StatusTypes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookingResponseDto {
    private String id;
    private String serviceName;
    private String employeeName;
    private String customerName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private StatusTypes status;
    private LocalDateTime createdAt;
    private String color;
}
