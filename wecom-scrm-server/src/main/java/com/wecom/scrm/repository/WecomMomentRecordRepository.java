package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomMomentRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WecomMomentRecordRepository extends JpaRepository<WecomMomentRecord, Long> {
    List<WecomMomentRecord> findByMomentId(Long momentId);
    Optional<WecomMomentRecord> findByMomentIdAndUserid(Long momentId, String userid);
    void deleteByMomentId(Long momentId);
}
