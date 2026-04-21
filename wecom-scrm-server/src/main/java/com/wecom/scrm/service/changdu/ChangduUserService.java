package com.wecom.scrm.service.changdu;

import com.wecom.scrm.entity.changdu.ChangduUser;
import com.wecom.scrm.repository.changdu.ChangduUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangduUserService {

    private final ChangduUserRepository userRepository;
    private final ChangduSyncService syncService;

    public Page<ChangduUser> getUsers(int page, int size, Long distributorId, String openId, String nickname, String sortField, String sortOrder) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        if (sortField != null && !sortField.isEmpty()) {
            Sort.Direction direction = "ascending".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortField);
        }
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        return userRepository.findUsers(distributorId, openId, nickname, pageable);
    }

    public void syncUsers(Long distributorId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusYears(1);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }
        long startTs = startTime.toEpochSecond(ZoneOffset.ofHours(8));
        long endTs = endTime.toEpochSecond(ZoneOffset.ofHours(8));
        syncService.syncUsers(distributorId, startTs, endTs);
    }

    public List<ChangduUser> findByCustomer(String externalUserid) {
        return userRepository.findByExternalId(externalUserid);
    }
}
