package com.wecom.scrm.task;

import com.wecom.scrm.entity.WecomCustomerMessage;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.repository.WecomCustomerMessageRepository;
import com.wecom.scrm.service.CustomerMessageService;
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
public class CustomerMessageTask {

    private final WecomCustomerMessageRepository messageRepository;
    private final CustomerMessageService messageService;
    private final WecomEnterpriseService enterpriseService;

    public CustomerMessageTask(WecomCustomerMessageRepository messageRepository,
                               CustomerMessageService messageService,
                               WecomEnterpriseService enterpriseService) {
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.enterpriseService = enterpriseService;
    }

    @Scheduled(cron = "0 0/2 * * * ?") // Run every 2 minutes
    public void executeScheduledTasks() {
        log.info("Running CustomerMessageTask to pick up scheduled broadcasts across all tenants...");
        
        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise ent : enterprises) {
            String corpId = ent.getCorpId();
            try {
                // Switch context
                DynamicDataSourceContextHolder.push(corpId);
                WxCpServiceManager.setCurrentCorpId(corpId);
                
                processTenantTasks(corpId);
                
            } catch (Exception e) {
                log.error("Error processing scheduled tasks for tenant: {}", corpId, e);
            } finally {
                // Clear context
                DynamicDataSourceContextHolder.poll();
                WxCpServiceManager.clearCurrentCorpId();
            }
        }
    }

    private void processTenantTasks(String corpId) {
        List<WecomCustomerMessage> pendingTasks = messageRepository.findByStatusAndSendTypeAndSendTimeBefore(
                0, 1, LocalDateTime.now());

        if (pendingTasks.isEmpty()) {
            return;
        }

        log.info("[{}] Found {} pending scheduled broadcast tasks", corpId, pendingTasks.size());
        for (WecomCustomerMessage task : pendingTasks) {
            try {
                messageService.executeTask(task);
            } catch (Exception e) {
                log.error("[{}] Failed to execute scheduled task: {}", corpId, task.getId(), e);
            }
        }
    }
}
