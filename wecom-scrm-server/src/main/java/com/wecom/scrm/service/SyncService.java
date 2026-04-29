package com.wecom.scrm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecom.scrm.entity.*;
import com.wecom.scrm.repository.*;
import lombok.extern.slf4j.Slf4j;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.bean.WxCpDepart;
import me.chanjar.weixin.cp.bean.WxCpUser;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatInfo;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalGroupChatList;
import me.chanjar.weixin.cp.bean.external.contact.WxCpExternalContactInfo;
import me.chanjar.weixin.cp.bean.external.contact.FollowedUser;
import me.chanjar.weixin.cp.bean.external.contact.FollowedUser.Tag;
import me.chanjar.weixin.cp.bean.external.WxCpUserExternalTagGroupList;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.dao.ConcurrencyFailureException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

@Slf4j
@Service
public class SyncService {

    private final WxCpServiceManager wxCpServiceManager;
    private final WecomUserRepository userRepository;
    private final WecomCustomerRepository customerRepository;
    private final WecomCustomerRelationRepository customerRelationRepository;
    private final WecomSyncLogRepository syncLogRepository;
    private final WecomTagGroupRepository tagGroupRepository;
    private final WecomTagRepository tagRepository;
    private final WecomCustomerTagRepository customerTagRepository;
    private final WecomDepartmentRepository departmentRepository;
    private final GroupChatRepository groupChatRepository;
    private final GroupChatMemberRepository memberRepository;
    private final Executor bizAsyncExecutor;
    private final Executor syncCustomersExecutor;
    private final TransactionTemplate transactionTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public SyncService(@Lazy WxCpServiceManager wxCpServiceManager,
                       WecomUserRepository userRepository, 
                       WecomCustomerRepository customerRepository,
                       WecomCustomerRelationRepository customerRelationRepository,
                       WecomSyncLogRepository syncLogRepository,
                       WecomTagGroupRepository tagGroupRepository,
                       WecomTagRepository tagRepository,
                       WecomCustomerTagRepository customerTagRepository,
                       WecomDepartmentRepository departmentRepository,
                       GroupChatRepository groupChatRepository,
                       GroupChatMemberRepository memberRepository,
                       @Qualifier("bizAsyncExecutor") Executor bizAsyncExecutor,
                       @Qualifier("syncCustomersExecutor") Executor syncCustomersExecutor,
                       TransactionTemplate transactionTemplate) {
        this.wxCpServiceManager = wxCpServiceManager;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.customerRelationRepository = customerRelationRepository;
        this.syncLogRepository = syncLogRepository;
        this.tagGroupRepository = tagGroupRepository;
        this.tagRepository = tagRepository;
        this.customerTagRepository = customerTagRepository;
        this.departmentRepository = departmentRepository;
        this.groupChatRepository = groupChatRepository;
        this.memberRepository = memberRepository;
        this.bizAsyncExecutor = bizAsyncExecutor;
        this.syncCustomersExecutor = syncCustomersExecutor;
        this.transactionTemplate = transactionTemplate;
    }

    @Async("bizAsyncExecutor")
    @Transactional
    public void syncAllUsers() {
        WecomSyncLog syncLog = new WecomSyncLog();
        syncLog.setSyncType("USER_SYNC");
        syncLog.setStatus(0); // Running
        syncLog = syncLogRepository.save(syncLog);
        
        try {
            // Usually, root department id is 1. fetchChild = true, status = 0 (all statuses)
            List<WxCpUser> users = wxCpServiceManager.getWxCpService().getUserService().listByDepartment(1L, true, 0);
            log.info("Fetched {} users from WeCom.", users.size());

            for (WxCpUser wxCpUser : users) {
                Optional<WecomUser> existingUserOpt = userRepository.findByUserid(wxCpUser.getUserId());
                
                WecomUser user = existingUserOpt.orElse(new WecomUser());
                user.setUserid(wxCpUser.getUserId());
                user.setName(wxCpUser.getName());
                user.setMobile(wxCpUser.getMobile());
                user.setStatus(wxCpUser.getStatus());
                user.setAvatar(wxCpUser.getAvatar());
                if (wxCpUser.getDepartIds() != null) {
                    user.setDepartmentIds(objectMapper.writeValueAsString(wxCpUser.getDepartIds()));
                }
                userRepository.save(user);
            }
            
            syncLog.setStatus(1); // Success
            syncLogRepository.save(syncLog);
            log.info("User sync completed successfully.");
            
        } catch (Exception e) {
            log.error("Failed to sync users", e);
            syncLog.setStatus(2); // Failed
            syncLog.setErrorMsg(e.getMessage());
            syncLogRepository.save(syncLog);
        }
    }

