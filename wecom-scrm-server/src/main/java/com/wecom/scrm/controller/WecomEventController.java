package com.wecom.scrm.controller;

import com.wecom.scrm.dto.WecomEventLogDTO;
import com.wecom.scrm.service.WecomEventService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/wecom/events")
public class WecomEventController {

    private final WecomEventService wecomEventService;

    public WecomEventController(WecomEventService wecomEventService) {
        this.wecomEventService = wecomEventService;
    }

    @GetMapping
    public Page<WecomEventLogDTO> getEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return wecomEventService.getEvents(page, size);
    }
}
