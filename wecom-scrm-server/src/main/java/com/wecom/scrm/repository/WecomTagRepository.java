package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface WecomTagRepository extends JpaRepository<WecomTag, Long> {
    Optional<WecomTag> findByTagId(String tagId);
    List<WecomTag> findByGroupId(String groupId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WecomTag WHERE groupId = :groupId")
    void deleteByGroupId(@Param("groupId") String groupId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WecomTag WHERE tagId IN :tagIds")
    void deleteByTagIdIn(@Param("tagIds") Collection<String> tagIds);
}
