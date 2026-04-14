package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomSyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WecomSyncLogRepository extends JpaRepository<WecomSyncLog, Long> {
    Page<WecomSyncLog> findAllByOrderByCreateTimeDesc(Pageable pageable);
}
