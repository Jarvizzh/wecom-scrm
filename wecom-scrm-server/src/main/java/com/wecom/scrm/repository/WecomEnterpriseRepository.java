package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomEnterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WecomEnterpriseRepository extends JpaRepository<WecomEnterprise, Long> {
    WecomEnterprise findByCorpId(String corpId);
    List<WecomEnterprise> findAllByStatus(Integer status);
}
