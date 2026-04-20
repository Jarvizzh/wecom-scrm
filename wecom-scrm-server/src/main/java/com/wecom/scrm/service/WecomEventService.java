package com.wecom.scrm.service;

import com.wecom.scrm.config.WxCpServiceManager;
import com.wecom.scrm.entity.WecomEventLog;
import com.wecom.scrm.repository.WecomEventLogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.wecom.scrm.dto.WecomEventLogDTO;
import com.wecom.scrm.entity.WecomUser;
import com.wecom.scrm.entity.WecomCustomer;
import com.wecom.scrm.listener.event.WecomEvent;
import com.wecom.scrm.repository.WecomUserRepository;
import com.wecom.scrm.repository.WecomCustomerRepository;
import com.wecom.scrm.repository.GroupChatRepository;
import com.wecom.scrm.entity.WecomGroupChat;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WecomEventService {

    private final WecomEventLogRepository eventLogRepository;
    private final WecomUserRepository userRepository;
    private final WecomCustomerRepository customerRepository;
    private final GroupChatRepository groupChatRepository;
    private final WxCpServiceManager wxCpServiceManager;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final WecomEventService self;

    public WecomEventService(WecomEventLogRepository eventLogRepository,
            WecomUserRepository userRepository,
            WecomCustomerRepository customerRepository,
            GroupChatRepository groupChatRepository,
            WxCpServiceManager wxCpServiceManager,
            ObjectMapper objectMapper,
            ApplicationEventPublisher eventPublisher,
            @Lazy WecomEventService self) {
        this.eventLogRepository = eventLogRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.groupChatRepository = groupChatRepository;
        this.wxCpServiceManager = wxCpServiceManager;
        this.objectMapper = objectMapper;
        this.eventPublisher = eventPublisher;
        this.self = self;
    }

    /**
     * Entry point for asynchronous callback handling. 
     * This moves the DB transaction out of the Web thread.
     */
    @Async("eventSaveExecutor")
    public void asyncSaveEvent(String corpId, WxCpXmlMessage msg) {
        log.debug("Async callback ingestion for corpId: {}", corpId);
        try {
            // Call the transactional method via proxy to ensure @Transactional works
            self.saveEvent(corpId, msg);
        } catch (Exception e) {
            log.error("Failed to async save WeCom event for corpId: {}", corpId, e);
        }
    }

    @Transactional
    public WecomEventLog saveEvent(String corpId, WxCpXmlMessage msg) {
        WecomEventLog eventLog = new WecomEventLog();
        eventLog.setCorpId(corpId);
        eventLog.setMsgType(msg.getMsgType());
        eventLog.setEvent(msg.getEvent());
        eventLog.setChangeType(msg.getChangeType());
        eventLog.setExternalUserid(msg.getExternalUserId());
        eventLog.setUserid(msg.getUserId());
        eventLog.setStatus(0); // Pending
        eventLog.setCreateTime(LocalDateTime.now());

        try {
            eventLog.setContent(objectMapper.writeValueAsString(msg));
        } catch (Exception e) {
            log.error("Failed to serialize WeCom message", e);
            eventLog.setContent("{}");
        }

        WecomEventLog savedLog = eventLogRepository.save(eventLog);
        
        // Publish event for asynchronous processing
        eventPublisher.publishEvent(new WecomEvent(this, savedLog));
        
        return savedLog;
    }

    /**
     * Processes a single event log.
     * Note: Not @Transactional at this level to avoid long-running transactions 
     * while calling external WeCom APIs.
     */
    public void processEvent(WecomEventLog eventLog) {
        // Atomic claim: try to set status to 3 (Processing) if current status is 0 or 2
        int updated = eventLogRepository.claimEvent(eventLog.getId(), LocalDateTime.now());
        if (updated == 0) {
            log.debug("Event {} already being processed or completed by another thread, skipping.", eventLog.getId());
            return;
        }

        log.info("Processing WeCom event: {} for corpId: {}. Current DS: {}",
                eventLog.getId(), eventLog.getCorpId(), DynamicDataSourceContextHolder.peek());
        
        try {
            // Ensure context is set (important for multi-tenant background tasks)
            WxCpServiceManager.setCurrentCorpId(eventLog.getCorpId());

            WxCpMessageRouter router = wxCpServiceManager.getWxCpMessageRouter(eventLog.getCorpId());
            if (router == null) {
                throw new RuntimeException("No message router found for corpId: " + eventLog.getCorpId());
            }

            WxCpXmlMessage inMessage = objectMapper.readValue(eventLog.getContent(), WxCpXmlMessage.class);
            router.route(inMessage);

            eventLog.setStatus(1); // Success
            eventLog.setErrorMsg(null);
        } catch (Exception e) {
            log.error("Failed to process event log: {}", eventLog.getId(), e);
            eventLog.setStatus(2); // Failed
            eventLog.setErrorMsg(e.getMessage());
            eventLog.setRetryCount(eventLog.getRetryCount() + 1);
        } finally {
            eventLog.setUpdateTime(LocalDateTime.now());
            // Use saveAndFlush to ensure state is committed and visible to other threads/tasks
            eventLogRepository.saveAndFlush(eventLog);
        }
    }

    @Transactional(readOnly = true)
    public Page<WecomEventLogDTO> getEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<WecomEventLog> eventPage = eventLogRepository.findAll(pageable);

        List<WecomEventLogDTO> dtoList = eventPage.getContent().stream().map(eventLog -> {
            WecomEventLogDTO dto = new WecomEventLogDTO();
            BeanUtils.copyProperties(eventLog, dto);

            try {
                WxCpXmlMessage msg = objectMapper.readValue(eventLog.getContent(), WxCpXmlMessage.class);
                dto.setChatId(msg.getChatId());
                dto.setUpdateDetail(msg.getUpdateDetail());

                // Extract memberId for group chat events
                if ("change_external_chat".equals(msg.getEvent())) {
                    Map<String, Object> allFields = msg.getAllFieldsMap();
                    if (allFields != null && allFields.containsKey("MemChangeList")) {
                        Object mcl = allFields.get("MemChangeList");
                        if (mcl instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> memChangeList = (Map<String, Object>) mcl;
                            Object itemObj = memChangeList.get("Item");
                            if (itemObj instanceof List && !((List<?>) itemObj).isEmpty()) {
                                dto.setMemberId(((List<?>) itemObj).get(0).toString());
                            } else if (itemObj instanceof String) {
                                dto.setMemberId((String) itemObj);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse event content for DTO: {}", eventLog.getId(), e);
            }
            return dto;
        }).collect(Collectors.toList());

        // Collect IDs for bulk lookup
        Set<String> userids = dtoList.stream()
                .map(WecomEventLogDTO::getUserid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> externalUserids = dtoList.stream()
                .map(WecomEventLogDTO::getExternalUserid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> chatIds = dtoList.stream()
                .map(WecomEventLogDTO::getChatId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> memberIds = dtoList.stream()
                .map(WecomEventLogDTO::getMemberId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Fetch names and avatars
        Map<String, WecomUser> userMap = new HashMap<>();
        if (!userids.isEmpty()) {
            userRepository.findByUseridIn(new ArrayList<>(userids))
                    .forEach(u -> userMap.put(u.getUserid(), u));
        }

        Map<String, WecomCustomer> customerMap = new HashMap<>();
        if (!externalUserids.isEmpty()) {
            customerRepository.findByExternalUseridIn(new ArrayList<>(externalUserids))
                    .forEach(c -> customerMap.put(c.getExternalUserid(), c));
        }

        if (!memberIds.isEmpty()) {
            customerRepository.findByExternalUseridIn(new ArrayList<>(memberIds))
                    .forEach(c -> customerMap.putIfAbsent(c.getExternalUserid(), c));
        }

        Map<String, WecomGroupChat> groupMap = new HashMap<>();
        if (!chatIds.isEmpty()) {
            groupChatRepository.findByChatIdIn(new ArrayList<>(chatIds))
                    .forEach(g -> groupMap.put(g.getChatId(), g));
        }

        return eventPage.map(eventLog -> {
            // Find the dto we already prepared
            WecomEventLogDTO dto = dtoList.stream().filter(d -> d.getId().equals(eventLog.getId())).findFirst()
                    .orElse(new WecomEventLogDTO());

            if (dto.getUserid() != null && userMap.containsKey(dto.getUserid())) {
                WecomUser user = userMap.get(dto.getUserid());
                dto.setUserName(user.getName());
                dto.setUserAvatar(user.getAvatar());
            }

            if (dto.getExternalUserid() != null && customerMap.containsKey(dto.getExternalUserid())) {
                WecomCustomer customer = customerMap.get(dto.getExternalUserid());
                dto.setExternalUserName(customer.getName());
                dto.setExternalUserAvatar(customer.getAvatar());
            }

            if (dto.getChatId() != null && groupMap.containsKey(dto.getChatId())) {
                dto.setGroupName(groupMap.get(dto.getChatId()).getName());
            }

            if (dto.getMemberId() != null) {
                if (customerMap.containsKey(dto.getMemberId())) {
                    WecomCustomer customer = customerMap.get(dto.getMemberId());
                    dto.setMemberName(customer.getName());
                    dto.setMemberAvatar(customer.getAvatar());
                } else {
                    dto.setMemberName(dto.getMemberId()); // Fallback to ID
                }
            }

            return dto;
        });
    }

    @Transactional
    public void cleanupOldEvents() {
        // Delete successfully processed events older than 30 days
        LocalDateTime cleanupThreshold = LocalDateTime.now().minusDays(30);
        eventLogRepository.deleteOldEvents(cleanupThreshold);
        log.info("Cleaned up WeCom events older than {}", cleanupThreshold);
    }
}
