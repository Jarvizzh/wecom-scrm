package com.wecom.scrm.repository.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YuewenUserRepository extends JpaRepository<YuewenUser, Long> {

    Optional<YuewenUser> findByAppFlagAndOpenid(String appFlag, String openid);

    @Query("SELECT u FROM YuewenUser u WHERE (:appFlag IS NULL OR u.appFlag = :appFlag OR :appFlag = '') " +
            "AND (:openid IS NULL OR u.openid LIKE %:openid% OR :openid = '') " +
            "AND (:minAmount IS NULL OR u.chargeAmount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR u.chargeAmount <= :maxAmount)")
    Page<YuewenUser> findByFilters(String appFlag, String openid, Long minAmount, Long maxAmount, Pageable pageable);


    @Query("SELECT DISTINCT u.externalUserid FROM YuewenUser u WHERE (:appFlag IS NULL OR u.appFlag = :appFlag OR :appFlag = '') " +
            "AND (:openid IS NULL OR u.openid LIKE %:openid% OR :openid = '') " +
            "AND (:minAmount IS NULL OR u.chargeAmount >= :minAmount) " +
            "AND (:maxAmount IS NULL OR u.chargeAmount <= :maxAmount) " +
            "AND (u.externalUserid IS NOT NULL AND u.externalUserid != '')")
    List<String> findExternalUseridsByFilters(String appFlag, String openid, Long minAmount, Long maxAmount);

    List<YuewenUser> findByExternalUserid(String externalUserid);
}
