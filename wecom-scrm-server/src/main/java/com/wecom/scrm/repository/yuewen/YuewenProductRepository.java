package com.wecom.scrm.repository.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YuewenProductRepository extends JpaRepository<YuewenProduct, Long> {
    Optional<YuewenProduct> findByAppFlag(String appFlag);
    List<YuewenProduct> findByStatus(Integer status);
}
