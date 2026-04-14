package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomCustomer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WecomCustomerRepository extends JpaRepository<WecomCustomer, Long> {
    Optional<WecomCustomer> findByExternalUserid(String externalUserid);
    
    List<WecomCustomer> findByExternalUseridIn(List<String> externalUserids);
}
