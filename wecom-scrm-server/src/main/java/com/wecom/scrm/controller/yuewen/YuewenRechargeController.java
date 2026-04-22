package com.wecom.scrm.controller.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenRechargeRecordDTO;
import com.wecom.scrm.entity.yuewen.YuewenRechargeRecord;
import com.wecom.scrm.service.yuewen.YuewenRechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/yuewen/recharge")
@RequiredArgsConstructor
public class YuewenRechargeController {

    private final YuewenRechargeService rechargeService;

    @GetMapping
    public Page<YuewenRechargeRecordDTO> getRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String appFlag,
            @RequestParam(required = false) String openid,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String orderId) {
        return rechargeService.getRecords(page, size, appFlag, openid, nickname, orderId);
    }

    @PostMapping("/sync")
    public void syncRecords(
            @RequestParam String appFlag,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        rechargeService.syncRecords(appFlag, startTime, endTime);
    }
}
