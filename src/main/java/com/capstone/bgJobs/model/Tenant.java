package com.capstone.bgJobs.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="tenant_id", length = 50, nullable = false, unique = true)
    private String tenantId;

    @Column(name = "tenant_name", nullable = false)
    private String tenantName;

    @Column(name = "owner", nullable = false)
    private String owner;

    @Column(name = "repo", nullable = false)
    private String repo;

    @Column(name = "pat", nullable = false)
    private String pat;

    @Column(name = "es_index", nullable = false)
    private String esIndex;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "username")
    private String username;

    @Column(name = "account_url")
    private String accountUrl;

    @Column(name = "api_token")
    private String apiToken;

    @Column(name = "project_key")
    private String projectKey;
    
    public Tenant() {}

    public Tenant(Long id, String tenantId, String tenantName, String owner, String repo, String pat, String esIndex,
            LocalDateTime createdAt, LocalDateTime updatedAt, String username, String accountUrl, String apiToken, String projectKey) {
        this.id = id;
        this.tenantId = tenantId;
        this.tenantName = tenantName;
        this.owner = owner;
        this.repo = repo;
        this.pat = pat;
        this.esIndex = esIndex;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.username = username;
        this.accountUrl = accountUrl;
        this.apiToken = apiToken;
        this.projectKey = projectKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getPat() {
        return pat;
    }

    public void setPat(String pat) {
        this.pat = pat;
    }

    public String getEsIndex() {
        return esIndex;
    }

    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccountUrl() {
        return accountUrl;
    }

    public void setAccountUrl(String accountUrl) {
        this.accountUrl = accountUrl;
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }

    public Tenant orElseThrow(Object object) {
        throw new UnsupportedOperationException("Unimplemented method 'orElseThrow'");
    }    
}