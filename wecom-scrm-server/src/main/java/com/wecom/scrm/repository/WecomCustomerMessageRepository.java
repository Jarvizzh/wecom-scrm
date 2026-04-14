package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomCustomerMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WecomCustomerMessageRepository extends JpaRepository<WecomCustomerMessage, Long> {

    List<WecomCustomerMessage> findByStatusAndSendTypeAndSendTimeBefore(Integer status, Integer sendType, LocalDateTime sendTime);

    List<WecomCustomerMessage> findByStatus(Integer status);

    List<WecomCustomerMessage> findAllByOrderByCreateTimeDesc();
    
    @Modifying
    @Transactional
    @Query("UPDATE WecomCustomerMessage m SET m.status = :newStatus WHERE m.id = :id AND m.status = :oldStatus")
    int updateStatusIfPending(Long id, Integer oldStatus, Integer newStatus);
}
