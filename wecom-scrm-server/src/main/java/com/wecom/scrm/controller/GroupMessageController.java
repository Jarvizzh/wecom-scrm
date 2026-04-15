package com.wecom.scrm.controller;

import com.wecom.scrm.dto.GroupMessageDTO;
import com.wecom.scrm.entity.WecomGroupMessage;
import com.wecom.scrm.service.GroupMessageService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/group-message")
public class GroupMessageController {

    private final GroupMessageService messageService;

    public GroupMessageController(GroupMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<WecomGroupMessage> createMessage(@RequestBody GroupMessageDTO.CreateRequest request) throws Exception {
        String creatorUserid = "admin";
        return ResponseEntity.ok(messageService.createMessageTask(request, creatorUserid));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<WecomGroupMessage>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(messageService.listTasks(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WecomGroupMessage> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getTask(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WecomGroupMessage> updateTask(@PathVariable Long id,
            @RequestBody GroupMessageDTO.CreateRequest request) throws Exception {
        return ResponseEntity.ok(messageService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        messageService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/send-result")
    public ResponseEntity<GroupMessageDTO.SendResult> getSendResult(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(messageService.getSendResult(id));
    }
}
