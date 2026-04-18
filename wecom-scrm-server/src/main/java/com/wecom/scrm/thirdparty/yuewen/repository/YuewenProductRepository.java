package com.wecom.scrm.thirdparty.yuewen.repository;

import com.wecom.scrm.thirdparty.yuewen.entity.YuewenProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface YuewenProductRepository extends JpaRepository<YuewenProduct, Long> {
    Optional<YuewenProduct> findByAppFlag(String appFlag);
    List<YuewenProduct> findByStatus(Integer status);
}
