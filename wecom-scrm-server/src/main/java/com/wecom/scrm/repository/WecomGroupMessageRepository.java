package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomGroupMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WecomGroupMessageRepository extends JpaRepository<WecomGroupMessage, Long> {

    List<WecomGroupMessage> findByStatusAndSendTypeAndSendTimeBefore(Integer status, Integer sendType, LocalDateTime sendTime);

    List<WecomGroupMessage> findByStatus(Integer status);

    List<WecomGroupMessage> findAllByOrderByCreateTimeDesc();
}
