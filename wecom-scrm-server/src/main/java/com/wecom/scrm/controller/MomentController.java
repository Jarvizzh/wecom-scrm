package com.wecom.scrm.controller;

import com.wecom.scrm.dto.MomentDTO;
import com.wecom.scrm.entity.WecomMoment;
import com.wecom.scrm.entity.WecomMomentRecord;
import com.wecom.scrm.service.MomentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/moments")
public class MomentController {

    private final MomentService momentService;

    public MomentController(MomentService momentService) {
        this.momentService = momentService;
    }

    @PostMapping
    public ResponseEntity<WecomMoment> createMoment(@RequestBody MomentDTO.CreateRequest request) throws Exception {
        // Creator should be retrieved from session/token, for MVP we use a dummy ID
        String creatorUserid = "admin";
        WecomMoment moment = momentService.createMomentTask(request, creatorUserid);
        return ResponseEntity.ok(moment);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<WecomMoment>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(momentService.listMoments(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<WecomMoment> getTask(@PathVariable Long id) {
        return momentService.getMomentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<WecomMoment> updateTask(@PathVariable Long id, @RequestBody MomentDTO.CreateRequest request) throws Exception {
        return ResponseEntity.ok(momentService.updateMomentTask(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        momentService.deleteMomentTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/records")
    public ResponseEntity<List<WecomMomentRecord>> listRecords(@PathVariable Long id) {
        return ResponseEntity.ok(momentService.getRecords(id));
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncStatuses() {
        new Thread(momentService::syncAllMomentStatuses).start();
        return ResponseEntity.ok("Sync task started in background.");
    }
}
