package com.wecom.scrm.task;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.config.WxCpServiceManager;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.service.SyncService;
import com.wecom.scrm.service.WecomEnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Daily scheduled full synchronization tasks for WeCom data consistency.
 */
@Slf4j
@Component
public class WecomFullSyncTask {

    private final WecomEnterpriseService enterpriseService;
    private final SyncService syncService;

    public WecomFullSyncTask(WecomEnterpriseService enterpriseService, SyncService syncService) {
        this.enterpriseService = enterpriseService;
        this.syncService = syncService;
    }

    /**
     * Daily synchronization of Contacts (Departments, Users) and Tags at 0:00.
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncContactsAndTagsDaily() {
        log.info("Starting designated daily Full Sync (Contacts & Tags) at 00:00...");
        processDailySync(true, false);
    }

    /**
     * Daily synchronization of Customers and Group Chats at 1:00.
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void syncCustomersAndGroupsDaily() {
        log.info("Starting designated daily Full Sync (Customers & Groups) at 01:00...");
        processDailySync(false, true);
    }

    /**
     * Logic to iterate through all active enterprises and trigger sync methods.
     * Context is set per enterprise and propagated to async threads by MdcTaskDecorator.
     */
    private void processDailySync(boolean syncContacts, boolean syncCustomers) {
        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise enterprise : enterprises) {
            // Only sync active enterprises (status 1 = Enabled)
            if (enterprise.getStatus() == null || enterprise.getStatus() != 1) {
                continue;
            }

            String corpId = enterprise.getCorpId();
            try {
                // Set multi-tenant context (CorpId and DataSource)
                DynamicDataSourceContextHolder.push(corpId);
                WxCpServiceManager.setCurrentCorpId(corpId);

                log.info("[{}] Triggering daily synchronization suite (syncContacts={}, syncCustomers={})", 
                        corpId, syncContacts, syncCustomers);
                
                if (syncContacts) {
                    syncService.syncAllDepartments();
                    syncService.syncAllUsers();
                    syncService.syncCorpTags();
                }

                if (syncCustomers) {
                    syncService.syncAllCustomers();
                    syncService.syncGroupChats();
                }

            } catch (Exception e) {
                log.error("Error during scheduled sync for enterprise: {}", corpId, e);
            } finally {
                // Clear context to prevent leakage
                DynamicDataSourceContextHolder.poll();
                WxCpServiceManager.clearCurrentCorpId();
            }
        }
    }
}