    @Async("bizAsyncExecutor")
    @Transactional
    public void syncAllDepartments() {
        WecomSyncLog syncLog = new WecomSyncLog();
        syncLog.setSyncType("DEPARTMENT_SYNC");
        syncLog.setStatus(0); // Running
        syncLog = syncLogRepository.save(syncLog);

        try {
            List<WxCpDepart> departs = wxCpServiceManager.getWxCpService().getDepartmentService().list(null);
            log.info("Fetched {} departments from WeCom.", departs.size());

            for (WxCpDepart wxCpDepart : departs) {
                WecomDepartment dept = departmentRepository.findByDepartmentId(wxCpDepart.getId()).orElse(new WecomDepartment());
                dept.setDepartmentId(wxCpDepart.getId());
                dept.setName(wxCpDepart.getName());
                dept.setParentId(wxCpDepart.getParentId());
                dept.setOrder(wxCpDepart.getOrder());
                departmentRepository.save(dept);
            }

            syncLog.setStatus(1); // Success
            syncLogRepository.save(syncLog);
            log.info("Department sync completed successfully.");

        } catch (Exception e) {
            log.error("Failed to sync departments", e);
            syncLog.setStatus(2); // Failed
            syncLog.setErrorMsg(e.getMessage());
            syncLogRepository.save(syncLog);
        }
    }

    @Async("bizAsyncExecutor")
    @Transactional
    public void syncCorpTags() {
        WecomSyncLog syncLog = new WecomSyncLog();
        syncLog.setSyncType("TAG_SYNC");
        syncLog.setStatus(0);
        syncLog = syncLogRepository.save(syncLog);
        
        try {
            WxCpUserExternalTagGroupList tagGroupList = wxCpServiceManager.getWxCpService().getExternalContactService().getCorpTagList(null);
            
            Set<String> fetchedGroupIds = new HashSet<>();
            Set<String> fetchedTagIds = new HashSet<>();
            
            if (tagGroupList != null && tagGroupList.getTagGroupList() != null) {
                for (WxCpUserExternalTagGroupList.TagGroup group : tagGroupList.getTagGroupList()) {
                    fetchedGroupIds.add(group.getGroupId());
                    
                    WecomTagGroup tagGroup = tagGroupRepository.findByGroupId(group.getGroupId()).orElse(new WecomTagGroup());
                    tagGroup.setGroupId(group.getGroupId());
                    tagGroup.setGroupName(group.getGroupName());
                    tagGroup.setOrder(group.getOrder());
                    tagGroupRepository.save(tagGroup);
                    
                    if (group.getTag() != null) {
                        for (WxCpUserExternalTagGroupList.TagGroup.Tag tag : group.getTag()) {
                            fetchedTagIds.add(tag.getId());
                            
                            WecomTag wecomTag = tagRepository.findByTagId(tag.getId()).orElse(new WecomTag());
                            wecomTag.setTagId(tag.getId());
                            wecomTag.setGroupId(group.getGroupId());
                            wecomTag.setName(tag.getName());
                            wecomTag.setOrder(tag.getOrder());
                            tagRepository.save(wecomTag);
                        }
                    }
                }
            }

            // Handle deletions
            // 1. Find lost groups
            List<String> dbGroupIds = tagGroupRepository.findAll().stream().map(WecomTagGroup::getGroupId).collect(Collectors.toList());
            List<String> lostGroupIds = dbGroupIds.stream().filter(id -> !fetchedGroupIds.contains(id)).collect(Collectors.toList());
            if (!lostGroupIds.isEmpty()) {
                log.info("Detected {} lost tag groups, deleting...", lostGroupIds.size());
                for (String groupId : lostGroupIds) {
                    customerTagRepository.deleteByGroupId(groupId);
                    tagRepository.deleteByGroupId(groupId);
                }
                tagGroupRepository.deleteByGroupIdIn(lostGroupIds);
            }

            // 2. Find lost tags (in groups that weren't deleted)
            List<String> dbTagIds = tagRepository.findAll().stream().map(WecomTag::getTagId).collect(Collectors.toList());
            List<String> lostTagIds = dbTagIds.stream().filter(id -> !fetchedTagIds.contains(id)).collect(Collectors.toList());
            if (!lostTagIds.isEmpty()) {
                log.info("Detected {} lost tags, deleting...", lostTagIds.size());
                for (String tagId : lostTagIds) {
                    customerTagRepository.deleteByTagId(tagId);
                }
                tagRepository.deleteByTagIdIn(lostTagIds);
            }

            syncLog.setStatus(1);
            syncLogRepository.save(syncLog);
            log.info("Corporate tags sync completed.");
        } catch (Exception e) {
            log.error("Failed to sync corp tags", e);
            syncLog.setStatus(2);
            syncLog.setErrorMsg(e.getMessage());
            syncLogRepository.save(syncLog);
        }
    }

