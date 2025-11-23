package com.bookhair.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookhair.backend.model.UserPhoto;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto, String> {
    UserPhoto findByReferenceId(String referenceId);

    boolean existsByReferenceId(String referenceId);

    void deleteByReferenceId(String referenceId);
}
