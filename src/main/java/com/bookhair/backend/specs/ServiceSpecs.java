package com.bookhair.backend.specs;

import java.math.BigDecimal;

import org.springframework.data.jpa.domain.Specification;

import com.bookhair.backend.model.Services;

public class ServiceSpecs {

    // duração >= min
    public static Specification<Services> durationGte(Integer min) {
        return (root, query, cb) -> (min == null || min < 0) ? null : cb.ge(root.get("duration"), min);

    }

    // duração <= max
    public static Specification<Services> durationLte(Integer max) {
        return (root, query, cb) -> (max == null || max < 0) ? null : cb.le(root.get("duration"), max);
    }

    // preço >= min
    public static Specification<Services> priceGte(BigDecimal min) {
        return (root, query, cb) -> (min == null)
                ? null
                : cb.greaterThanOrEqualTo(root.get("price"), min);
    }

    // preço <= max
    public static Specification<Services> priceLte(BigDecimal max) {
        return (root, query, cb) -> (max == null)
                ? null
                : cb.lessThanOrEqualTo(root.get("price"), max);
    }

    // (extra) nome contém, ignore case
    public static Specification<Services> nameContainsIgnoreCase(String q) {
        if (q == null)
            return null;
        String t = q.trim();
        return t.isEmpty() ? null
                : (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + t.toLowerCase() + "%");
    }
}
