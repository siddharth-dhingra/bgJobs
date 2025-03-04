package com.capstone.bgJobs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capstone.bgJobs.model.Runbook;

@Repository
public interface RunbookRepository extends JpaRepository<Runbook, Long> {
    List<Runbook> findByTenantId(String tenantId);
    Optional<Runbook> findByRunbookId(String runbookId);
}