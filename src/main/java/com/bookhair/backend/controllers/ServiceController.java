package com.bookhair.backend.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

import com.bookhair.backend.model.Services;
import com.bookhair.backend.services.ServicesService;
import com.bookhair.dto.ServicesDto.ServiceCreateDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/service")
public class ServiceController {

    @Autowired
    private ServicesService serviceService;

    // GET MAPPING

    @GetMapping
    public ResponseEntity<List<Services>> getAllServices() {
        List<Services> services = serviceService.getAllServices();
        return ResponseEntity.ok(services);
    }

    @GetMapping("/{serviceId}")
    public ResponseEntity<Services> getServiceById(@PathVariable("serviceId") String serviceId) {
        Services service = serviceService.getServiceById(serviceId);
        return ResponseEntity.ok(service);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<List<Services>> getServicesByName(@PathVariable("name") String name) {
        List<Services> services = serviceService.getServicesByName(name);
        return ResponseEntity.ok(services);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<Services>> filter(
            @RequestParam(name = "minDuration", required = false) Integer minDuration,
            @RequestParam(name = "maxDuration", required = false) Integer maxDuration,
            @RequestParam(name = "minPrice", required = false) BigDecimal minPrice,
            @RequestParam(name = "maxPrice", required = false) BigDecimal maxPrice,
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "sortBy", required = false) String sortBy,
            @RequestParam(name = "sortDir", required = false) String sortDir) {
        List<Services> results = serviceService.findByFilters(
                minDuration, maxDuration, minPrice, maxPrice, q, sortBy, sortDir);
        return ResponseEntity.ok(results);
    }

    // Post Mapping

    @PostMapping
    public ResponseEntity<Services> createService(@Valid @RequestBody ServiceCreateDto serviceCreateDto) {
        Services createdService = serviceService.createService(serviceCreateDto);
        return createdService != null ? ResponseEntity.status(HttpStatus.CREATED).body(createdService)
                : ResponseEntity.badRequest().build();
    }

    // Put Mapping

    @PutMapping("/{serviceId}")
    public ResponseEntity<Services> updateService(@Valid @RequestBody ServiceCreateDto service,
            @PathVariable("serviceId") String serviceID) {
        Services updatedService = serviceService.updateService(service, serviceID);
        return updatedService != null ? ResponseEntity.ok(updatedService) : ResponseEntity.notFound().build();
    }

    // Delete Mapping

    @DeleteMapping("/{serviceId}")
    public ResponseEntity<Void> deleteService(@PathVariable("serviceId") String serviceId) {
        boolean deleted = serviceService.deleteService(serviceId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

}
