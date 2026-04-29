package com.wecom.scrm.service.changdu;

import com.wecom.scrm.dto.changdu.ChangduRechargeRecordDTO;
import com.wecom.scrm.entity.changdu.ChangduRechargeRecord;
import com.wecom.scrm.repository.changdu.ChangduRechargeRecordRepository;
import com.wecom.scrm.repository.changdu.ChangduUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class ChangduRechargeRecordService {

    private final ChangduRechargeRecordRepository recordRepository;
    private final ChangduUserRepository userRepository;
    private final ChangduSyncService syncService;

    public Page<ChangduRechargeRecordDTO> getRecords(int page, int size, Long distributorId, String openId, String nickname, Long tradeNo) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "payTime"));
        return recordRepository.findRecords(distributorId, openId, tradeNo, nickname, pageable);
    }

    @Async("bizAsyncExecutor")
    public void syncRecords(Long distributorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        long startTs = startTime.toEpochSecond(ZoneOffset.ofHours(8));
        long endTs = endTime.toEpochSecond(ZoneOffset.ofHours(8));
        syncService.syncRecharge(distributorId, startTs, endTs);
    }
}
