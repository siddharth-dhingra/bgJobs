package com.capstone.bgJobs.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.capstone.bgJobs.model.RunbookConfig;

@Repository
public interface RunbookConfigRepository extends JpaRepository<RunbookConfig, Long> {
    Optional<RunbookConfig> findByRunbookId(String runbookId);
}