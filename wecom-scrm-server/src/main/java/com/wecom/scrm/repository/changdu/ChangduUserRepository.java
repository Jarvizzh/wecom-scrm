package com.wecom.scrm.repository.changdu;

import com.wecom.scrm.entity.changdu.ChangduUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChangduUserRepository extends JpaRepository<ChangduUser, Long> {
    Optional<ChangduUser> findByDistributorIdAndEncryptedDeviceId(Long distributorId, String encryptedDeviceId);

    @Query("SELECT u FROM ChangduUser u WHERE (:distributorId IS NULL OR u.distributorId = :distributorId) " +
           "AND (:openId IS NULL OR u.openId = :openId) " +
           "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%)")
    Page<ChangduUser> findUsers(
            @Param("distributorId") Long distributorId,
            @Param("openId") String openId,
            @Param("nickname") String nickname,
            Pageable pageable);

    @Query("SELECT DISTINCT u.externalId FROM ChangduUser u WHERE u.externalId IS NOT NULL " +
           "AND (:distributorId IS NULL OR u.distributorId = :distributorId) " +
           "AND (:openId IS NULL OR u.openId = :openId) " +
           "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%)")
    List<String> findExternalUseridsByFilters(
            @Param("distributorId") Long distributorId,
            @Param("openId") String openId,
            @Param("nickname") String nickname);

    List<ChangduUser> findByExternalId(String externalId);
}
