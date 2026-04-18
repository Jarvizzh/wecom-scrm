package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomWelcomeMsg;
import com.wecom.scrm.service.WelcomeMsgService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/welcome-msg")
public class WelcomeMsgController {

    private final WelcomeMsgService welcomeMsgService;

    public WelcomeMsgController(WelcomeMsgService welcomeMsgService) {
        this.welcomeMsgService = welcomeMsgService;
    }

    @GetMapping
    public Page<WecomWelcomeMsg> getWelcomeMsgs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return welcomeMsgService.getWelcomeMsgs(page, size);
    }

    @GetMapping("/{id}")
    public WecomWelcomeMsg getWelcomeMsg(@PathVariable Long id) {
        return welcomeMsgService.getById(id).orElse(null);
    }

    @PostMapping
    public WecomWelcomeMsg saveWelcomeMsg(@RequestBody WecomWelcomeMsg welcomeMsg) {
        return welcomeMsgService.save(welcomeMsg);
    }

    @DeleteMapping("/{id}")
    public void deleteWelcomeMsg(@PathVariable Long id) {
        welcomeMsgService.delete(id);
    }
}
