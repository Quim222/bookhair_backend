package com.bookhair.backend.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookhair.backend.services.AnalyticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/analytics")
@RequiredArgsConstructor
public class AnalyticController {

    private final AnalyticsService analiticService;

    // GetMapping

    @GetMapping("/total-users")
    public int getTotalUsers() {
        return analiticService.getTotalUsers();
    }

    @GetMapping("/total-bookings")
    public int getTotalBookings() {
        return analiticService.getTotalBookings();
    }

    @GetMapping("/average-bookings-per-user")
    public double getAverageBookingsPerUser() {
        return analiticService.getAverageBookingsPerUser();
    }

    @GetMapping("/most-used-overall")
    public Object[] getMostUsedOverall() {
        return analiticService.getMostUsedOverall();
    }

    @GetMapping("/most-used-overall/{clientId}")
    public Object[] getMostUsedOverall(@PathVariable(value = "clientId") String clientId) {
        return analiticService.getMostUsedPerClient(clientId);
    }

    @GetMapping("/most-frequent-time/{days}")
    public Object[] getMostFrequentTime(@PathVariable(value = "days") String days) {
        return analiticService.getMostFrequentTime(days);
    }

    @GetMapping("/most-frequent-time/{days}/{clientId}")
    public Object[] getMostFrequentTimeByClient(@PathVariable String days, @PathVariable String clientId) {
        return analiticService.getMostFrequentTimeByClient(days, clientId);
    }

    @GetMapping("/most-employee-worked")
    public Object[] getMostEmployeeWorked() {
        return analiticService.getMostEmployeeWorked();
    }

}
