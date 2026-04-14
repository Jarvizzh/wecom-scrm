package com.wecom.scrm.task;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.entity.WecomMpAccount;
import com.wecom.scrm.service.MpService;
import com.wecom.scrm.service.WecomEnterpriseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MpUserSyncTask {

    private final MpService mpService;
    private final WecomEnterpriseService enterpriseService;

    public MpUserSyncTask(MpService mpService, WecomEnterpriseService enterpriseService) {
        this.mpService = mpService;
        this.enterpriseService = enterpriseService;
    }

    /**
     * Scheduled task to sync MP users for all enterprises.
     * Cron: 0 15 0/6 * * ? (Starting from 00:15, every 6 hours)
     * Hours: 00:15, 06:15, 12:15, 18:15
     */
    @Scheduled(cron = "0 15 0/6 * * ?")
    public void runMpUserSync() {
        log.info("Starting Scheduled MP User Sync Task...");

        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise enterprise : enterprises) {
            String corpId = enterprise.getCorpId();
            try {
                // Set multi-tenant context
                DynamicDataSourceContextHolder.push(corpId);
                
                // Get all MP accounts for this enterprise
                List<WecomMpAccount> accounts = mpService.getAllAccounts();
                if (accounts.isEmpty()) {
                    continue;
                }

                log.info("[{}] Found {} MP accounts to sync", corpId, accounts.size());

                for (WecomMpAccount account : accounts) {
                    // Trigger async sync (uses mpSyncExecutor)
                    mpService.syncUsers(account.getAppId(), corpId);
                }

            } catch (Exception e) {
                log.error("Error in MP sync task for tenant: {}", corpId, e);
            } finally {
                // Clear multi-tenant context
                DynamicDataSourceContextHolder.poll();
            }
        }
    }
}
