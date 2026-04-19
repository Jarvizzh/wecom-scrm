package com.wecom.scrm.repository.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenConsumeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface YuewenConsumeRecordRepository extends JpaRepository<YuewenConsumeRecord, Long> {

    Optional<YuewenConsumeRecord> findByOrderId(String orderId);

    @Query("SELECT r FROM YuewenConsumeRecord r WHERE " +
           "(:appFlag IS NULL OR r.appFlag = :appFlag) AND " +
           "(:openid IS NULL OR r.openid = :openid) AND " +
           "(:guid IS NULL OR r.guid = :guid) AND " +
           "(:bookName IS NULL OR r.bookName LIKE %:bookName%)")
    Page<YuewenConsumeRecord> findByFilters(
            @Param("appFlag") String appFlag,
            @Param("openid") String openid,
            @Param("guid") Long guid,
            @Param("bookName") String bookName,
            Pageable pageable);
}
