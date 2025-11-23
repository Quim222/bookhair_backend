package com.bookhair.backend.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookhair.backend.model.Bookings;
import com.bookhair.backend.model.Guest_Customers;
import com.bookhair.backend.model.Services;
import com.bookhair.backend.model.StatusTypes;
import com.bookhair.backend.model.User;
import com.bookhair.backend.model.UserRole;
import com.bookhair.backend.repositories.BookingRepository;
import com.bookhair.backend.repositories.Guest_user_Respository;
import com.bookhair.dto.BookingsDto.BookingCreateDto;
import com.bookhair.dto.BookingsDto.BookingGuestCreateDTO;
import com.bookhair.dto.BookingsDto.BookingResponseDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final Guest_user_Respository guestUserRespository;
    private final ServicesService servicesService;
    private final UserService userService;

    public boolean createBooking(BookingCreateDto booking) {

        System.out.println("Verificando data/hora da reserva...");
        System.out.println(booking.getStartTime());
        System.out.println(booking.getEmployerId());
        System.out.println(booking.getServiceId());
        System.out.println(booking.getUserId());
        String bookingId = UUID.randomUUID().toString();
        User user = userService.getUserById(booking.getUserId());
        User employee = userService.getUserById(booking.getEmployerId());
        Services service = servicesService.getServiceById(booking.getServiceId());
        if (user == null || employee == null || service == null) {
            throw new IllegalArgumentException("User, Employee ou Service não encontrado");
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Não é possível criar uma reserva em uma data/hora passada");
        }

        int duration = service.getDuration();
        LocalDateTime endTime = booking.getStartTime().plusMinutes(duration);

        Bookings entity = new Bookings();
        entity.setId(bookingId);
        entity.setUser(user);
        entity.setEmployee(employee);
        entity.setService(service);
        entity.setStartTime(booking.getStartTime());
        entity.setEndTime(endTime);
        entity.setStatus(StatusTypes.PENDENTE);

        System.out.println("Criando nova reserva: " + bookingId);
        System.out.println("Usuário: " + user.getName());
        System.out.println("Funcionário: " + employee.getName());
        System.out.println("Serviço: " + service.getName());
        System.out.println("Horário da reserva: " + booking.getStartTime() + " - " + endTime);

        if (!verifyBookingTime(entity, booking.getStartTime(), endTime))
            throw new IllegalArgumentException("Intervalo inválido para a duração do serviço");

        if (!verifyEmployeeAvailability(employee.getUserId(), booking.getStartTime(), endTime, null))
            throw new IllegalArgumentException("Funcionário indisponível no horário");

        bookingRepository.save(entity);
        return true;
    }

    public boolean createGuestBooking(BookingGuestCreateDTO booking) {
        String bookingId = UUID.randomUUID().toString();
        String userId = UUID.randomUUID().toString();
        Guest_Customers user = new Guest_Customers(userId, booking.getName_user(), booking.getPhone_user(),
                booking.isConsent_terms());
        User employee = userService.getUserById(booking.getEmployeeId());
        Services service = servicesService.getServiceById(booking.getServiceId());
        if (user == null || employee == null || service == null) {
            throw new IllegalArgumentException("User, Employee ou Service não encontrado");
        }

        LocalDateTime now = LocalDateTime.now();
        if (booking.getStartTime().isBefore(now)) {
            throw new IllegalArgumentException("Não é possível criar uma reserva em uma data/hora passada");
        }

        int duration = service.getDuration();
        LocalDateTime endTime = booking.getStartTime().plusMinutes(duration);

        if (guestUserRespository.existsByGuestPhoneNumberAndCreatedAt(userId, booking.getStartTime())) {
            throw new IllegalArgumentException("Usuário convidado já existe para este horário");
        }
        Bookings entity = new Bookings();
        entity.setId(bookingId);
        entity.setUser(null);
        entity.setGuest(user);
        entity.setEmployee(employee);
        entity.setService(service);
        entity.setStartTime(booking.getStartTime());
        entity.setEndTime(endTime);
        entity.setStatus(StatusTypes.PENDENTE);

        System.out.println("Criando nova reserva: " + bookingId);
        System.out.println("Usuário: " + user.getName());
        System.out.println("Funcionário: " + employee.getName());
        System.out.println("Serviço: " + service.getName());
        System.out.println("Horário da reserva: " + booking.getStartTime() + " - " + endTime);

        if (!verifyBookingTime(entity, booking.getStartTime(), endTime))
            throw new IllegalArgumentException("Intervalo inválido para a duração do serviço");

        if (!verifyEmployeeAvailability(employee.getUserId(), booking.getStartTime(), endTime, null))
            throw new IllegalArgumentException("Funcionário indisponível no horário");

        guestUserRespository.save(entity.getGuest());
        bookingRepository.save(entity);
        return true;
    }

    public boolean deleteBooking(String bookingId) {
        if (!bookingRepository.existsById(bookingId)) {
            return false;
        }
        bookingRepository.deleteById(bookingId);
        return !bookingRepository.existsById(bookingId);
    }

    public boolean deleteAll() {
        bookingRepository.deleteAll();
        return bookingRepository.count() == 0;
    }

    @Transactional(readOnly = true)
    public BookingResponseDto getBookingByIdResponseDto(String bookingId) {
        return toResponseDto(bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId)));
    }

    @Transactional(readOnly = true)
    public Bookings getBookingById(String bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + bookingId));
    }

    public boolean bookingExists(String bookingId) {
        return bookingRepository.existsById(bookingId);
    }

    public boolean updateBooking(BookingCreateDto booking, String bookID) {
        Bookings old = getBookingById(bookID);

        if (!Objects.equals(booking.getUserId(), old.getUser().getUserId()))
            old.setUser(userService.getUserById(booking.getUserId()));

        if (!Objects.equals(booking.getEmployerId(), old.getEmployee().getUserId()))
            old.setEmployee(userService.getUserById(booking.getEmployerId()));

        if (!Objects.equals(booking.getServiceId(), old.getService().getId()))
            old.setService(servicesService.getServiceById(booking.getServiceId()));

        int duration = old.getService().getDuration();
        LocalDateTime endTime = booking.getStartTime().plusMinutes(duration);

        old.setStartTime(booking.getStartTime());
        old.setEndTime(endTime);

        if (!verifyBookingTime(old, booking.getStartTime(), endTime))
            throw new IllegalArgumentException("Intervalo inválido para a duração do serviço");

        if (!verifyEmployeeAvailability(old.getEmployee().getUserId(), booking.getStartTime(), endTime,
                old.getId()))
            throw new IllegalArgumentException("Funcionário indisponível no horário");

        bookingRepository.save(old);
        return true;
    }

    public boolean updateStatus(String idBooking, StatusTypes status) {
        Bookings booking = getBookingById(idBooking);
        booking.setStatus(status);
        bookingRepository.save(booking);
        return true;
    }

    // Listagens sem stream/findAll (evita N+1)
    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookings() {
        List<Bookings> bookings = bookingRepository.findAllWithJoins();

        bookings.forEach(this::autoUpdateStatus);

        return bookings.stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByUserId(String userId) {
        User user = userService.getUserById(userId);

        if (user.getUserRole().equals(UserRole.FUNCIONARIO)) {
            return bookingRepository.findByEmployee_UserId(userId).stream()
                    .map(this::toResponseDto)
                    .toList();
        } else {
            return bookingRepository.findByUser_UserId(userId).stream()
                    .map(this::toResponseDto)
                    .toList();
        }

    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByServiceId(String serviceId) {
        return bookingRepository.findByService_Id(serviceId).stream().map(this::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByUserAndService(String userId, String serviceId) {
        return bookingRepository.findByUserAndService(userId, serviceId).stream().map(this::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getAllBookingsByDate(LocalDateTime date) {
        LocalDateTime start = date.toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return bookingRepository.findAllByDay(start, end).stream().map(this::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null)
            throw new IllegalArgumentException("startDate e endDate são obrigatórios");
        return bookingRepository.findByDateRange(startDate, endDate).stream().map(this::toResponseDto).toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponseDto> getBookingsByStatus(StatusTypes status) {
        return bookingRepository.findByStatus(status).stream()
                .map(this::toResponseDto)
                .toList();
    }

    private BookingResponseDto toResponseDto(Bookings b) {
        User user = b.getUser();
        Guest_Customers guest = b.getGuest();

        return new BookingResponseDto(
                b.getId(),
                b.getService().getName(),
                b.getEmployee().getName(),
                user != null ? user.getName() : guest.getName(),
                b.getStartTime(),
                b.getEndTime(),
                b.getStatus(),
                b.getCreatedAt() != null ? b.getCreatedAt() : LocalDateTime.now(),
                b.getService().getColor());
    }

    private void autoUpdateStatus(Bookings booking) {
        LocalDateTime now = LocalDateTime.now();

        if (booking.getStartTime().isBefore(now)
                && booking.getStatus() != StatusTypes.FINISHED
                && booking.getStatus() != StatusTypes.CANCELLED) {

            booking.setStatus(StatusTypes.FINISHED);
            bookingRepository.save(booking);
        }
    }

    private boolean verifyBookingTime(Bookings booking, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime))
            return false;
        int expected = booking.getService().getDuration(); // minutos
        long minutes = ChronoUnit.MINUTES.between(startTime, endTime);
        return minutes == expected;
    }

    private boolean verifyEmployeeAvailability(String employeeId, LocalDateTime start, LocalDateTime end,
            String ignoreBookingId) {
        List<Bookings> bookings = bookingRepository.findByEmployee_UserId(employeeId);
        for (Bookings b : bookings) {
            if (ignoreBookingId != null && b.getId().equals(ignoreBookingId))
                continue;
            // sobreposição: [b.start,b.end) intersecta [start,end)
            if (b.getStartTime().isBefore(end) && b.getEndTime().isAfter(start))
                return false;
        }
        return true;
    }

}
