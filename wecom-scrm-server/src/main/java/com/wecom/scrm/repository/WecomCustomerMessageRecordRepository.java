package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomCustomerMessageRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WecomCustomerMessageRecordRepository extends JpaRepository<WecomCustomerMessageRecord, Long> {

    List<WecomCustomerMessageRecord> findByMessageId(Long messageId);
}
