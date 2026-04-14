package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomTagGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;

public interface WecomTagGroupRepository extends JpaRepository<WecomTagGroup, Long> {
    Optional<WecomTagGroup> findByGroupId(String groupId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WecomTagGroup WHERE groupId IN :groupIds")
    void deleteByGroupIdIn(@Param("groupIds") Collection<String> groupIds);
}
