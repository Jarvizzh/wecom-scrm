package com.wecom.scrm.controller.changdu;

import com.wecom.scrm.dto.changdu.ChangduRechargeRecordDTO;
import com.wecom.scrm.entity.changdu.ChangduRechargeRecord;
import com.wecom.scrm.service.changdu.ChangduRechargeRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/changdu/recharge")
@RequiredArgsConstructor
public class ChangduRechargeRecordController {

    private final ChangduRechargeRecordService recordService;

    @GetMapping
    public Page<ChangduRechargeRecordDTO> getRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long distributorId,
            @RequestParam(required = false) String openId,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) Long tradeNo) {
        return recordService.getRecords(page, size, distributorId, openId, nickname, tradeNo);
    }

    @PostMapping("/sync")
    public void syncRecords(
            @RequestParam Long distributorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        recordService.syncRecords(distributorId, startTime, endTime);
    }
}
