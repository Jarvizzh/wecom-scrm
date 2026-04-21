package com.wecom.scrm.service;

import com.wecom.scrm.entity.WecomCustomerRelation;
import com.wecom.scrm.entity.WecomCustomerTag;
import com.wecom.scrm.entity.WecomTag;
import com.wecom.scrm.entity.WecomTagGroup;
import com.wecom.scrm.repository.WecomCustomerTagRepository;
import com.wecom.scrm.repository.WecomTagGroupRepository;
import com.wecom.scrm.repository.WecomTagRepository;
import com.wecom.scrm.repository.WecomCustomerRelationRepository;
import com.wecom.scrm.repository.yuewen.YuewenUserRepository;
import com.wecom.scrm.repository.changdu.ChangduUserRepository;
import lombok.extern.slf4j.Slf4j;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.bean.WxCpBaseResp;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalTagGroupInfo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wecom.scrm.dto.TagDTO;
import com.wecom.scrm.dto.CustomerTargetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TagService {

    private final WxCpServiceManager wxCpServiceManager;
    private final WecomTagGroupRepository tagGroupRepository;
    private final WecomTagRepository tagRepository;
    private final WecomCustomerTagRepository customerTagRepository;
    private final WecomCustomerRelationRepository customerRelationRepository;
    private final YuewenUserRepository yuewenUserRepository;
    private final ChangduUserRepository changduUserRepository;

    @Autowired
    @Lazy
    private TagService self;

    public TagService(@Lazy WxCpServiceManager wxCpServiceManager,
            WecomTagGroupRepository tagGroupRepository,
            WecomTagRepository tagRepository,
            WecomCustomerTagRepository customerTagRepository,
            WecomCustomerRelationRepository customerRelationRepository,
            YuewenUserRepository yuewenUserRepository,
            ChangduUserRepository changduUserRepository) {
        this.wxCpServiceManager = wxCpServiceManager;
        this.tagGroupRepository = tagGroupRepository;
        this.tagRepository = tagRepository;
        this.customerTagRepository = customerTagRepository;
        this.customerRelationRepository = customerRelationRepository;
        this.yuewenUserRepository = yuewenUserRepository;
        this.changduUserRepository = changduUserRepository;
    }

    public List<WecomTagGroup> getAllTagGroups() {
        return tagGroupRepository.findAll();
    }

    public List<WecomTag> getTagsByGroupId(String groupId) {
        return tagRepository.findByGroupId(groupId);
    }

    @Transactional
    public void addCorpTag(String groupName, String tagName) throws Exception {
        WxCpUserExternalTagGroupInfo groupInfo = new WxCpUserExternalTagGroupInfo();
        WxCpUserExternalTagGroupInfo.TagGroup group = new WxCpUserExternalTagGroupInfo.TagGroup();
        group.setGroupName(groupName);

        WxCpUserExternalTagGroupInfo.Tag tag = new WxCpUserExternalTagGroupInfo.Tag();
        tag.setName(tagName);
        group.setTag(Collections.singletonList(tag));
        groupInfo.setTagGroup(group);

        WxCpUserExternalTagGroupInfo result = wxCpServiceManager.getWxCpService().getExternalContactService().addCorpTag(groupInfo);

        if (result != null && result.getTagGroup() != null) {
            WxCpUserExternalTagGroupInfo.TagGroup resGroup = result.getTagGroup();
            WecomTagGroup tg = tagGroupRepository.findByGroupId(resGroup.getGroupId()).orElse(new WecomTagGroup());
            tg.setGroupId(resGroup.getGroupId());
            tg.setGroupName(resGroup.getGroupName());
            tg.setOrder(resGroup.getOrder());
            tagGroupRepository.save(tg);

            if (resGroup.getTag() != null) {
                for (WxCpUserExternalTagGroupInfo.Tag resTag : resGroup.getTag()) {
                    WecomTag t = tagRepository.findByTagId(resTag.getId()).orElse(new WecomTag());
                    t.setTagId(resTag.getId());
                    t.setGroupId(resGroup.getGroupId());
                    t.setName(resTag.getName());
                    t.setOrder(resTag.getOrder());
                    tagRepository.save(t);
                }
            }
        }
    }

    @Transactional
    public void deleteCorpTag(String tagId, String groupId) throws Exception {
        String[] tagIds = tagId != null ? new String[] { tagId } : null;
        String[] groupIds = groupId != null ? new String[] { groupId } : null;

        wxCpServiceManager.getWxCpService().getExternalContactService().delCorpTag(tagIds, groupIds);

        if (tagId != null) {
            tagRepository.findByTagId(tagId).ifPresent(tagRepository::delete);
            // Cleanup tag relations
            customerTagRepository.deleteByTagId(tagId);
        } else if (groupId != null) {
            // Cleanup relations for all tags in group
            customerTagRepository.deleteByGroupId(groupId);
            
            tagGroupRepository.findByGroupId(groupId).ifPresent(tagGroupRepository::delete);
            List<WecomTag> tags = tagRepository.findByGroupId(groupId);
            tagRepository.deleteAll(tags);
        }
    }

    @Transactional
    public void markTags(String userid, String externalUserid, List<String> addTagIds, List<String> removeTagIds)
            throws Exception {

        WxCpBaseResp resp = wxCpServiceManager.getWxCpService().getExternalContactService().markTag(userid, externalUserid,
                addTagIds != null ? addTagIds.toArray(new String[0]) : null,
                removeTagIds != null ? removeTagIds.toArray(new String[0]) : null);
        if (!resp.success()) {
            log.error("mark tags error code: {} reason: {} ", resp.getErrcode(), resp.getErrmsg());
            throw new RuntimeException("mark tags error!" + resp.getErrmsg());
        }

        // If no exception is thrown, the operation is considered successful on WeCom side
        // Update local DB
        if (removeTagIds != null && !removeTagIds.isEmpty()) {
            customerTagRepository.deleteByExternalUseridAndUseridAndTagIdIn(externalUserid, userid, removeTagIds);
        }

        if (addTagIds != null && !addTagIds.isEmpty()) {
            // Fetch existing to avoid duplicates (though we use distinct in sync, better to be clean here)
            List<WecomCustomerTag> existing = customerTagRepository.findByExternalUseridAndUserid(externalUserid, userid);
            Set<String> existingTagIds = existing.stream()
                    .map(WecomCustomerTag::getTagId)
                    .collect(Collectors.toSet());

            for (String tid : addTagIds) {
                if (!existingTagIds.contains(tid)) {
                    WecomCustomerTag customerTag = new WecomCustomerTag();
                    customerTag.setExternalUserid(externalUserid);
                    customerTag.setUserid(userid);
                    customerTag.setTagId(tid);
                    customerTagRepository.save(customerTag);
                }
            }
        }
    }

    @Async("syncExecutor")
    public void batchMarkTags(TagDTO.BatchMarkTagsRequest request) {
        List<TagDTO.TagTarget> targets = request.getTargets();

        if (request.isSelectAll()) {
            log.info("Resolving targets for global select all tagging. targetType: {}", request.getTargetType());
            List<String> tagIds = request.getTagIds();
            List<String> cleanTagIds = (tagIds != null) ? tagIds.stream()
                    .filter(tid -> tid != null && !tid.trim().isEmpty())
                    .collect(Collectors.toList()) : null;
            boolean hasTags = (cleanTagIds != null && !cleanTagIds.isEmpty());
            if (!hasTags) {
                cleanTagIds = null;
            }

            if ("yuewen".equalsIgnoreCase(request.getTargetType())) {
                // Yuewen selection
                log.info("Using Yuewen filters for global selection: appFlag={}, openid={}, nickname={}", 
                        request.getAppFlag(), request.getOpenid(), request.getNickname());
                List<String> externalUserids = yuewenUserRepository.findExternalUseridsByFilters(
                        request.getAppFlag(),
                        request.getOpenid(),
                        request.getNickname(),
                        request.getMinAmount(),
                        request.getMaxAmount()
                );
                targets = externalUserids.stream().map(eid -> {
                    TagDTO.TagTarget target = new TagDTO.TagTarget();
                    target.setExternalUserid(eid);
                    return target;
                }).collect(Collectors.toList());
            } else if ("changdu".equalsIgnoreCase(request.getTargetType())) {
                // Changdu selection
                log.info("Using Changdu filters for global selection: distributorId={}, openId={}, nickname={}", 
                        request.getDistributorId(), request.getOpenid(), request.getNickname());
                List<String> externalUserids = changduUserRepository.findExternalUseridsByFilters(
                        request.getDistributorId(),
                        request.getOpenid(),
                        request.getNickname()
                );
                targets = externalUserids.stream().map(eid -> {
                    TagDTO.TagTarget target = new TagDTO.TagTarget();
                    target.setExternalUserid(eid);
                    return target;
                }).collect(Collectors.toList());
            } else {
                // Customer list selection
                List<CustomerTargetDTO> candidates = customerRelationRepository.findTargetsByFilters(
                        request.getCustomerName(),
                        request.getUnionid(),
                        request.getEmployeeName(),
                        request.getMpAppId(),
                        cleanTagIds,
                        hasTags,
                        request.getStatus(),
                        request.isOnlyDuplicates()
                );
                targets = candidates.stream().map(dto -> {
                    TagDTO.TagTarget target = new TagDTO.TagTarget();
                    target.setUserid(dto.getUserid());
                    target.setExternalUserid(dto.getExternalUserid());
                    return target;
                }).collect(Collectors.toList());
            }
        }

        if (targets == null || targets.isEmpty()) {
            return;
        }

        log.info("Starting batch tagging for {} targets", targets.size());
        
        int successCount = 0;
        int failCount = 0;

        for (TagDTO.TagTarget target : targets) {
            String externalUserid = target.getExternalUserid();
            String userid = target.getUserid();
            
            if (userid == null || userid.trim().isEmpty()) {
                // Resolve all relations for this externalUserid
                List<WecomCustomerRelation> relations = customerRelationRepository.findByExternalUserid(externalUserid);
                for (WecomCustomerRelation relation : relations) {
                    try {
                        self.markTags(relation.getUserid(), externalUserid, 
                                 request.getAddTagIds(), request.getRemoveTagIds());
                        successCount++;
                    } catch (Exception e) {
                        log.error("Failed to mark tags for customer {} / user {}: {}", 
                                 externalUserid, relation.getUserid(), e.getMessage());
                        failCount++;
                    }
                }
            } else {
                try {
                    self.markTags(userid, externalUserid, 
                             request.getAddTagIds(), request.getRemoveTagIds());
                    successCount++;
                } catch (Exception e) {
                    log.error("Failed to mark tags for customer {} / user {}: {}", 
                            externalUserid, userid, e.getMessage());
                    failCount++;
                }
            }
        }
        
        log.info("Batch tagging completed. Success: {}, Failed: {}", successCount, failCount);
    }
}
