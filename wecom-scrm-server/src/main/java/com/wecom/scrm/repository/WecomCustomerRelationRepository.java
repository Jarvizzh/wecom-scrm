package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomCustomerRelation;
import com.wecom.scrm.dto.CustomerRelationDTO;
import com.wecom.scrm.dto.CustomerTargetDTO;
import com.wecom.scrm.dto.AddWayStatDTO;
import com.wecom.scrm.dto.TrendStatDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface WecomCustomerRelationRepository extends JpaRepository<WecomCustomerRelation, Long> {
    Optional<WecomCustomerRelation> findByUseridAndExternalUserid(String userid, String externalUserid);
    
    List<WecomCustomerRelation> findByExternalUserid(String externalUserid);
    
    List<WecomCustomerRelation> findByExternalUseridIn(List<String> externalUserids);

    @Query("SELECT new com.wecom.scrm.dto.CustomerRelationDTO(c.name, c.avatar, c.type, c.unionid, r.externalUserid, u.name, r.userid, r.status, r.createTime) " +
           "FROM WecomCustomerRelation r " +
           "LEFT JOIN WecomCustomer c ON r.externalUserid = c.externalUserid " +
           "LEFT JOIN WecomUser u ON r.userid = u.userid " +
           "WHERE (:customerName IS NULL OR c.name LIKE %:customerName%) " +
           "AND (:unionid IS NULL OR c.unionid = :unionid) " +
           "AND (:employeeName IS NULL OR u.name LIKE %:employeeName%) " +
           "AND (:mpAppId IS NULL OR EXISTS (SELECT 1 FROM WecomMpUser m WHERE m.unionid = c.unionid AND m.mpAppId = :mpAppId)) " +
           "AND (:hasTags = false OR EXISTS (SELECT 1 FROM WecomCustomerTag ct WHERE ct.externalUserid = r.externalUserid AND ct.userid = r.userid AND ct.tagId IN :tagIds)) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:onlyDuplicates = false OR (SELECT COUNT(r2) FROM WecomCustomerRelation r2 WHERE r2.externalUserid = r.externalUserid AND r2.status = 0) > 1)")
    Page<CustomerRelationDTO> findCustomerRelations(@Param("customerName") String customerName, 
                                                    @Param("unionid") String unionid,
                                                    @Param("employeeName") String employeeName, 
                                                    @Param("mpAppId") String mpAppId,
                                                    @Param("tagIds") List<String> tagIds,
                                                    @Param("hasTags") boolean hasTags,
                                                    @Param("status") Integer status,
                                                    @Param("onlyDuplicates") boolean onlyDuplicates,
                                                    Pageable pageable);

    @Query("SELECT new com.wecom.scrm.dto.CustomerTargetDTO(r.userid, r.externalUserid) " +
           "FROM WecomCustomerRelation r " +
           "LEFT JOIN WecomCustomer c ON r.externalUserid = c.externalUserid " +
           "LEFT JOIN WecomUser u ON r.userid = u.userid " +
           "WHERE (:customerName IS NULL OR c.name LIKE %:customerName%) " +
           "AND (:unionid IS NULL OR c.unionid = :unionid) " +
           "AND (:employeeName IS NULL OR u.name LIKE %:employeeName%) " +
           "AND (:mpAppId IS NULL OR EXISTS (SELECT 1 FROM WecomMpUser m WHERE m.unionid = c.unionid AND m.mpAppId = :mpAppId)) " +
           "AND (:hasTags = false OR EXISTS (SELECT 1 FROM WecomCustomerTag ct WHERE ct.externalUserid = r.externalUserid AND ct.userid = r.userid AND ct.tagId IN :tagIds)) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:onlyDuplicates = false OR (SELECT COUNT(r2) FROM WecomCustomerRelation r2 WHERE r2.externalUserid = r.externalUserid AND r2.status = 0) > 1)")
    List<CustomerTargetDTO> findTargetsByFilters(@Param("customerName") String customerName, 
                                                                   @Param("unionid") String unionid,
                                                                   @Param("employeeName") String employeeName, 
                                                                   @Param("mpAppId") String mpAppId,
                                                                   @Param("tagIds") List<String> tagIds,
                                                                   @Param("hasTags") boolean hasTags,
                                                                   @Param("status") Integer status,
                                                                   @Param("onlyDuplicates") boolean onlyDuplicates);

    @Query("SELECT DISTINCT r.externalUserid FROM WecomCustomerRelation r WHERE (:status IS NULL OR r.status = :status)")
    List<String> findExternalUseridsByStatus(@Param("status") Integer status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE WecomCustomerRelation r SET r.status = :status WHERE r.externalUserid IN :externalUserids")
    void markStatusByExternalUseridIn(@Param("externalUserids") Collection<String> externalUserids, @Param("status") Integer status);

    @Query("SELECT COUNT(r) FROM WecomCustomerRelation r WHERE r.createTime >= :startTime AND r.status = 0")
    long countByCreateTimeAfter(@Param("startTime") java.time.LocalDateTime startTime);

    @Query("SELECT new com.wecom.scrm.dto.AddWayStatDTO(r.addWay, COUNT(r)) " +
           "FROM WecomCustomerRelation r WHERE r.status = 0 GROUP BY r.addWay")
    List<AddWayStatDTO> countByAddWay();

    @Query("SELECT new com.wecom.scrm.dto.TrendStatDTO(CAST(r.createTime AS LocalDate), COUNT(r)) " +
           "FROM WecomCustomerRelation r " +
           "WHERE r.createTime >= :startTime AND r.status = 0 GROUP BY CAST(r.createTime AS LocalDate) ORDER BY CAST(r.createTime AS LocalDate) ASC")
    List<TrendStatDTO> countTrendByCreateTimeAfter(@Param("startTime") java.time.LocalDateTime startTime);
}
