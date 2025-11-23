package com.bookhair.backend.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "services")
public class Services {

    @Id
    @Column(name = "id_service", nullable = false)
    private String id;

    @Column(name = "name_service", nullable = false)
    private String name;

    @Column(name = "desc_service", nullable = false)
    private String description;

    @Column(name = "duration_minutes", nullable = false)
    private int duration;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "color", nullable = false)
    private String color;

    public Services(String id, String name, String description, int duration, BigDecimal price, String color) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.duration = duration;
        this.price = price;
        this.color = color;
    }
}
