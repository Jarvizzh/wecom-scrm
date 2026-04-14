package com.wecom.scrm.controller;

import com.wecom.scrm.vo.GroupChatMemberVO;
import com.wecom.scrm.vo.GroupChatVO;
import com.wecom.scrm.service.GroupChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/group-chat")
@RequiredArgsConstructor
public class GroupChatController {

    private final GroupChatService groupChatService;

    @GetMapping("/list")
    public ResponseEntity<Page<GroupChatVO>> listGroupChats(
            @RequestParam(required = false) String owner,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortProp,
            @RequestParam(required = false) String sortOrder) {

        log.info("Listing group chats with owner filter: '{}', name filter: '{}'", owner, name);
        
        Sort sort = Sort.by(Sort.Direction.DESC, "createTime");
        if (StringUtils.isNotEmpty(sortProp) && StringUtils.isNotEmpty(sortOrder)) {
            Sort.Direction direction = "ascending".equals(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, sortProp);
        }
        
        String ownerToFilter = StringUtils.isBlank(owner) ? null : owner;
        String nameToFilter = StringUtils.isBlank(name) ? null : name;
        return ResponseEntity.ok(groupChatService.getGroupChats(ownerToFilter, nameToFilter, PageRequest.of(Math.max(0, page - 1), size, sort)));
    }

    @GetMapping("/{chatId}/members")
    public ResponseEntity<List<GroupChatMemberVO>> getGroupMembers(@PathVariable String chatId) {
        return ResponseEntity.ok(groupChatService.getGroupMembers(chatId));
    }
}
