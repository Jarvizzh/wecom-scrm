package com.wecom.scrm.task;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.service.WecomEnterpriseService;
import com.wecom.scrm.service.changdu.ChangduSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChangduSyncTask {

    private final WecomEnterpriseService enterpriseService;
    private final ChangduSyncService syncService;

    /**
     * Every 30 minutes, sync Changdu data (users and recharge) from the last hour.
     */
    @Scheduled(cron = "0 15/30 * * * ?") // Offset by 15 mins from Changdu to spread load
    public void runAutoSync() {
        log.info("Starting automated Changdu data sync task...");

        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now.minusHours(1);

        for (WecomEnterprise enterprise : enterprises) {
            String corpId = enterprise.getCorpId();
            try {
                DynamicDataSourceContextHolder.push(corpId);
                
                log.info("[{}] Syncing Changdu data for {} to {}", corpId, startTime, now);
                
                // This will sync products, then users and recharge for all enabled products in this tenant
                syncService.syncAllEnabledProductUsers(startTime, now);

            } catch (Exception e) {
                log.error("Error in Changdu sync task for tenant: {}", corpId, e);
            } finally {
                DynamicDataSourceContextHolder.poll();
            }
        }
        log.info("Automated Changdu data sync task finished.");
    }
}
