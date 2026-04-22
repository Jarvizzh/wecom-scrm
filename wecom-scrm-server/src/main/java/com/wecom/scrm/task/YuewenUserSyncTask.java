package com.wecom.scrm.task;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.service.WecomEnterpriseService;
import com.wecom.scrm.entity.yuewen.YuewenProduct;
import com.wecom.scrm.service.yuewen.YuewenProductService;
import com.wecom.scrm.service.yuewen.YuewenSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class YuewenUserSyncTask {

    private final WecomEnterpriseService enterpriseService;
    private final YuewenProductService productService;
    private final YuewenSyncService syncService;

    /**
     * Every 35 minutes, sync users from the last hour.
     * E.g., at 00:01, sync 23:01-00:01. At 00:16, sync 23:16-00:16.
     */
    @Scheduled(cron = "0 1/15 * * * ?")
    public void runAutoSync() {
        log.info("Starting automated Yuewen user sync task...");

        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise enterprise : enterprises) {
            String corpId = enterprise.getCorpId();
            try {
                DynamicDataSourceContextHolder.push(corpId);

                List<YuewenProduct> activeProducts = productService.getActiveProducts();
                if (activeProducts.isEmpty()) {
                    continue;
                }

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime oneHourAgo = now.minusHours(1);
                
                long startTime = oneHourAgo.toEpochSecond(ZoneOffset.ofHours(8));
                long endTime = now.toEpochSecond(ZoneOffset.ofHours(8));

                log.info("[{}] Found {} active Yuewen products to sync users for {} to {}",
                        corpId, activeProducts.size(), oneHourAgo, now);

                // This will sync users and recharge for all active products in this tenant
                syncService.syncAllActiveProduct(activeProducts, startTime, endTime);

            } catch (Exception e) {
                log.error("Error in Yuewen sync task for tenant: {}", corpId, e);
            } finally {
                DynamicDataSourceContextHolder.poll();
            }
        }
    }
}
