package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomSyncLog;
import com.wecom.scrm.service.SyncLogService;
import com.wecom.scrm.service.SyncService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/sync")
public class AdminSyncController {

    private final SyncService syncService;
    private final SyncLogService syncLogService;

    public AdminSyncController(SyncService syncService, SyncLogService syncLogService) {
        this.syncService = syncService;
        this.syncLogService = syncLogService;
    }

    @GetMapping("/logs")
    public Page<WecomSyncLog> getSyncLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return syncLogService.getSyncLogs(page, size);
    }

    @RequestMapping("/users")
    public ResponseEntity<String> syncUsers() {
        // Run async in a real app, but for MVP synchronous is fine to see immediate
        // errors
        syncService.syncAllDepartments();
        syncService.syncAllUsers();
        return ResponseEntity.ok("User sync started in background.");
    }

    @RequestMapping("/departments")
    public ResponseEntity<String> syncDepartments() {
        syncService.syncAllDepartments();
        return ResponseEntity.ok("Department sync started in background.");
    }

    @RequestMapping("/customers")
    public ResponseEntity<String> syncCustomers() {
        syncService.syncAllCustomers();
        return ResponseEntity.ok("Customer sync started in background.");
    }

    @PostMapping("/group-chats")
    public ResponseEntity<String> syncGroupChats() {
        syncService.syncGroupChats();
        return ResponseEntity.ok("Group-chat sync started in background.");
    }

    @RequestMapping("/tags")
    public ResponseEntity<String> syncTags() {
        syncService.syncCorpTags();
        return ResponseEntity.ok("Tag sync started in background.");
    }
}
