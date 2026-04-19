package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomTag;
import com.wecom.scrm.entity.WecomTagGroup;
import com.wecom.scrm.service.TagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import lombok.Data;
import com.wecom.scrm.dto.TagDTO;

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

    @PostMapping("/batch-mark")
    public ResponseEntity<String> batchMarkTags(@RequestBody TagDTO.BatchMarkTagsRequest request) {
        tagService.batchMarkTags(request);
        return ResponseEntity.ok("Batch tagging task started in background");
    }
}
