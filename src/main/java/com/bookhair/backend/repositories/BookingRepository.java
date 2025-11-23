package com.bookhair.backend.repositories;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bookhair.backend.model.Bookings;
import com.bookhair.backend.model.StatusTypes;

public interface BookingRepository extends JpaRepository<Bookings, String> {

    List<Bookings> findByUser_UserId(String userId);

    @Query("""
            select b from Bookings b
            left join fetch b.user u
            left join fetch b.guest g
            left join fetch b.employee e
            left join fetch b.service s
            """)
    List<Bookings> findAllWithJoins();

    List<Bookings> findByGuest_GuestId(String guestId);

    List<Bookings> findByEmployee_UserId(String employeeId);

    List<Bookings> findByService_Id(String serviceId);

    List<Bookings> findByStatus(StatusTypes status);

    @Query("""
                select b from Bookings b
                where b.user.userId = :userId and b.service.id = :serviceId
            """)
    List<Bookings> findByUserAndService(@Param("userId") String userId, @Param("serviceId") String serviceId);

    @Query("""
                select b from Bookings b
                where b.startTime >= :start and b.endTime <= :end
            """)
    List<Bookings> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("""
                select b from Bookings b
                where b.startTime >= :dayStart and b.startTime < :dayEnd
            """)
    List<Bookings> findAllByDay(@Param("dayStart") LocalDateTime dayStart, @Param("dayEnd") LocalDateTime dayEnd);
}
