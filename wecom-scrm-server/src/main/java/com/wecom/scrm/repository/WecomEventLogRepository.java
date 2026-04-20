package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomEventLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WecomEventLogRepository extends JpaRepository<WecomEventLog, Long> {

    List<WecomEventLog> findByStatusAndCreateTimeBefore(Integer status, LocalDateTime time);

    List<WecomEventLog> findByStatusAndRetryCountLessThan(Integer status, Integer maxRetries);

    List<WecomEventLog> findByStatusAndUpdateTimeBefore(Integer status, LocalDateTime time);

    @Modifying
    @Transactional
    @Query("UPDATE WecomEventLog e SET e.status = 3, e.updateTime = :now WHERE e.id = :id AND (e.status = 0 OR e.status = 2)")
    int claimEvent(@Param("id") Long id, @Param("now") LocalDateTime now);

    @Modifying
    @Query("DELETE FROM WecomEventLog e WHERE e.status = 1 AND e.updateTime < :threshold")
    void deleteOldEvents(@Param("threshold") LocalDateTime threshold);
}
