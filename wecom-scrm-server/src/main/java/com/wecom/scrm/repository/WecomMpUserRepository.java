package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomMpUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

@Repository
public interface WecomMpUserRepository extends JpaRepository<WecomMpUser, Long> {
    Optional<WecomMpUser> findByMpAppIdAndOpenid(String mpAppId, String openid);
    
    Page<WecomMpUser> findByMpAppId(String mpAppId, Pageable pageable);
    
    @org.springframework.data.jpa.repository.Query("SELECT u FROM WecomMpUser u WHERE " +
            "(:mpAppId IS NULL OR u.mpAppId = :mpAppId) AND " +
            "(:keyword IS NULL OR u.nickname LIKE %:keyword% OR u.openid LIKE %:keyword% OR u.unionid LIKE %:keyword%)")
    Page<WecomMpUser> findByKeyword(@Param("mpAppId") String mpAppId, 
                                    @Param("keyword") String keyword, 
                                    Pageable pageable);

    java.util.List<WecomMpUser> findByUnionidIn(java.util.Collection<String> unionids);
}
