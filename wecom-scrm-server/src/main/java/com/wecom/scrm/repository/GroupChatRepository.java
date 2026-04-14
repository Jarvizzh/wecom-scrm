package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomGroupChat;
import com.wecom.scrm.vo.GroupChatVO;
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
public interface GroupChatRepository extends JpaRepository<WecomGroupChat, Long> {
    Optional<WecomGroupChat> findByChatId(String chatId);

    @Query("SELECT new com.wecom.scrm.vo.GroupChatVO(g.id, g.chatId, g.name, g.owner, u.name, g.createTime, g.memberCount, g.status) " +
           "FROM WecomGroupChat g LEFT JOIN WecomUser u ON g.owner = u.userid " +
           "WHERE (:owner IS NULL OR g.owner = :owner) " +
           "AND (:name IS NULL OR g.name LIKE %:name%)")
    Page<GroupChatVO> findAllWithVO(@Param("owner") String owner, @Param("name") String name, Pageable pageable);

    @Query("SELECT g.chatId FROM WecomGroupChat g WHERE (:status IS NULL OR g.status = :status)")
    List<String> findChatIdsByStatus(@Param("status") Integer status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE WecomGroupChat g SET g.status = 2 WHERE g.chatId IN :chatIds")
    void markAsDeletedByChatIdIn(@Param("chatIds") Collection<String> chatIds);
}
