package com.wecom.scrm.repository.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YuewenUserRepository extends JpaRepository<YuewenUser, Long> {
    Optional<YuewenUser> findByAppFlagAndOpenid(String appFlag, String openid);

    @Query("SELECT u FROM YuewenUser u WHERE (:appFlag IS NULL OR u.appFlag = :appFlag OR :appFlag = '') " +
           "AND (:openid IS NULL OR u.openid LIKE %:openid% OR :openid = '')")
    Page<YuewenUser> findByFilters(String appFlag, String openid, Pageable pageable);
}
