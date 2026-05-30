package com.wecom.scrm.controller;

import com.wecom.scrm.dto.CustomerMessageLoopDTO;
import com.wecom.scrm.entity.WecomCustomerMessageLoop;
import com.wecom.scrm.service.CustomerMessageLoopService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer-message-loop")
public class CustomerMessageLoopController {

    private final CustomerMessageLoopService loopService;

    public CustomerMessageLoopController(CustomerMessageLoopService loopService) {
        this.loopService = loopService;
    }

    @PostMapping
    public ResponseEntity<WecomCustomerMessageLoop> createLoopTask(@RequestBody CustomerMessageLoopDTO.CreateRequest request) throws Exception {
        // Hardcode creator for simplicity or get from auth context if needed
        String creatorUserid = "admin";
        return ResponseEntity.ok(loopService.createLoopTask(request, creatorUserid));
    }

    @GetMapping("/list")
    public ResponseEntity<Page<WecomCustomerMessageLoop>> listLoopTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(loopService.listLoopTasks(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WecomCustomerMessageLoop> getLoopTask(@PathVariable Long id) {
        return ResponseEntity.ok(loopService.getLoopTask(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WecomCustomerMessageLoop> updateLoopTask(@PathVariable Long id, @RequestBody CustomerMessageLoopDTO.CreateRequest request) throws Exception {
        return ResponseEntity.ok(loopService.updateLoopTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoopTask(@PathVariable Long id) {
        loopService.deleteLoopTask(id);
        return ResponseEntity.ok().build();
    }
}
