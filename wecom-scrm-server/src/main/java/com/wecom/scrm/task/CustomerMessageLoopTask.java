package com.wecom.scrm.task;

import com.wecom.scrm.entity.WecomCustomerMessage;
import com.wecom.scrm.entity.WecomCustomerMessageLoop;
import com.wecom.scrm.entity.WecomEnterprise;
import com.wecom.scrm.entity.WecomMaterial;
import com.wecom.scrm.repository.WecomCustomerMessageLoopRepository;
import com.wecom.scrm.repository.WecomCustomerMessageRepository;
import com.wecom.scrm.repository.WecomMaterialRepository;
import com.wecom.scrm.service.CustomerMessageLoopService;
import com.wecom.scrm.service.CustomerMessageService;
import com.wecom.scrm.service.WecomEnterpriseService;
import com.wecom.scrm.dto.WecomAttachmentDTO;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.config.WxCpServiceManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class CustomerMessageLoopTask {

    private final WecomCustomerMessageLoopRepository loopRepository;
    private final WecomCustomerMessageRepository messageRepository;
    private final CustomerMessageService messageService;
    private final CustomerMessageLoopService loopService;
    private final WecomEnterpriseService enterpriseService;
    private final WecomMaterialRepository materialRepository;
    private final ObjectMapper objectMapper;

    public CustomerMessageLoopTask(WecomCustomerMessageLoopRepository loopRepository,
                                   WecomCustomerMessageRepository messageRepository,
                                   CustomerMessageService messageService,
                                   CustomerMessageLoopService loopService,
                                   WecomEnterpriseService enterpriseService,
                                   WecomMaterialRepository materialRepository,
                                   ObjectMapper objectMapper) {
        this.loopRepository = loopRepository;
        this.messageRepository = messageRepository;
        this.messageService = messageService;
        this.loopService = loopService;
        this.enterpriseService = enterpriseService;
        this.materialRepository = materialRepository;
        this.objectMapper = objectMapper;
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

        // Auto-update attachment titles from Material Library if enabled
        String attachmentsJson = loop.getAttachments();
        if (Integer.valueOf(1).equals(loop.getAutoUpdateAttachmentTitle()) && attachmentsJson != null && !attachmentsJson.trim().isEmpty()) {
            try {
                List<WecomAttachmentDTO> currentAtts = objectMapper.readValue(attachmentsJson, new TypeReference<List<WecomAttachmentDTO>>() {});
                List<WecomAttachmentDTO> targetAtts = currentAtts.stream()
                        .filter(att -> ("link".equals(att.getMsgtype()) && att.getLink() != null) || ("miniprogram".equals(att.getMsgtype()) && att.getMiniprogram() != null))
                        .collect(Collectors.toList());
                int n = targetAtts.size();
                if (n > 0) {
                    // Query all TEXT materials
                    org.springframework.data.domain.Page<WecomMaterial> materialsPage = materialRepository.findByFilters("TEXT", null, null, PageRequest.of(0, 1000));
                    List<WecomMaterial> materials = materialsPage.getContent();
                    List<String> availableTitles = materials.stream()
                            .map(WecomMaterial::getContent)
                            .filter(org.springframework.util.StringUtils::hasText)
                            .distinct()
                            .collect(Collectors.toList());
                    int m = availableTitles.size();
                    if (m >= n) {
                        // Extract last used titles (which are on the loop task's attachments right now)
                        Set<String> lastUsedTitles = targetAtts.stream()
                                .map(att -> "link".equals(att.getMsgtype()) ? att.getLink().getTitle() : att.getMiniprogram().getTitle())
                                .filter(org.springframework.util.StringUtils::hasText)
                                .collect(Collectors.toSet());

                        // Candidates: availableTitles - lastUsedTitles
                        List<String> candidates = new ArrayList<>(availableTitles);
                        candidates.removeAll(lastUsedTitles);
                        Collections.shuffle(candidates);

                        if (candidates.size() < n) {
                            List<String> lastUsedCandidates = new ArrayList<>(availableTitles);
                            lastUsedCandidates.retainAll(lastUsedTitles);
                            Collections.shuffle(lastUsedCandidates);
                            candidates.addAll(lastUsedCandidates);
                        }

                        // Assign
                        int idx = 0;
                        for (WecomAttachmentDTO att : currentAtts) {
                            if ("link".equals(att.getMsgtype()) && att.getLink() != null) {
                                att.getLink().setTitle(candidates.get(idx++));
                            } else if ("miniprogram".equals(att.getMsgtype()) && att.getMiniprogram() != null) {
                                att.getMiniprogram().setTitle(candidates.get(idx++));
                            }
                        }

                        String updatedJson = objectMapper.writeValueAsString(currentAtts);
                        loop.setAttachments(updatedJson);
                        msg.setAttachments(updatedJson);
                    } else {
                        log.info("Material library unique text copy count {} is less than target attachment count {}, skipping auto-update.", m, n);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to auto-update attachment titles for loop task {}", loop.getId(), e);
            }
        }
        
        // Calculate target count using public method in CustomerMessageService
        List<String> targetExternalUserids = messageService.findMatchedExternalUserids(msg);
        msg.setTargetCount(targetExternalUserids.size());
        
        messageRepository.save(msg);
        
        // Update last trigger time and attachments if updated
        loop.setLastTriggerTime(executionTime);
        loopRepository.save(loop);
        
        log.info("Successfully generated message task for loop task {}, execution time: {}, targets: {}", 
                loop.getId(), executionTime, msg.getTargetCount());
    }
}
