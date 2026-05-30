package com.wecom.scrm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecom.scrm.dto.CustomerMessageLoopDTO;
import com.wecom.scrm.entity.WecomCustomerMessageLoop;
import com.wecom.scrm.repository.WecomCustomerMessageLoopRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CustomerMessageLoopService {

    private final WecomCustomerMessageLoopRepository loopRepository;
    private final ObjectMapper objectMapper;
    private final CustomerMessageService messageService;

    public CustomerMessageLoopService(WecomCustomerMessageLoopRepository loopRepository,
                                      ObjectMapper objectMapper,
                                      CustomerMessageService messageService) {
        this.loopRepository = loopRepository;
        this.objectMapper = objectMapper;
        this.messageService = messageService;
    }

    @Transactional
    public WecomCustomerMessageLoop createLoopTask(CustomerMessageLoopDTO.CreateRequest request, String creatorUserid)
            throws Exception {
        WecomCustomerMessageLoop loop = new WecomCustomerMessageLoop();
        loop.setTaskName(request.getTaskName());
        loop.setTargetType(request.getTargetType());
        if (request.getTargetCondition() != null) {
            loop.setTargetCondition(objectMapper.writeValueAsString(request.getTargetCondition()));
        }
        loop.setContent(request.getText());
        if (request.getAttachments() != null) {
            loop.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        }
        if (request.getSenderList() != null && !request.getSenderList().isEmpty()) {
            loop.setSenderList(objectMapper.writeValueAsString(request.getSenderList()));
        }
        
        loop.setLoopType(request.getLoopType());
        loop.setLoopDayOfWeek(request.getLoopDayOfWeek());
        loop.setSendTimeOfDay(request.getSendTimeOfDay());
        loop.setStatus(request.getStatus() != null ? request.getStatus() : 1); // 1 = Enabled by default
        loop.setCreatorUserid(creatorUserid);

        validateLoopConfig(loop);

        return loopRepository.save(loop);
    }

    @Transactional
    public WecomCustomerMessageLoop updateLoopTask(Long id, CustomerMessageLoopDTO.CreateRequest request)
            throws Exception {
        WecomCustomerMessageLoop loop = getLoopTask(id);
        
        loop.setTaskName(request.getTaskName());
        loop.setTargetType(request.getTargetType());
        if (request.getTargetCondition() != null) {
            loop.setTargetCondition(objectMapper.writeValueAsString(request.getTargetCondition()));
        } else {
            loop.setTargetCondition(null);
        }
        loop.setContent(request.getText());
        if (request.getAttachments() != null) {
            loop.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        } else {
            loop.setAttachments(null);
        }
        if (request.getSenderList() != null) {
            loop.setSenderList(objectMapper.writeValueAsString(request.getSenderList()));
        } else {
            loop.setSenderList(null);
        }

        loop.setLoopType(request.getLoopType());
        loop.setLoopDayOfWeek(request.getLoopDayOfWeek());
        loop.setSendTimeOfDay(request.getSendTimeOfDay());
        if (request.getStatus() != null) {
            loop.setStatus(request.getStatus());
        }

        validateLoopConfig(loop);

        return loopRepository.save(loop);
    }

    private void validateLoopConfig(WecomCustomerMessageLoop loop) {
        if (loop.getLoopType() == null || (loop.getLoopType() != 1 && loop.getLoopType() != 2)) {
            throw new IllegalArgumentException("无效的循环类型 / Invalid loop type");
        }
        if (loop.getLoopType() == 2 && (loop.getLoopDayOfWeek() == null || loop.getLoopDayOfWeek().trim().isEmpty())) {
            throw new IllegalArgumentException("每周循环模式下必须选择发送日 / Days of week must be provided for weekly loop");
        }
        if (loop.getSendTimeOfDay() == null || loop.getSendTimeOfDay().trim().isEmpty()) {
            throw new IllegalArgumentException("发送时间不能为空 / Send time of day must be provided");
        }
        // Try parsing the time of day
        try {
            LocalTime.parse(loop.getSendTimeOfDay());
        } catch (Exception e) {
            throw new IllegalArgumentException("发送时间格式无效 (应为 HH:mm:ss) / Invalid send time format");
        }
    }

    public Page<WecomCustomerMessageLoop> listLoopTasks(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return loopRepository.findAll(pageable);
    }

    public WecomCustomerMessageLoop getLoopTask(Long id) {
        return loopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loop task not found with id: " + id));
    }

    @Transactional
    public void deleteLoopTask(Long id) {
        WecomCustomerMessageLoop loop = getLoopTask(id);
        loopRepository.delete(loop);
    }

    public LocalDateTime calculateNextExecutionTime(WecomCustomerMessageLoop loop, LocalDateTime referenceTime) {
        if (loop.getSendTimeOfDay() == null) {
            return null;
        }
        LocalTime time = LocalTime.parse(loop.getSendTimeOfDay());
        if (loop.getLoopType() == 1) { // Daily
            LocalDateTime candidate = LocalDateTime.of(referenceTime.toLocalDate(), time);
            if (!candidate.isAfter(referenceTime)) {
                candidate = candidate.plusDays(1);
            }
            return candidate;
        } else if (loop.getLoopType() == 2) { // Weekly
            if (loop.getLoopDayOfWeek() == null || loop.getLoopDayOfWeek().trim().isEmpty()) {
                return null;
            }
            String[] daysStr = loop.getLoopDayOfWeek().split(",");
            List<Integer> days = new ArrayList<>();
            for (String d : daysStr) {
                try {
                    days.add(Integer.parseInt(d.trim()));
                } catch (NumberFormatException ignored) {}
            }
            if (days.isEmpty()) {
                return null;
            }
            // Find the closest upcoming day of the week
            for (int i = 0; i <= 7; i++) {
                LocalDate candidateDate = referenceTime.toLocalDate().plusDays(i);
                int dayValue = candidateDate.getDayOfWeek().getValue(); // 1=Mon, 7=Sun
                if (days.contains(dayValue)) {
                    LocalDateTime candidate = LocalDateTime.of(candidateDate, time);
                    if (candidate.isAfter(referenceTime)) {
                        return candidate;
                    }
                }
            }
        }
        return null;
    }
}
