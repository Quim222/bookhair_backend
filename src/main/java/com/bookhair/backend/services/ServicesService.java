package com.bookhair.backend.services;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bookhair.backend.model.Services;
import com.bookhair.backend.repositories.ServiceRepository;
import com.bookhair.backend.specs.ServiceSpecs;
import com.bookhair.dto.ServicesDto.ServiceCreateDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicesService {

    private final ServiceRepository serviceRepository;
    private final UserPhotoService userPhotoService;

    private static final Set<String> SORTABLE_FIELDS = Set.of("price", "duration", "name");

    @Transactional
    public Services createService(ServiceCreateDto service) {
        String serviceId = UUID.randomUUID().toString();
        boolean exists = serviceRepository.existsByNameIgnoreCase(service.getName());
        if (exists) {
            throw new IllegalArgumentException("Já existe um serviço com este nome.");
        }
        System.out.println(service.getColor());
        Services newService = new Services(
                serviceId,
                service.getName(),
                service.getDescription(),
                service.getDuration(),
                service.getPrice(),
                service.getColor());
        serviceRepository.save(newService);
        return newService;
    }

    @Transactional
    public Services updateService(ServiceCreateDto service, String serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            return null;
        }

        Services existingService = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado."));

        if (!existingService.getName().equalsIgnoreCase(service.getName())
                && serviceRepository.existsByNameIgnoreCase(service.getName())) {
            throw new IllegalArgumentException("Já existe um serviço com este nome.");
        }

        if (!existingService.getName().equalsIgnoreCase(service.getName())) {
            existingService.setName(service.getName());
        }

        if (!Objects.equals(existingService.getDuration(), service.getDuration())) {
            existingService.setDuration(service.getDuration());
        }
        if (!Objects.equals(existingService.getPrice(), service.getPrice())) {
            existingService.setPrice(service.getPrice());
        }

        serviceRepository.save(existingService);
        return existingService;
    }

    @Transactional
    public boolean deleteService(String serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            return false;
        }
        serviceRepository.deleteById(serviceId);
        userPhotoService.delete(serviceId);
        return true;
    }

    public List<Services> getAllServices() {
        return serviceRepository.findAll();
    }

    public Services getServiceById(String serviceId) {
        return serviceRepository.findById(serviceId).orElse(null);
    }

    public List<Services> getServicesByName(String name) {
        return serviceRepository.findAllByNameContainingIgnoreCase(name);
    }

    public List<Services> findByFilters(
            @Nullable Integer minDuration,
            @Nullable Integer maxDuration,
            @Nullable BigDecimal minPrice,
            @Nullable BigDecimal maxPrice,
            @Nullable String q,
            @Nullable String sortBy, // "price", "duration" ou "name"
            @Nullable String sortDir // "asc" ou "desc"
    ) {
        validateRanges(minDuration, maxDuration, minPrice, maxPrice);

        Specification<Services> spec = Specification.<Services>unrestricted()
                .and(ServiceSpecs.durationGte(minDuration))
                .and(ServiceSpecs.durationLte(maxDuration))
                .and(ServiceSpecs.priceGte(minPrice))
                .and(ServiceSpecs.priceLte(maxPrice))
                .and(ServiceSpecs.nameContainsIgnoreCase(q));

        Sort sort = buildSort(sortBy, sortDir);

        return (sort == null) ? serviceRepository.findAll(spec) : serviceRepository.findAll(spec, sort);
    }

    private static void validateRanges(
            Integer minDuration, Integer maxDuration,
            BigDecimal minPrice, BigDecimal maxPrice) {
        if (minDuration != null && maxDuration != null && minDuration > maxDuration) {
            throw new IllegalArgumentException("minDuration não pode ser maior que maxDuration");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice não pode ser maior que maxPrice");
        }
        if (minDuration != null && minDuration < 0) {
            throw new IllegalArgumentException("minDuration não pode ser negativo");
        }
        if (maxDuration != null && maxDuration < 0) {
            throw new IllegalArgumentException("maxDuration não pode ser negativo");
        }
    }

    private static Sort buildSort(@Nullable String sortBy, @Nullable String sortDir) {
        if (sortBy == null || sortBy.isBlank())
            return null;

        // garantir segurança: só aceitar campos conhecidos
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Campo de ordenação inválido: " + sortBy);
        }
        boolean desc = "desc".equalsIgnoreCase(sortDir);
        return desc ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    }
}
