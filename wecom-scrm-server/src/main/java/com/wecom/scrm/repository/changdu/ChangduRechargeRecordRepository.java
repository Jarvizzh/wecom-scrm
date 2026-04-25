package com.wecom.scrm.repository.changdu;

import com.wecom.scrm.dto.changdu.ChangduRechargeRecordDTO;
import com.wecom.scrm.entity.changdu.ChangduRechargeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface ChangduRechargeRecordRepository extends JpaRepository<ChangduRechargeRecord, Long> {
    Optional<ChangduRechargeRecord> findByTradeNo(Long tradeNo);

    @Query("SELECT new com.wecom.scrm.dto.changdu.ChangduRechargeRecordDTO(" +
           "r.id, r.distributorId, r.tradeNo, r.outTradeNo, r.deviceId, r.openId, r.externalId, " +
           "r.payWay, r.payFee, r.status, r.bookId, r.bookName, r.promotionId, r.payTime, " +
           "r.orderCreateTime, r.rechargeType, r.orderType, u.nickname, u.avatar) " +
           "FROM ChangduRechargeRecord r " +
           "LEFT JOIN ChangduUser u ON r.distributorId = u.distributorId AND r.deviceId = u.encryptedDeviceId " +
           "WHERE (:distributorId IS NULL OR r.distributorId = :distributorId) " +
           "AND (:openId IS NULL OR r.openId = :openId) " +
           "AND (:tradeNo IS NULL OR r.tradeNo = :tradeNo) " +
           "AND (:nickname IS NULL OR u.nickname LIKE %:nickname%)")
    Page<ChangduRechargeRecordDTO> findRecords(
            @Param("distributorId") Long distributorId,
            @Param("openId") String openId,
            @Param("tradeNo") Long tradeNo,
            @Param("nickname") String nickname,
            Pageable pageable);

    @Query(value = "SELECT COALESCE(SUM(pay_fee), 0) FROM wecom_changdu_recharge_record WHERE status = '0'", nativeQuery = true)
    Long sumTotalPayFee();

    @Query(value = "SELECT COALESCE(SUM(pay_fee), 0) FROM wecom_changdu_recharge_record WHERE status = '0' AND pay_time >= :startTime AND pay_time < :endTime", nativeQuery = true)
    Long sumPayFeeByTimeRange(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Query(value = "SELECT p.product_name as name, COALESCE(SUM(r.pay_fee), 0) as amount, COUNT(DISTINCT r.device_id) as userCount FROM wecom_changdu_recharge_record r LEFT JOIN wecom_changdu_product p ON r.distributor_id = p.distributor_id WHERE r.status = '0' AND r.pay_time >= :startTime AND r.pay_time < :endTime GROUP BY p.product_name", nativeQuery = true)
    List<Map<String, Object>> sumPayFeeByTimeRangeGroupByName(@Param("startTime") String startTime, @Param("endTime") String endTime);

    @Query(value = "SELECT p.product_name as name, SUBSTRING(r.pay_time, 1, 10) as date, COALESCE(SUM(r.pay_fee), 0) as amount, COUNT(DISTINCT r.device_id) as userCount FROM wecom_changdu_recharge_record r LEFT JOIN wecom_changdu_product p ON r.distributor_id = p.distributor_id WHERE r.status = '0' AND r.pay_time >= :startTime GROUP BY p.product_name, SUBSTRING(r.pay_time, 1, 10)", nativeQuery = true)
    List<Map<String, Object>> sumPayFeeByDateAndName(@Param("startTime") String startTime);
}
