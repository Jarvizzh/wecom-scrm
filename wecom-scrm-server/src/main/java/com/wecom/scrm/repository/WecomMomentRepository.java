package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomMoment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WecomMomentRepository extends JpaRepository<WecomMoment, Long> {
    Optional<WecomMoment> findByJobid(String jobid);
    Optional<WecomMoment> findByMomentId(String momentId);

    List<WecomMoment> findAllByOrderByCreateTimeDesc();
}
