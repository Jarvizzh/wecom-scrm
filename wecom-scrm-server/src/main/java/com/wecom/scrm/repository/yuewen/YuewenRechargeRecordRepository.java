package com.wecom.scrm.repository.yuewen;

import com.wecom.scrm.dto.yuewen.YuewenRechargeRecordDTO;
import com.wecom.scrm.entity.yuewen.YuewenRechargeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface YuewenRechargeRecordRepository extends JpaRepository<YuewenRechargeRecord, Long> {

    Optional<YuewenRechargeRecord> findByYwOrderId(String ywOrderId);

    @Query("SELECT new com.wecom.scrm.dto.yuewen.YuewenRechargeRecordDTO(" +
            "r.id, r.appFlag, r.appName, r.amount, r.ywOrderId, r.orderId, r.orderTime, r.payTime, " +
            "r.orderStatus, r.orderType, r.openid, r.guid, u.nickname, u.avatar, r.sex, " +
            "r.channelId, r.channelName, r.bookId, r.bookName, r.wxAppId, r.itemName, r.orderChannel) " +
            "FROM YuewenRechargeRecord r " +
            "LEFT JOIN YuewenUser u ON r.appFlag = u.appFlag AND r.guid = u.guid " +
            "WHERE (:appFlag IS NULL OR r.appFlag = :appFlag) AND " +
            "(:openid IS NULL OR r.openid = :openid) AND " +
            "(:nickname IS NULL OR u.nickname LIKE %:nickname%) AND " +
            "(:orderId IS NULL OR r.orderId = :orderId OR r.ywOrderId = :orderId)")
    Page<YuewenRechargeRecordDTO> findRecords(
            @Param("appFlag") String appFlag,
            @Param("openid") String openid,
            @Param("nickname") String nickname,
            @Param("orderId") String orderId,
            Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(CAST(amount AS DECIMAL(10,2))), 0) FROM wecom_yuewen_recharge_record WHERE order_status = 2", nativeQuery = true)
    BigDecimal sumTotalAmount();

    @Query(value = "SELECT COALESCE(SUM(CAST(amount AS DECIMAL(10,2))), 0) FROM wecom_yuewen_recharge_record WHERE order_status = 2 AND pay_time >= :startTime AND pay_time < :endTime", nativeQuery = true)
    BigDecimal sumAmountByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT app_name as name, COALESCE(SUM(CAST(amount AS DECIMAL(10,2))), 0) as amount, COUNT(DISTINCT openid) as userCount FROM wecom_yuewen_recharge_record WHERE order_status = 2 AND pay_time >= :startTime AND pay_time < :endTime GROUP BY app_name", nativeQuery = true)
    List<Map<String, Object>> sumAmountByTimeRangeGroupByName(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Query(value = "SELECT DATE(pay_time) as date, COALESCE(SUM(CAST(amount AS DECIMAL(10,2))), 0) as amount FROM wecom_yuewen_recharge_record WHERE order_status = 2 AND pay_time >= :startTime GROUP BY DATE(pay_time)", nativeQuery = true)
    List<Map<String, Object>> sumTrendByTimeAfter(@Param("startTime") LocalDateTime startTime);
}
