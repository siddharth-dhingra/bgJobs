package com.capstone.bgJobs.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.capstone.bgJobs.dto.RunbookDTO;
import com.capstone.bgJobs.model.Runbook;
import com.capstone.bgJobs.repository.RunbookConfigRepository;
import com.capstone.bgJobs.repository.RunbookRepository;

@Service
public class RunbookConfigurationService {

    private final RunbookRepository runbookRepository;
    private final RunbookConfigRepository runbookConfigRepository;

    public RunbookConfigurationService(RunbookRepository runbookRepository,
                                       RunbookConfigRepository runbookConfigRepository) {
        this.runbookRepository = runbookRepository;
        this.runbookConfigRepository = runbookConfigRepository;
    }

    public List<RunbookDTO> getRunbooksByTenantAndTrigger(String tenantId, String triggerType) {
        List<Runbook> runbooks = runbookRepository.findByTenantId(tenantId);
        List<RunbookDTO> dtos = new ArrayList<>();
        for (Runbook rb : runbooks) {
            runbookConfigRepository.findByRunbookId(rb.getRunbookId()).ifPresent(config -> {
                if (config.getTrigger() != null && config.getTrigger().toString().equalsIgnoreCase(triggerType)
                        && Boolean.TRUE.equals(rb.getEnabled())) {
                    RunbookDTO dto = new RunbookDTO();
                    dto.setRunbookId(rb.getRunbookId());
                    dto.setTenantId(rb.getTenantId());
                    dto.setName(rb.getName());
                    dto.setDescription(rb.getDescription());
                    dto.setEnabled(rb.getEnabled());
                    dto.setTrigger(config.getTrigger().toString());
                    dto.setFiltersJson(config.getFiltersJson());
                    dto.setActionsJson(config.getActionsJson());
                    dtos.add(dto);
                }
            });
        }
        return dtos;
    }
}