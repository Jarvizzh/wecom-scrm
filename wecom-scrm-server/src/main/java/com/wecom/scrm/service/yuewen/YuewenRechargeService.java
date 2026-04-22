package com.wecom.scrm.service.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenRechargeRecordDTO;
import com.wecom.scrm.entity.yuewen.YuewenRechargeRecord;
import com.wecom.scrm.repository.yuewen.YuewenRechargeRecordRepository;
import com.wecom.scrm.repository.yuewen.YuewenUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class YuewenRechargeService {

    private final YuewenRechargeRecordRepository rechargeRepository;
    private final YuewenUserRepository userRepository;
    private final YuewenSyncService syncService;

    public Page<YuewenRechargeRecordDTO> getRecords(int page, int size, String appFlag, String openid, String nickname, String orderId) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "payTime"));
        return rechargeRepository.findRecords(appFlag, openid, nickname, orderId, pageable);
    }


    public void syncRecords(String appFlag, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusMonths(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        syncService.manualSyncRecharge(appFlag, startTime, endTime);
    }
}
