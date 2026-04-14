package com.wecom.scrm.task;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.config.WxCpServiceManager;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.entity.WecomEventLog;
import com.wecom.scrm.repository.WecomEventLogRepository;
import com.wecom.scrm.service.WecomEnterpriseService;
import com.wecom.scrm.service.WecomEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class WecomEventCompensationTask {

    private final WecomEventLogRepository eventLogRepository;
    private final WecomEventService eventService;
    private final WecomEnterpriseService enterpriseService;

    public WecomEventCompensationTask(WecomEventLogRepository eventLogRepository,
                                     WecomEventService eventService,
                                     WecomEnterpriseService enterpriseService) {
        this.eventLogRepository = eventLogRepository;
        this.eventService = eventService;
        this.enterpriseService = enterpriseService;
    }

    @Scheduled(cron = "0 0/5 * * * ?") // Run every 5 minutes
    public void runEventCompensation() {
        log.info("Starting WeCom Event Compensation Task...");
        
        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise enterprise : enterprises) {
            String corpId = enterprise.getCorpId();
            try {
                // Set multi-tenant context
                DynamicDataSourceContextHolder.push(corpId);
                WxCpServiceManager.setCurrentCorpId(corpId);
                
                processTenantEvents(corpId);
                eventService.cleanupOldEvents();
                
            } catch (Exception e) {
                log.error("Error in compensation task for tenant: {}", corpId, e);
            } finally {
                // Clear multi-tenant context
                DynamicDataSourceContextHolder.poll();
                WxCpServiceManager.clearCurrentCorpId();
            }
        }
    }

    private void processTenantEvents(String corpId) {
        // 1. Pick up pending events older than 5 minutes
        LocalDateTime pendingThreshold = LocalDateTime.now().minusMinutes(5);
        List<WecomEventLog> pendingEvents = eventLogRepository.findByStatusAndCreateTimeBefore(0, pendingThreshold);
        
        // 2. Pick up failed events with retry count < 3
        List<WecomEventLog> failedEvents = eventLogRepository.findByStatusAndRetryCountLessThan(2, 3);
        
        if (pendingEvents.isEmpty() && failedEvents.isEmpty()) {
            return;
        }

        log.info("[{}] Found {} pending and {} failed events to compensate", 
                corpId, pendingEvents.size(), failedEvents.size());

        for (WecomEventLog event : pendingEvents) {
            eventService.processEvent(event);
        }

        for (WecomEventLog event : failedEvents) {
            eventService.processEvent(event);
        }
    }
}
