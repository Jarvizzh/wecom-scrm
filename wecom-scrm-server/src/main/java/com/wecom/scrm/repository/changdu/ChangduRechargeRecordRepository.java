package com.wecom.scrm.repository.changdu;

import com.wecom.scrm.entity.changdu.ChangduRechargeRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChangduRechargeRecordRepository extends JpaRepository<ChangduRechargeRecord, Long> {
    Optional<ChangduRechargeRecord> findByTradeNo(Long tradeNo);
}
