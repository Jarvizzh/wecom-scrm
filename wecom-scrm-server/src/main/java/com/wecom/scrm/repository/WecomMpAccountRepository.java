package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomMpAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface WecomMpAccountRepository extends JpaRepository<WecomMpAccount, Long> {

    Optional<WecomMpAccount> findByAppId(String appId);

    List<WecomMpAccount> findByStatus(Integer status);

}