    @Async("bizAsyncExecutor")
    public void syncAllCustomers() {
        WecomSyncLog syncLog = new WecomSyncLog();
        syncLog.setSyncType("CUSTOMER_SYNC");
        syncLog.setStatus(0); // Running
        syncLog = syncLogRepository.save(syncLog);

        try {
            List<WecomUser> users = userRepository.findAll();
            log.info("Starting optimized customer sync for {} users", users.size());

            // 1. Fetch all external userids in parallel for all active users
            List<CompletableFuture<List<String>>> userContactFutures = users.stream()
                    .filter(user -> user.getStatus() == 1)
                    .map(user -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return wxCpServiceManager.getWxCpService().getExternalContactService().listExternalContacts(user.getUserid());
                        } catch (Exception e) {
                            log.warn("Failed to get external contacts for user: {}", user.getUserid());
                            return Collections.<String>emptyList();
                        }
                    }, syncCustomersExecutor))
                    .collect(Collectors.toList());

            Set<String> allExternalUserids = userContactFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .collect(Collectors.toSet());

            log.info("Found {} unique external contacts to sync.", allExternalUserids.size());
            
            // Optimization: Handle lost customers (present in DB but not in WeCom sync)
            try {
                List<String> dbActiveUserids = customerRelationRepository.findExternalUseridsByStatus(0);
                Set<String> lostIds = dbActiveUserids.stream()
                        .filter(id -> !allExternalUserids.contains(id))
                        .collect(Collectors.toSet());
                
                if (!lostIds.isEmpty()) {
                    log.info("Detected {} lost customers. Marking their relations as deleted (status=1).", lostIds.size());
                    transactionTemplate.execute(status -> {
                        customerRelationRepository.markStatusByExternalUseridIn(lostIds, 1);
                        return null;
                    });
                }
            } catch (Exception e) {
                log.warn("Failed to check for lost customers", e);
            }

            log.info("Fetch details for all unique contacts in parallel..");
            // 2. Fetch details for all unique contacts in parallel
            List<CompletableFuture<WxCpExternalContactInfo>> detailFutures = allExternalUserids.stream()
                    .map(externalUserid -> CompletableFuture.supplyAsync(() -> {
                        try {
                            return wxCpServiceManager.getWxCpService().getExternalContactService().getContactDetail(externalUserid, null);
                        } catch (Exception e) {
                            log.warn("Failed to get contact detail for {}: {}", externalUserid, e.getMessage());
                            return null;
                        }
                    }, syncCustomersExecutor))
                    .collect(Collectors.toList());

            log.info("Process results in batches to save memory and database efficiency..");
            // 3. Process results in batches to save memory and database efficiency
            List<WxCpExternalContactInfo> batch = new ArrayList<>();
            int totalProcessed = 0;
            int batchSize = 100;

            for (CompletableFuture<WxCpExternalContactInfo> future : detailFutures) {
                WxCpExternalContactInfo info = future.join();
                if (info != null && info.getExternalContact() != null) {
                    batch.add(info);
                }

                if (batch.size() >= batchSize) {
                    processCustomerBatch(batch);
                    totalProcessed += batch.size();
                    log.info("Sync Progress: {}/{}", totalProcessed, allExternalUserids.size());
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                processCustomerBatch(batch);
                totalProcessed += batch.size();
            }

            syncLog.setStatus(1); // Success
            syncLogRepository.save(syncLog);
            log.info("Customer sync completed. Processed {} customers.", totalProcessed);

        } catch (Exception e) {
            log.error("Failed to sync customers", e);
            syncLog.setStatus(2); // Failed
            syncLog.setErrorMsg(e.getMessage());
            syncLogRepository.save(syncLog);
        }
    }

    /**
     * Synchronize a single customer's data from WeCom.
     * Useful for incremental updates via callback events.
     * 
     * @param externalUserid The WeCom external_userid to sync
     */
    public void syncSingleCustomer(String externalUserid) {
        try {
            WxCpExternalContactInfo info = wxCpServiceManager.getWxCpService().getExternalContactService()
                    .getContactDetail(externalUserid, null);
            if (info != null && info.getExternalContact() != null) {
                processCustomerBatch(Collections.singletonList(info));
                log.info("Successfully synced single customer: {}", externalUserid);
            }
        } catch (Exception e) {
            log.error("Failed to sync single customer: " + externalUserid, e);
        }
    }

    private void processCustomerBatch(List<WxCpExternalContactInfo> batch) {
        transactionTemplate.execute(status -> {
            List<String> externalUserids = batch.stream()
                    .map(info -> info.getExternalContact().getExternalUserId())
                    .collect(Collectors.toList());

            // Batch fetch existing customers and relations to minimize DB calls
            Map<String, WecomCustomer> existingCustomers = customerRepository.findByExternalUseridIn(externalUserids)
                    .stream().collect(Collectors.toMap(WecomCustomer::getExternalUserid, c -> c));
            
            // Find existing relations for these customers
            List<WecomCustomerRelation> relations = customerRelationRepository.findByExternalUseridIn(externalUserids);
            // Mark all filtered relations as status 1 (inactive) initially
            // relations.forEach(r -> r.setStatus(1));
            Map<String, WecomCustomerRelation> relationMap = relations.stream()
                    .collect(Collectors.toMap(r -> r.getUserid() + ":" + r.getExternalUserid(), r -> r));

            List<WecomCustomer> customersToSave = new ArrayList<>();
            List<WecomCustomerRelation> relationsToSave = new ArrayList<>();
            List<WecomCustomerTag> tagsToSave = new ArrayList<>();
            Set<String> processedTags = new HashSet<>();

            for (WxCpExternalContactInfo contactInfo : batch) {
                String externalUserid = contactInfo.getExternalContact().getExternalUserId();
                
                // 1. Prepare Customer
                WecomCustomer customer = existingCustomers.getOrDefault(externalUserid, new WecomCustomer());
                customer.setExternalUserid(externalUserid);
                customer.setName(contactInfo.getExternalContact().getName());
                customer.setAvatar(contactInfo.getExternalContact().getAvatar());
                customer.setType(contactInfo.getExternalContact().getType());
                customer.setGender(contactInfo.getExternalContact().getGender());
                customer.setUnionid(contactInfo.getExternalContact().getUnionId());
                customersToSave.add(customer);

                // 2. Prepare Relations and Tags
                if (contactInfo.getFollowedUsers() != null) {
                    for (FollowedUser followedUser : contactInfo.getFollowedUsers()) {
                        String key = followedUser.getUserId() + ":" + externalUserid;
                        WecomCustomerRelation relation = relationMap.getOrDefault(key, new WecomCustomerRelation());
                        
                        relation.setUserid(followedUser.getUserId());
                        relation.setExternalUserid(externalUserid);
                        relation.setRemark(followedUser.getRemark());
                        relation.setDescription(followedUser.getDescription());
                        if (followedUser.getAddWay() != null) {
                            try {
                                relation.setAddWay(Integer.parseInt(followedUser.getAddWay()));
                            } catch (Exception ignored) {}
                        }
                        relation.setState(followedUser.getState());
                        if (relation.getStatus() == null) {
                             relation.setStatus(0); // Active
                        }
                        relationsToSave.add(relation);

                        if (followedUser.getTags() != null) {
                            for (Tag tag : followedUser.getTags()) {
                                // Deduplicate by (external_userid, userid, tag_id)
                                String tagKey = externalUserid + ":" + followedUser.getUserId() + ":" + tag.getTagId();
                                if (processedTags.add(tagKey)) {
                                    WecomCustomerTag customerTag = new WecomCustomerTag();
                                    customerTag.setExternalUserid(externalUserid);
                                    customerTag.setUserid(followedUser.getUserId());
                                    customerTag.setTagId(tag.getTagId());
                                    tagsToSave.add(customerTag);
                                }
                            }
                        }
                    }
                }
            }

            customerRepository.saveAll(customersToSave);
            customerRelationRepository.saveAll(relationsToSave);
            
            // Delete old tags and save new ones for this batch
            if (!externalUserids.isEmpty()) {
                customerTagRepository.deleteByExternalUseridIn(externalUserids);
                customerTagRepository.flush(); // Ensure delete is executed before save
            }
            
            if (!tagsToSave.isEmpty()) {
                customerTagRepository.saveAll(tagsToSave);
            }
            return null;
        });
    }

    @Async("bizAsyncExecutor")
    public void syncGroupChats() {
        WecomSyncLog syncLog = new WecomSyncLog();
        syncLog.setSyncType("GROUP_CHAT_SYNC");
        syncLog.setStatus(0); // Running
        syncLog = syncLogRepository.save(syncLog);
        try {
            String cursor = null;
            int limit = 1000;
            boolean hasMore = true;
            Set<String> syncChatIds = new HashSet<>();
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            while (hasMore) {
                WxCpUserExternalGroupChatList list = wxCpServiceManager.getWxCpService().getExternalContactService()
                        .listGroupChat(limit, cursor, 0, null);

                List<WxCpUserExternalGroupChatList.ChatStatus> chatList = list.getGroupChatList();
                if (chatList == null || chatList.isEmpty()) {
                    break;
                }

                for (WxCpUserExternalGroupChatList.ChatStatus status : chatList) {
                    syncChatIds.add(status.getChatId());
                    futures.add(CompletableFuture.runAsync(() -> 
                        syncSingleGroupChat(status.getChatId(), status.getStatus()), bizAsyncExecutor));
                }

                cursor = list.getNextCursor();
                if (cursor == null || cursor.isEmpty()) {
                    hasMore = false;
                }
            }

            // Wait for all individual sync tasks to complete
            if (!futures.isEmpty()) {
                log.info("Waiting for {} group chat sync tasks to complete...", futures.size());
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            }

            // Identify lost group chats
            List<String> dbActiveChatIds = groupChatRepository.findChatIdsByStatus(0);
            Set<String> lostChatIds = dbActiveChatIds.stream()
                    .filter(id -> !syncChatIds.contains(id))
                    .collect(Collectors.toSet());

            if (!lostChatIds.isEmpty()) {
                log.info("Detected {} lost group chats. Marking as deleted (status=2).", lostChatIds.size());
                groupChatRepository.markAsDeletedByChatIdIn(lostChatIds);
            }

            syncLog.setStatus(1); // Success
            syncLogRepository.save(syncLog);
            log.info("Finished sync group chats. Total synced: {}", syncChatIds.size());
        } catch (Exception e) {
            log.error("Failed to sync group-chats", e);
            syncLog.setStatus(2); // Failed
            syncLog.setErrorMsg(e.getMessage());
            syncLogRepository.save(syncLog);
        }
    }

    public void syncSingleGroupChat(String chatId, Integer status) {
        try {
            WxCpUserExternalGroupChatInfo info = wxCpServiceManager.getWxCpService().getExternalContactService().getGroupChat(chatId, 1);
            if (info == null || info.getGroupChat() == null) {
                return;
            }

            final WxCpUserExternalGroupChatInfo.GroupChat wxChat = info.getGroupChat();
            for (int i = 0; i < 3; i++) {
                try {
                    transactionTemplate.execute(txStatus -> {
                        WecomGroupChat chat = groupChatRepository.findByChatId(chatId).orElse(new WecomGroupChat());
                        chat.setChatId(wxChat.getChatId());
                        chat.setName(wxChat.getName());
                        chat.setOwner(wxChat.getOwner());
                        if (wxChat.getCreateTime() != null) {
                            chat.setCreateTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(wxChat.getCreateTime()), ZoneId.systemDefault()));
                        }
                        chat.setStatus(status);

                        List<WxCpUserExternalGroupChatInfo.GroupMember> memberList = wxChat.getMemberList();
                        chat.setMemberCount(memberList != null ? memberList.size() : 0);

                        groupChatRepository.save(chat);

                        // Sync members
                        memberRepository.deleteByChatId(chatId);
                        if (memberList != null) {
                            List<WecomGroupChatMember> membersToSave = new ArrayList<>();
                            for (WxCpUserExternalGroupChatInfo.GroupMember wxMember : memberList) {
                                WecomGroupChatMember member = new WecomGroupChatMember();
                                member.setChatId(chatId);
                                member.setUserid(wxMember.getUserId());
                                member.setType(wxMember.getType());
                                if (wxMember.getJoinTime() != null) {
                                    member.setJoinTime(LocalDateTime.ofInstant(Instant.ofEpochSecond(wxMember.getJoinTime()), ZoneId.systemDefault()));
                                }
                                member.setJoinScene(wxMember.getJoinScene());
                                if (wxMember.getInvitor() != null) {
                                    member.setInvitor(wxMember.getInvitor().getUserId());
                                }
                                membersToSave.add(member);
                            }
                            if (!membersToSave.isEmpty()) {
                                memberRepository.saveAll(membersToSave);
                            }
                        }
                        return null;
                    });
                    return; // Success, exit method
                } catch (ConcurrencyFailureException e) {
                    if (i == 2) throw e;
                    log.warn("Deadlock detected during sync of group chat {}, retrying {}/3...", chatId, i + 1);
                    try { Thread.sleep(200); } catch (InterruptedException ignored) {}
                }
            }
        } catch (Exception e) {
            log.error("Sync single group chat error: " + chatId, e);
        }
    }

    /**
     * Mark a group chat as dismissed (status=1).
     * @param chatId The WeCom chat_id
     */
    public void dismissGroupChat(String chatId) {
        log.info("Marking group chat as dismissed: {}", chatId);
        groupChatRepository.findByChatId(chatId).ifPresent(chat -> {
            chat.setStatus(1); // 1 - 已解散
            groupChatRepository.save(chat);
        });
    }




    @Async("bizAsyncExecutor")
    public void initialSync() {
        log.info("Starting automated initial synchronization...");
        try {
            syncAllDepartments();
            syncAllUsers();
            syncCorpTags();
            syncAllCustomers();
            syncGroupChats();
            log.info("Automated initial synchronization completed successfully.");
        } catch (Exception e) {
            log.error("Error during automated initial synchronization", e);
        }
    }

}
