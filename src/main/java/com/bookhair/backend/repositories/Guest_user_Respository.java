package com.bookhair.backend.repositories;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookhair.backend.model.Guest_Customers;

@Repository
public interface Guest_user_Respository extends JpaRepository<Guest_Customers, String> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1 FROM bookings b
                left join guest_customers gc ON b.guest_id = gc.guest_id
                WHERE gc.phone = :phoneNumber
                AND b.start_time = :createdAt
                AND b.status = 'CONFIRMED'
            )
            """, nativeQuery = true)
    boolean existsByGuestPhoneNumberAndCreatedAt(
            @Param("phoneNumber") String phoneNumber,
            @Param("createdAt") LocalDateTime createdAt);
}
