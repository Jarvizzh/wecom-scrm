package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomGroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WecomGroupMessageRepository extends JpaRepository<WecomGroupMessage, Long> {

    List<WecomGroupMessage> findByStatusAndSendTypeAndSendTimeBefore(Integer status, Integer sendType, LocalDateTime sendTime);

    List<WecomGroupMessage> findByStatus(Integer status);

    List<WecomGroupMessage> findAllByOrderByCreateTimeDesc();
    
    @Modifying
    @Transactional
    @Query("UPDATE WecomGroupMessage m SET m.status = :newStatus WHERE m.id = :id AND m.status = :oldStatus")
    int updateStatusIfPending(@Param("id") Long id, @Param("oldStatus") Integer oldStatus, @Param("newStatus") Integer newStatus);
}
