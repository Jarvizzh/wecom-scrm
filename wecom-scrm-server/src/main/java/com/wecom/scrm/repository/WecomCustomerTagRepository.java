package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomCustomerTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface WecomCustomerTagRepository extends JpaRepository<WecomCustomerTag, Long> {
    List<WecomCustomerTag> findByExternalUseridAndUserid(String externalUserid, String userid);
    
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WecomCustomerTag WHERE externalUserid = ?1 AND userid = ?2")
    void deleteByExternalUseridAndUserid(String externalUserid, String userid);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM WecomCustomerTag WHERE externalUserid = :externalUserid AND userid = :userid AND tagId IN :tagIds")
    void deleteByExternalUseridAndUseridAndTagIdIn(@Param("externalUserid") String externalUserid,
                                                   @Param("userid") String userid,
                                                   @Param("tagIds") Collection<String> tagIds);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM WecomCustomerTag WHERE tagId = :tagId")
    void deleteByTagId(@Param("tagId") String tagId);

    @Modifying(flushAutomatically = true)
    @Query("DELETE FROM WecomCustomerTag WHERE tagId IN (SELECT t.tagId FROM WecomTag t WHERE t.groupId = :groupId)")
    void deleteByGroupId(@Param("groupId") String groupId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WecomCustomerTag WHERE externalUserid IN :externalUserids")
    void deleteByExternalUseridIn(@Param("externalUserids") Collection<String> externalUserids);
}
