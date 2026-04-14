package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WecomEventLogRepository extends JpaRepository<WecomEventLog, Long> {

    List<WecomEventLog> findByStatusAndCreateTimeBefore(Integer status, LocalDateTime time);

    List<WecomEventLog> findByStatusAndRetryCountLessThan(Integer status, Integer maxRetries);

    @Modifying
    @Query("DELETE FROM WecomEventLog e WHERE e.status = 1 AND e.updateTime < :threshold")
    void deleteOldEvents(@Param("threshold") LocalDateTime threshold);
}
