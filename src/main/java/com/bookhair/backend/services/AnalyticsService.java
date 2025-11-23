package com.bookhair.backend.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookhair.backend.repositories.AnalyticsRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final UserService userService;
    private final BookingService bookingService;
    private final AnalyticsRepository analyticsRepository;

    public record LabelCount(String label, long total) {
    }

    public record EmployeeCount(String name, long total) {
    }

    public int getTotalUsers() {
        return userService.getAllUsers().size();
    }

    public int getTotalBookings() {
        return bookingService.getAllBookings().size();
    }

    public double getAverageBookingsPerUser() {
        int totalUsers = getTotalUsers();
        if (totalUsers == 0) {
            return 0;
        }
        return (double) getTotalBookings() / totalUsers;
    }

    public Object[] getMostUsedPerClient(String clientId) {
        return analyticsRepository.getServiceMostUsedByClient(clientId);
    }

    public Object[] getMostUsedOverall() {
        return analyticsRepository.getServiceMostUsedByClient();
    }

    public Object[] getMostFrequentTime(String days) {
        return analyticsRepository.getTimeMostFrequency(days);
    }

    public Object[] getMostFrequentTimeByClient(String days, String clientId) {
        return analyticsRepository.getTimeMostFrequency(days, clientId);
    }

    public Object[] getMostEmployeeWorked() {
        return analyticsRepository.getMostEmployeeWorked();
    }
}
