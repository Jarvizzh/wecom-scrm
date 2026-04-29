package com.wecom.scrm.task;

import com.wecom.scrm.entity.WecomMoment;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.repository.WecomMomentRepository;
import com.wecom.scrm.service.MomentService;
import com.wecom.scrm.service.WecomEnterpriseService;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.config.WxCpServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class MomentTask {

    private final WecomMomentRepository momentRepository;
    private final MomentService momentService;
    private final WecomEnterpriseService enterpriseService;

    public MomentTask(WecomMomentRepository momentRepository,
                      MomentService momentService,
                      WecomEnterpriseService enterpriseService) {
        this.momentRepository = momentRepository;
        this.momentService = momentService;
        this.enterpriseService = enterpriseService;
    }

    @Scheduled(cron = "0 0/5 * * * ?") // Run every 5 minute
    public void executeScheduledTasks() {
        log.info("Running MomentTask to pick up scheduled moments across all tenants...");
        
        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise ent : enterprises) {
            String corpId = ent.getCorpId();
            try {
                // Switch context
                DynamicDataSourceContextHolder.push(corpId);
                WxCpServiceManager.setCurrentCorpId(corpId);
                
                processTenantTasks(corpId);
                
            } catch (Exception e) {
                log.error("Error processing scheduled moments for tenant: {}", corpId, e);
            } finally {
                // Clear context
                DynamicDataSourceContextHolder.poll();
                WxCpServiceManager.clearCurrentCorpId();
            }
        }
    }

    private void processTenantTasks(String corpId) {
        // Find tasks with status 3 (Scheduled) and sendTime before now
        List<WecomMoment> pendingTasks = momentRepository.findByStatusAndSendTypeAndSendTimeBefore(
                3, 1, LocalDateTime.now());

        if (pendingTasks.isEmpty()) {
            return;
        }

        log.info("[{}] Found {} pending scheduled moment tasks", corpId, pendingTasks.size());
        for (WecomMoment task : pendingTasks) {
            try {
                // Trigger async publishing (re-uses existing logic)
                momentService.publishMomentToWeCom(task.getId());
            } catch (Exception e) {
                log.error("[{}] Failed to execute scheduled moment task: {}", corpId, task.getId(), e);
            }
        }
    }
}
