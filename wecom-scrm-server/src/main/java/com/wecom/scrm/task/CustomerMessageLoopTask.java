package com.wecom.scrm.task;

import com.wecom.scrm.entity.WecomCustomerMessage;
import com.wecom.scrm.entity.WecomCustomerMessageLoop;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.repository.WecomCustomerMessageLoopRepository;
import com.wecom.scrm.repository.WecomCustomerMessageRepository;
import com.wecom.scrm.service.CustomerMessageLoopService;
import com.wecom.scrm.service.CustomerMessageService;
import com.wecom.scrm.service.WecomEnterpriseService;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.config.WxCpServiceManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class CustomerMessageLoopTask {

    private final WecomCustomerMessageLoopRepository loopRepository;
    private final WecomCustomerMessageRepository messageRepository;
    private final CustomerMessageService messageService;
    private final CustomerMessageLoopService loopService;
    private final WecomEnterpriseService enterpriseService;

    public CustomerMessageLoopTask(WecomCustomerMessageLoopRepository loopRepository,
                                   WecomCustomerMessageRepository messageRepository,
                                   CustomerMessageService messageService,
                                   CustomerMessageLoopService loopService,
                                   WecomEnterpriseService enterpriseService) {
        this.loopRepository = loopRepository;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.loopService = loopService;
        this.enterpriseService = enterpriseService;
    }

    @Scheduled(cron = "0 0/2 * * * ?") // Run every 2 minutes
    public void executeLoopScheduler() {
        log.info("Running CustomerMessageLoopTask to scan recurring scheduled broadcast tasks across all tenants...");
        List<WecomEnterprise> enterprises = enterpriseService.findAll();
        for (WecomEnterprise ent : enterprises) {
            String corpId = ent.getCorpId();
            try {
                // Switch database context
                DynamicDataSourceContextHolder.push(corpId);
                WxCpServiceManager.setCurrentCorpId(corpId);
                
                processTenantLoopTasks();
                
            } catch (Exception e) {
                log.error("Error processing loop tasks for tenant: {}", corpId, e);
            } finally {
                // Clear context
                DynamicDataSourceContextHolder.poll();
                WxCpServiceManager.clearCurrentCorpId();
            }
        }
    }

    private void processTenantLoopTasks() {
        List<WecomCustomerMessageLoop> loops = loopRepository.findByStatus(1);
        if (loops.isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        for (WecomCustomerMessageLoop loop : loops) {
            try {
                // Determine reference time for calculation
                LocalDateTime refTime = now;
                if (loop.getLastTriggerTime() != null && loop.getLastTriggerTime().isAfter(now.minusMinutes(5))) {
                    refTime = loop.getLastTriggerTime();
                }
                
                LocalDateTime nextExec = loopService.calculateNextExecutionTime(loop, refTime);
                if (nextExec == null) {
                    continue;
                }
                
                // Double check calculation with lastTriggerTime if it matches the current calculation
                if (loop.getLastTriggerTime() != null && !nextExec.isAfter(loop.getLastTriggerTime())) {
                    nextExec = loopService.calculateNextExecutionTime(loop, loop.getLastTriggerTime());
                }
                
                if (nextExec == null) {
                    continue;
                }
                
                // If the next execution time is within the next 5 minutes (and in the future)
                if (nextExec.isAfter(now) && nextExec.isBefore(now.plusMinutes(5))) {
                    // Trigger creation of the regular scheduled message task!
                    triggerTaskCreation(loop, nextExec);
                }
            } catch (Exception e) {
                log.error("Failed to process loop task {} of tenant", loop.getId(), e);
            }
        }
    }

    @Transactional
    public void triggerTaskCreation(WecomCustomerMessageLoop loop, LocalDateTime executionTime) throws Exception {
        log.info("Triggering loop task {} for occurrence time: {}", loop.getId(), executionTime);
        
        WecomCustomerMessage msg = new WecomCustomerMessage();
        String dateStr = executionTime.format(DateTimeFormatter.ofPattern("yyMMddHH"));
        msg.setTaskName(loop.getTaskName() + "_" + dateStr);
        msg.setSendType(1); // Scheduled
        msg.setSendTime(executionTime);
        msg.setTargetType(loop.getTargetType());
        msg.setTargetCondition(loop.getTargetCondition());
        msg.setContent(loop.getContent());
        msg.setAttachments(loop.getAttachments());
        msg.setSenderList(loop.getSenderList());
        msg.setStatus(0); // Pending
        msg.setCreatorUserid(loop.getCreatorUserid());
        
        // Calculate target count using public method in CustomerMessageService
        List<String> targetExternalUserids = messageService.findMatchedExternalUserids(msg);
        msg.setTargetCount(targetExternalUserids.size());
        
        messageRepository.save(msg);
        
        // Update last trigger time
        loop.setLastTriggerTime(executionTime);
        loopRepository.save(loop);
        
        log.info("Successfully generated message task for loop task {}, execution time: {}, targets: {}", 
                loop.getId(), executionTime, msg.getTargetCount());
    }
}
