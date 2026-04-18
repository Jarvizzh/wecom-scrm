package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomTag;
import com.wecom.scrm.entity.WecomTagGroup;
import com.wecom.scrm.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.Data;

@RestController
@RequestMapping("/api/admin/tags")
public class TagController {

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Data
    public static class AddTagRequest {
        private String groupName;
        private String tagName;
    }

    @Data
    public static class MarkTagsRequest {
        private String userid;
        private String externalUserid;
        private List<String> addTagIds;
        private List<String> removeTagIds;
    }

    @GetMapping("/groups")
    public List<WecomTagGroup> getTagGroups() {
        return tagService.getAllTagGroups();
    }

    @GetMapping("/group/{groupId}")
    public List<WecomTag> getTags(@PathVariable String groupId) {
        return tagService.getTagsByGroupId(groupId);
    }

    @PostMapping("/add")
    public ResponseEntity<String> addTag(@RequestBody AddTagRequest request) throws Exception {
        tagService.addCorpTag(request.getGroupName(), request.getTagName());
        return ResponseEntity.ok("Tag added successfully");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTag(@RequestParam(required = false) String tagId,
            @RequestParam(required = false) String groupId) throws Exception {
        tagService.deleteCorpTag(tagId, groupId);
        return ResponseEntity.ok("Tag/Group deleted successfully");
    }

    @PostMapping("/mark")
    public ResponseEntity<String> markTags(@RequestBody MarkTagsRequest request) throws Exception {
        tagService.markTags(request.getUserid(), request.getExternalUserid(), 
                            request.getAddTagIds(), request.getRemoveTagIds());
        return ResponseEntity.ok("Tags updated successfully");
    }

    @Data
    public static class BatchMarkTagsRequest {
        private List<TagTarget> targets;
        private List<String> addTagIds;
        private List<String> removeTagIds;
        
        // Select all support
        private boolean selectAll;
        private String customerName;
        private String unionid;
        private String employeeName;
        private String mpAppId;
        private List<String> tagIds;
        private Integer status;
        private boolean onlyDuplicates;

        // Yuewen filters
        private String appFlag;
        private String openid;
        private Long minAmount;
        private Long maxAmount;
    }

    @Data
    public static class TagTarget {
        private String userid;
        private String externalUserid;
    }

    @PostMapping("/batch-mark")
    public ResponseEntity<String> batchMarkTags(@RequestBody BatchMarkTagsRequest request) {
        tagService.batchMarkTags(request);
        return ResponseEntity.ok("Batch tagging task started in background");
    }
}
