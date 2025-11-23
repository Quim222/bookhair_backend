package com.bookhair.backend.model;

import java.time.Instant;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_photos")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserPhoto {

    @Id
    @Column(name = "reference_id", nullable = false, length = 36)
    private String referenceId;

    @Column(name = "mime_type", nullable = false, length = 100)
    private String mimeType;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.BINARY)
    @Column(name = "bytes", nullable = false)
    private byte[] bytes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "typephoto", nullable = false)
    private String type;

    @PrePersist
    void onCreate() {
        if (createdAt == null)
            createdAt = Instant.now();
    }
}
