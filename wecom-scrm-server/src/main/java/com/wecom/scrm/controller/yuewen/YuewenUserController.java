package com.wecom.scrm.controller.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenConsumeRecordDTO;
import com.wecom.scrm.thirdparty.api.yuewen.dto.YuewenSyncRequest;
import com.wecom.scrm.dto.yuewen.YuewenUserDTO;
import com.wecom.scrm.service.yuewen.YuewenConsumeService;
import com.wecom.scrm.service.yuewen.YuewenSyncService;
import com.wecom.scrm.service.yuewen.YuewenUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/yuewen/user")
@RequiredArgsConstructor
public class YuewenUserController {

    private final YuewenUserService userService;
    private final YuewenConsumeService consumeService;
    private final YuewenSyncService syncService;

    @GetMapping
    public Page<YuewenUserDTO> list(
            @RequestParam(required = false) String appFlag,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Long minAmount,
            @RequestParam(required = false) Long maxAmount,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.listUsers(appFlag, openid, nickname, minAmount, maxAmount, sortField, sortOrder, page, size);
    }

    @GetMapping("/customer/{externalUserid}")
    public List<YuewenUserDTO> getByCustomer(@PathVariable String externalUserid) {
        return userService.findByCustomer(externalUserid);
    }

    @GetMapping("/consume")
    public Page<YuewenConsumeRecordDTO> listConsume(
            @RequestParam(required = false) String appFlag,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) Long guid,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String sortField,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return consumeService.listConsumeRecords(appFlag, openid, guid, bookName, sortField, sortOrder, page, size);
    }

    @PostMapping("/sync/consume")
    public ResponseEntity<String> syncConsume(@RequestBody YuewenSyncRequest request) {
        syncService.manualSyncConsume(request.getAppFlag(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok("Sync task started successfully");
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncUsers(@RequestBody YuewenSyncRequest request) {
        syncService.manualSync(request.getAppFlag(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok("User sync task started successfully");
    }
}
