package com.nevvesdev.guardianapi.repository;

import com.nevvesdev.guardianapi.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, String> {

    List<Resource> findByOwnerIdAndDeletedAtIsNull(String ownerId);

    Optional<Resource> findByIdAndDeletedAtIsNull(String id);

    @Query("SELECT r FROM Resource r WHERE r.deletedAt IS NULL AND r.isActive = true")
    List<Resource> findAllActive();
}