package com.wecom.scrm.controller;

import com.wecom.scrm.dto.CustomerMessageDTO;
import com.wecom.scrm.entity.WecomCustomerMessage;
import com.wecom.scrm.service.CustomerMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer-message")
public class CustomerMessageController {

    private final CustomerMessageService messageService;

    public CustomerMessageController(CustomerMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping
    public ResponseEntity<WecomCustomerMessage> createMessage(@RequestBody CustomerMessageDTO.CreateRequest request) throws Exception {
        // For simplicity, hardcoding creator for now or get from context if auth was implemented
        String creatorUserid = "admin"; 
        return ResponseEntity.ok(messageService.createMessageTask(request, creatorUserid));
    }

    @GetMapping("/list")
    public ResponseEntity<List<WecomCustomerMessage>> listTasks() {
        return ResponseEntity.ok(messageService.listTasks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WecomCustomerMessage> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getTask(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WecomCustomerMessage> updateTask(@PathVariable Long id, @RequestBody CustomerMessageDTO.CreateRequest request) throws Exception {
        return ResponseEntity.ok(messageService.updateTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        messageService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/send-result")
    public ResponseEntity<CustomerMessageDTO.SendResult> getSendResult(@PathVariable Long id) throws Exception {
        return ResponseEntity.ok(messageService.getSendResult(id));
    }

    @GetMapping("/{id}/send-result/{userid}")
    public ResponseEntity<CustomerMessageDTO.MemberSendDetail> getMemberSendDetail(@PathVariable Long id, @PathVariable String userid) throws Exception {
        return ResponseEntity.ok(messageService.getMemberSendDetail(id, userid));
    }
}
