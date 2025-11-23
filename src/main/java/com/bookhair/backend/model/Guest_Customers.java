package com.bookhair.backend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "guest_customers")
public class Guest_Customers {

    @Id
    @NonNull
    @Column(name = "guest_id", nullable = false)
    private String guestId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true, name = "phone")
    private String phoneNumber;

    @Column(nullable = false)
    private boolean consent_contact;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Guest_Customers(String guestId, String name, String phoneNumber, boolean consent_contact) {
        this.guestId = guestId;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.consent_contact = consent_contact;
    }
}
