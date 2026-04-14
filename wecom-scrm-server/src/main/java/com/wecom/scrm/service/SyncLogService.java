package com.wecom.scrm.service;

import com.wecom.scrm.entity.WecomSyncLog;
import com.wecom.scrm.repository.WecomSyncLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class SyncLogService {

    private final WecomSyncLogRepository syncLogRepository;

    public SyncLogService(WecomSyncLogRepository syncLogRepository) {
        this.syncLogRepository = syncLogRepository;
    }

    public Page<WecomSyncLog> getSyncLogs(int page, int size) {
        // Spring Data Page is 0-indexed, but frontend usually sends 1-indexed
        return syncLogRepository.findAllByOrderByCreateTimeDesc(PageRequest.of(page - 1, size));
    }
}
