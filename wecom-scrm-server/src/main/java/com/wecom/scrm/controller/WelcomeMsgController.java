package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomWelcomeMsg;
import com.wecom.scrm.repository.WecomWelcomeMsgRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/welcome-msg")
public class WelcomeMsgController {

    private static final Logger log = LoggerFactory.getLogger(WelcomeMsgController.class);
    private final WecomWelcomeMsgRepository welcomeMsgRepository;

    public WelcomeMsgController(WecomWelcomeMsgRepository welcomeMsgRepository) {
        this.welcomeMsgRepository = welcomeMsgRepository;
    }

    @GetMapping
    public List<WecomWelcomeMsg> getWelcomeMsgs() {
        return welcomeMsgRepository.findAll();
    }

    @GetMapping("/{id}")
    public WecomWelcomeMsg getWelcomeMsg(@PathVariable Long id) {
        return welcomeMsgRepository.findById(id).orElse(null);
    }

    @PostMapping
    public WecomWelcomeMsg saveWelcomeMsg(@RequestBody WecomWelcomeMsg welcomeMsg) {
        log.info("Saving welcome message: {}", welcomeMsg.getName());
        if (welcomeMsg.getIsDefault() != null && welcomeMsg.getIsDefault() == 1) {
            // Reset other defaults
            List<WecomWelcomeMsg> all = welcomeMsgRepository.findAll();
            for (WecomWelcomeMsg msg : all) {
                if (welcomeMsg.getId() == null || !msg.getId().equals(welcomeMsg.getId())) {
                    if (msg.getIsDefault() != null && msg.getIsDefault() == 1) {
                        msg.setIsDefault(0);
                        welcomeMsgRepository.save(msg);
                    }
                }
            }
        }
        return welcomeMsgRepository.save(welcomeMsg);
    }

    @DeleteMapping("/{id}")
    public void deleteWelcomeMsg(@PathVariable Long id) {
        log.info("Deleting welcome message id: {}", id);
        welcomeMsgRepository.deleteById(id);
    }
}
