package com.wecom.scrm.repository.changdu;

import com.wecom.scrm.entity.changdu.ChangduProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChangduProductRepository extends JpaRepository<ChangduProduct, Long> {
    Optional<ChangduProduct> findByDistributorId(Long distributorId);
}
