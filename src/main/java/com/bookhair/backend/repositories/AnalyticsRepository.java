package com.bookhair.backend.repositories;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Object[] getServiceMostUsedByClient() {
        String query = """
                SELECT s.name_service, COUNT(*) AS total_bookings
                FROM bookings b
                JOIN services s ON b.service_id = s.id_service
                JOIN users u ON b.user_id = u.user_id
                WHERE u."role" = 'CLIENTE' AND b.status = 'CONFIRMED'
                GROUP BY s.id_service, s.name_service
                ORDER BY total_bookings DESC, s.name_service
                LIMIT 1;
                """;
        Object[] result = (Object[]) entityManager.createNativeQuery(query)
                .getSingleResult();

        return result;
    }

    public Object[] getServiceMostUsedByClient(String clientId) {
        String query = """
                SELECT s.name_service, COUNT(*) AS total_bookings
                FROM bookings b
                JOIN services s ON b.service_id = s.id_service
                JOIN users u ON b.user_id = u.user_id
                WHERE u."role" = 'CLIENTE' AND b.status = 'CONFIRMED' AND u.user_id = :clientId
                GROUP BY s.id_service, s.name_service
                ORDER BY total_bookings DESC, s.name_service
                LIMIT 1
                """;
        Object[] result = (Object[]) entityManager.createNativeQuery(query)
                .setParameter("clientId", clientId)
                .getSingleResult();

        return result;
    }

    public Object[] getTimeMostFrequency(String days) {
        String sql = """
                WITH tz AS (
                SELECT (b.start_time AT TIME ZONE 'Europe/Lisbon') AS ts
                FROM bookings b
                WHERE b.status = 'CONFIRMED'
                    AND b.start_time >= now() - interval '180 days'
                )
                SELECT to_char(ts, 'HH24:MI') AS hora, COUNT(*) AS total
                FROM tz
                GROUP BY hora
                ORDER BY total DESC, hora
                LIMIT 1
                """;
        Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                .getSingleResult();

        return result;
    }

    public Object[] getTimeMostFrequency(String days, String clientId) {
        String sql = """
                WITH tz AS (
                SELECT (b.start_time AT TIME ZONE 'Europe/Lisbon') AS ts
                FROM bookings b
                JOIN users u ON b.user_id = u.user_id
                WHERE b.status = 'CONFIRMED'
                    AND b.start_time >= now() - (CAST(:days AS TEXT) || ' days')::interval
                    AND u.user_id = :clientId
                )
                SELECT to_char(ts, 'HH24:MI') AS hora, COUNT(*) AS total
                FROM tz
                GROUP BY hora
                ORDER BY total DESC, hora
                LIMIT 1;
                """;
        Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                .setParameter("days", days)
                .setParameter("clientId", clientId)
                .getSingleResult();

        return result;
    }

    public Object[] getMostEmployeeWorked() {
        String sql = """
                SELECT u."name", count(b.*) AS total_bookings
                FROM bookings b
                JOIN users u ON b.employee_id = u.user_id
                WHERE b.status = 'CONFIRMED'
                GROUP BY u."name"
                ORDER BY total_bookings desc
                LIMIT 1
                """;
        Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                .getSingleResult();

        return result;
    }

}
