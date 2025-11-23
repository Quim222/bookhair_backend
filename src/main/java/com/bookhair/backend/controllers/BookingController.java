package com.bookhair.backend.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bookhair.backend.model.StatusTypes;
import com.bookhair.backend.services.BookingService;
import com.bookhair.dto.BookingsDto.BookingCreateDto;
import com.bookhair.dto.BookingsDto.BookingGuestCreateDTO;
import com.bookhair.dto.BookingsDto.BookingResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    // Post Mapping
    @PostMapping
    public ResponseEntity<Boolean> createBooking(@RequestBody BookingCreateDto booking) {
        return ResponseEntity.status(201).body(bookingService.createBooking(booking));
    }

    @PostMapping("/guest")
    public ResponseEntity<Boolean> createGuestBooking(@RequestBody BookingGuestCreateDTO booking) {
        return ResponseEntity.status(201).body(bookingService.createGuestBooking(booking));
    }

    // Get Mapping
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponseDto> getBooking(@PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.getBookingByIdResponseDto(id));
    }

    @GetMapping
    public ResponseEntity<List<BookingResponseDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUserId(@PathVariable("userId") String userId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserId(userId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByStatus(@PathVariable("status") StatusTypes status) {
        return ResponseEntity.ok(bookingService.getBookingsByStatus(status));
    }

    @GetMapping("/service/{serviceId}")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByServiceId(
            @PathVariable String serviceId) {
        return ResponseEntity.ok(bookingService.getBookingsByServiceId(serviceId));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> bookingExists(@PathVariable("id") String id) {
        return ResponseEntity.ok(bookingService.bookingExists(id));
    }

    @GetMapping(params = { "userId", "serviceId" })
    public ResponseEntity<List<BookingResponseDto>> getBookingsByUserIdAndServiceId(
            @RequestParam(name = "userId") String userId,
            @RequestParam(name = "serviceId") String serviceId) {
        return ResponseEntity.ok(bookingService.getBookingsByUserAndService(userId, serviceId));
    }

    @GetMapping("/range")
    public ResponseEntity<List<BookingResponseDto>> getBookingsInRange(
            @RequestParam(name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(bookingService.getBookingsByDateRange(startDate, endDate));
    }

    @GetMapping("/date")
    public ResponseEntity<List<BookingResponseDto>> getBookingsByDate(
            @RequestParam(name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(bookingService.getAllBookingsByDate(date));
    }

    // PUT /bookings/{id} -> 200 + body atualizado
    @PutMapping("/{id}")
    public ResponseEntity<Boolean> updateBooking(
            @PathVariable(name = "id") String id,
            @Valid @RequestBody BookingCreateDto booking) {
        var updated = bookingService.updateBooking(booking, id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Boolean> updateBookingStatus(
            @PathVariable(name = "id") String id,
            @RequestParam(name = "status") StatusTypes status) {
        var updated = bookingService.updateStatus(id, status);
        return ResponseEntity.ok(updated);
    }

    // Delete Mapping\
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("id") String id) {
        boolean isDeleted = bookingService.deleteBooking(id);
        if (!isDeleted)
            return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllBookings() {
        boolean isDeleted = bookingService.deleteAll();
        if (!isDeleted)
            return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }

}
