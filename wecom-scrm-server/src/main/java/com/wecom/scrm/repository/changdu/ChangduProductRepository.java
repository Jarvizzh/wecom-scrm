package com.wecom.scrm.repository.changdu;

import com.wecom.scrm.entity.changdu.ChangduProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ChangduProductRepository extends JpaRepository<ChangduProduct, Long> {
    Optional<ChangduProduct> findByDistributorId(Long distributorId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE ChangduProduct p SET p.status = :status WHERE p.id IN :ids")
    void batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);
}
