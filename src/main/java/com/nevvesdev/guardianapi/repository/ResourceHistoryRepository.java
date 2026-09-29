package com.nevvesdev.guardianapi.repository;

import com.nevvesdev.guardianapi.entity.ResourceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceHistoryRepository extends JpaRepository<ResourceHistory, String> {

    List<ResourceHistory> findByResourceIdOrderByCreatedAtDesc(String resourceId);

    List<ResourceHistory> findByChangedByOrderByCreatedAtDesc(String changedBy);

    List<ResourceHistory> findByChangeTypeOrderByCreatedAtDesc(String changeType);
}