package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomWelcomeMsg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WecomWelcomeMsgRepository extends JpaRepository<WecomWelcomeMsg, Long> {
}
