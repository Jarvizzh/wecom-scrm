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
import com.wecom.scrm.repository.WecomUserRepository;
import com.wecom.scrm.repository.WecomCustomerRepository;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WecomEventService {

    private final WecomEventLogRepository eventLogRepository;
    private final WecomUserRepository userRepository;
    private final WecomCustomerRepository customerRepository;
    private final WxCpServiceManager wxCpServiceManager;
    private final ObjectMapper objectMapper;

    public WecomEventService(WecomEventLogRepository eventLogRepository, 
                             WecomUserRepository userRepository,
                             WecomCustomerRepository customerRepository,
                             WxCpServiceManager wxCpServiceManager,
                             ObjectMapper objectMapper) {
        this.eventLogRepository = eventLogRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.wxCpServiceManager = wxCpServiceManager;
        this.objectMapper = objectMapper;
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
        
        return eventLogRepository.save(eventLog);
    }

    @Transactional
    public void processEvent(WecomEventLog eventLog) {
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
            eventLogRepository.save(eventLog);
        }
    }

    @Transactional(readOnly = true)
    public Page<WecomEventLogDTO> getEvents(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<WecomEventLog> eventPage = eventLogRepository.findAll(pageable);
        
        // Collect IDs for bulk lookup
        Set<String> userids = eventPage.getContent().stream()
                .map(WecomEventLog::getUserid)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        Set<String> externalUserids = eventPage.getContent().stream()
                .map(WecomEventLog::getExternalUserid)
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
        
        return eventPage.map(log -> {
            WecomEventLogDTO dto = new WecomEventLogDTO();
            BeanUtils.copyProperties(log, dto);
            
            if (log.getUserid() != null && userMap.containsKey(log.getUserid())) {
                WecomUser user = userMap.get(log.getUserid());
                dto.setUserName(user.getName());
                dto.setUserAvatar(user.getAvatar());
            }
            
            if (log.getExternalUserid() != null && customerMap.containsKey(log.getExternalUserid())) {
                WecomCustomer customer = customerMap.get(log.getExternalUserid());
                dto.setExternalUserName(customer.getName());
                dto.setExternalUserAvatar(customer.getAvatar());
            }
            
            return dto;
        });
    }

    @Transactional
    public void cleanupOldEvents() {
        // Delete successfully processed events older than 3 days
        LocalDateTime cleanupThreshold = LocalDateTime.now().minusDays(3);
        eventLogRepository.deleteOldEvents(cleanupThreshold);
        log.info("Cleaned up WeCom events older than {}", cleanupThreshold);
    }
}
