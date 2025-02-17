package com.capstone.bgJobs.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.capstone.bgJobs.model.Tenant;

import java.util.Optional;

public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByTenantId(String tenantId);
}