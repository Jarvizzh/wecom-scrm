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
                
            } catch (Exception e) {
                log.error("Error in compensation task for tenant: {}", corpId, e);
            } finally {
                // Clear multi-tenant context
                DynamicDataSourceContextHolder.poll();
                WxCpServiceManager.clearCurrentCorpId();
            }
        }
    }

    @Scheduled(cron = "0 0 3 * * ?") // 3:00 AM every day
    public void runEventCleanup() {
        log.info("Starting WeCom Event Cleanup Task...");
        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise enterprise : enterprises) {
            String corpId = enterprise.getCorpId();
            try {
                DynamicDataSourceContextHolder.push(corpId);
                eventService.cleanupOldEvents();
            } catch (Exception e) {
                log.error("Error in cleanup task for tenant: {}", corpId, e);
            } finally {
                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    private void processTenantEvents(String corpId) {
        // 1. Pick up pending events older than 10 minutes (safety margin for async listener)
        LocalDateTime pendingThreshold = LocalDateTime.now().minusMinutes(10);
        List<WecomEventLog> pendingEvents = eventLogRepository.findByStatusAndCreateTimeBefore(0, pendingThreshold);
        
        // 2. Pick up failed events with retry count < 3
        List<WecomEventLog> failedEvents = eventLogRepository.findByStatusAndRetryCountLessThan(2, 3);

        // 3. Pick up "Processing" zombie tasks (stuck for more than 30 minutes)
        LocalDateTime zombieThreshold = LocalDateTime.now().minusMinutes(30);
        List<WecomEventLog> zombieEvents = eventLogRepository.findByStatusAndUpdateTimeBefore(3, zombieThreshold);
        
        if (pendingEvents.isEmpty() && failedEvents.isEmpty() && zombieEvents.isEmpty()) {
            return;
        }

        log.info("[{}] Compensation needed: {} pending, {} failed, {} zombies", 
                corpId, pendingEvents.size(), failedEvents.size(), zombieEvents.size());

        // Process all identified events. 
        // processEvent() now handles atomic status claiming internally.
        for (WecomEventLog event : pendingEvents) {
            eventService.processEvent(event);
        }

        for (WecomEventLog event : failedEvents) {
            eventService.processEvent(event);
        }

        for (WecomEventLog event : zombieEvents) {
            log.warn("[{}] Recovering zombie event: {}", corpId, event.getId());
            eventService.processEvent(event);
        }
    }
}
