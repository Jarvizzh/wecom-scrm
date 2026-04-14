package com.wecom.scrm.service;

import com.wecom.scrm.entity.*;
import com.wecom.scrm.repository.*;
import com.wecom.scrm.vo.GroupChatMemberVO;
import com.wecom.scrm.vo.GroupChatVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupChatService {

    private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository memberRepository;
    private final WecomUserRepository userRepository;
    private final WecomCustomerRepository customerRepository;

    public Page<GroupChatVO> getGroupChats(String owner, String name, Pageable pageable) {
        return groupChatRepository.findAllWithVO(owner, name, pageable);
    }

    public List<GroupChatMemberVO> getGroupMembers(String chatId) {
        List<WecomGroupChatMember> members = memberRepository.findByChatId(chatId);
        return members.stream().map(m -> {
            GroupChatMemberVO vo = new GroupChatMemberVO();
            vo.setId(m.getId());
            vo.setChatId(m.getChatId());
            vo.setUserid(m.getUserid());
            vo.setType(m.getType());
            vo.setJoinTime(m.getJoinTime());
            vo.setJoinScene(m.getJoinScene());
            vo.setInvitor(m.getInvitor());

            // Resolve name and avatar
            if (m.getType() == 1) { // Internal
                userRepository.findByUserid(m.getUserid()).ifPresent(u -> {
                    vo.setMemberName(u.getName());
                    vo.setAvatar(u.getAvatar());
                });
            } else if (m.getType() == 2) { // External
                customerRepository.findByExternalUserid(m.getUserid()).ifPresent(c -> {
                    vo.setMemberName(c.getName());
                    vo.setAvatar(c.getAvatar());
                });
            }
            
            if (vo.getMemberName() == null) {
                vo.setMemberName(m.getUserid()); // Fallback to ID
            }

            return vo;
        }).collect(Collectors.toList());
    }
}
