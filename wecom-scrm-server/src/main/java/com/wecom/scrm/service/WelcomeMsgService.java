package com.wecom.scrm.service;

import com.wecom.scrm.entity.WecomWelcomeMsg;
import com.wecom.scrm.repository.WecomWelcomeMsgRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class WelcomeMsgService {

    private final WecomWelcomeMsgRepository welcomeMsgRepository;

    public WelcomeMsgService(WecomWelcomeMsgRepository welcomeMsgRepository) {
        this.welcomeMsgRepository = welcomeMsgRepository;
    }

    public Page<WecomWelcomeMsg> getWelcomeMsgs(int page, int size) {
        return welcomeMsgRepository.findAll(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "sysUpdateTime")));
    }

    public Optional<WecomWelcomeMsg> getById(Long id) {
        return welcomeMsgRepository.findById(id);
    }

    @Transactional
    public WecomWelcomeMsg save(WecomWelcomeMsg welcomeMsg) {
        log.info("Saving welcome message: {}", welcomeMsg.getName());
        
        LocalDateTime now = LocalDateTime.now();
        if (welcomeMsg.getId() == null) {
            welcomeMsg.setSysCreateTime(now);
        }
        welcomeMsg.setSysUpdateTime(now);

        if (welcomeMsg.getIsDefault() != null && welcomeMsg.getIsDefault() == 1) {
            // Reset other defaults
            List<WecomWelcomeMsg> all = welcomeMsgRepository.findAll();
            for (WecomWelcomeMsg msg : all) {
                if (welcomeMsg.getId() == null || !msg.getId().equals(welcomeMsg.getId())) {
                    if (msg.getIsDefault() != null && msg.getIsDefault() == 1) {
                        msg.setIsDefault(0);
                        msg.setSysUpdateTime(now);
                        welcomeMsgRepository.save(msg);
                    }
                }
            }
        }
        return welcomeMsgRepository.save(welcomeMsg);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting welcome message id: {}", id);
        welcomeMsgRepository.deleteById(id);
    }
}
