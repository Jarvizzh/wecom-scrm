package com.wecom.scrm.repository;

import com.wecom.scrm.entity.WecomGroupChatMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupChatMemberRepository extends JpaRepository<WecomGroupChatMember, Long> {
    List<WecomGroupChatMember> findByChatId(String chatId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("DELETE FROM WecomGroupChatMember m WHERE m.chatId = ?1")
    void deleteByChatId(String chatId);
}
