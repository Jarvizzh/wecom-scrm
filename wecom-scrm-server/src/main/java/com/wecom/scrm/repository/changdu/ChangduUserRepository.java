package com.wecom.scrm.repository.changdu;

import com.wecom.scrm.entity.changdu.ChangduUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChangduUserRepository extends JpaRepository<ChangduUser, Long> {
    Optional<ChangduUser> findByDistributorIdAndEncryptedDeviceId(Long distributorId, String encryptedDeviceId);
}
