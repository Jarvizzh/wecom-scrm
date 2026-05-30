package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomCustomerMessageLoop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WecomCustomerMessageLoopRepository extends JpaRepository<WecomCustomerMessageLoop, Long> {

    List<WecomCustomerMessageLoop> findByStatus(Integer status);
    
    List<WecomCustomerMessageLoop> findAllByOrderByCreateTimeDesc();
}
