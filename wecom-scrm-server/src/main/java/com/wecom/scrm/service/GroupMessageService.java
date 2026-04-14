package com.wecom.scrm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecom.scrm.dto.GroupMessageDTO;
import com.wecom.scrm.dto.MomentDTO;
import com.wecom.scrm.entity.WecomGroupMessage;
import com.wecom.scrm.repository.GroupChatRepository;
import com.wecom.scrm.repository.WecomGroupMessageRepository;
import lombok.extern.slf4j.Slf4j;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.bean.external.WxCpMsgTemplate;
import me.chanjar.weixin.cp.bean.external.WxCpMsgTemplateAddResult;
import me.chanjar.weixin.cp.bean.external.contact.WxCpGroupMsgResult;
import me.chanjar.weixin.cp.bean.external.msg.Attachment;
import me.chanjar.weixin.cp.bean.external.msg.Link;
import me.chanjar.weixin.cp.bean.external.msg.MiniProgram;
import me.chanjar.weixin.cp.bean.external.msg.Text;
import me.chanjar.weixin.cp.bean.external.msg.Image;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GroupMessageService {

    private final WecomGroupMessageRepository messageRepository;
    private final GroupChatRepository groupChatRepository;
    private final WxCpServiceManager wxCpServiceManager;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    public GroupMessageService(WecomGroupMessageRepository messageRepository,
            GroupChatRepository groupChatRepository,
            WxCpServiceManager wxCpServiceManager,
            EntityManager entityManager,
            ObjectMapper objectMapper) {
        this.messageRepository = messageRepository;
        this.groupChatRepository = groupChatRepository;
        this.wxCpServiceManager = wxCpServiceManager;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public WecomGroupMessage createMessageTask(GroupMessageDTO.CreateRequest request, String creatorUserid)
            throws Exception {
        WecomGroupMessage message = new WecomGroupMessage();
        message.setTaskName(request.getTaskName());
        message.setSendType(request.getSendType());
        message.setSendTime(request.getSendTime());

        if (Integer.valueOf(1).equals(request.getSendType())) {
            if (request.getSendTime() == null) {
                throw new IllegalArgumentException("定时发送时间不能为空");
            }
            if (request.getSendTime().isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("发送时间不得早于当前时间");
            }
        }

        message.setTargetType(request.getTargetType());
        if (request.getTargetCondition() != null) {
            message.setTargetCondition(objectMapper.writeValueAsString(request.getTargetCondition()));
        }
        message.setContent(request.getText());
        if (request.getAttachments() != null) {
            message.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        }
        message.setStatus(0); // Pending
        message.setCreatorUserid(creatorUserid);

        // Calculate target group count
        List<String> chatIds = findMatchedChatIds(message);
        message.setTargetCount(chatIds.size());

        message = messageRepository.save(message);

        if (message.getSendType() == 0) {
            this.executeTask(message);
        }

        return message;
    }

    @Transactional
    public void executeTask(WecomGroupMessage message) {
        try {
            message.setStatus(1); // Sending
            messageRepository.save(message);

            List<String> owners = findMatchedOwners(message);
            if (owners.isEmpty()) {
                message.setStatus(2);
                message.setFailMsg("No matching group chats found.");
                messageRepository.save(message);
                return;
            }

            List<String> msgIds = new ArrayList<>();
            StringBuilder failMsgs = new StringBuilder();
            boolean anySuccess = false;

            for (String owner : owners) {
                List<String> chatIds = findMatchedChatIds(message, owner);
                if (chatIds.isEmpty()) continue;

                // Split by 2000 per call (WeCom API limit)
                for (int i = 0; i < chatIds.size(); i += 2000) {
                    List<String> batchIds = chatIds.subList(i, Math.min(i + 2000, chatIds.size()));
                    
                    WxCpMsgTemplate template = new WxCpMsgTemplate();
                    template.setChatType("group");
                    template.setChatIdList(batchIds);
                    template.setSender(owner);
                    populateTemplateContent(template, message);

                    try {
                        WxCpMsgTemplateAddResult result = wxCpServiceManager.getWxCpService().getExternalContactService()
                                .addMsgTemplate(template);
                        if (result != null && result.getMsgId() != null) {
                            msgIds.add(result.getMsgId());
                            anySuccess = true;
                        } else {
                            failMsgs.append("Owner ").append(owner).append(" failed: empty result. ");
                        }
                    } catch (Exception e) {
                        log.error("Failed to execute group broadcast task for owner: {}", owner, e);
                        failMsgs.append("Owner ").append(owner).append(" failed: ").append(e.getMessage()).append(". ");
                    }
                }
            }

            if (!anySuccess) {
                message.setStatus(3);
                message.setFailMsg(failMsgs.length() > 0 ? failMsgs.toString() : "All group tasks failed.");
            } else {
                message.setMsgid(String.join(",", msgIds));
                message.setStatus(2);
                if (failMsgs.length() > 0) {
                    message.setFailMsg(failMsgs.toString());
                }
            }
            messageRepository.save(message);

        } catch (Exception e) {
            log.error("Failed to execute group broadcast task: {}", message.getId(), e);
            message.setStatus(3);
            message.setFailMsg(e.getMessage());
            messageRepository.save(message);
        }
    }

    private void populateTemplateContent(WxCpMsgTemplate template, WecomGroupMessage message) throws Exception {
        if (message.getContent() != null) {
            Text text = new Text();
            text.setContent(message.getContent());
            template.setText(text);
        }

        if (message.getAttachments() != null) {
            List<MomentDTO.Attachment> atts = objectMapper.readValue(message.getAttachments(),
                    new TypeReference<List<MomentDTO.Attachment>>() {
                    });
            List<Attachment> attachments = new ArrayList<>();
            for (MomentDTO.Attachment att : atts) {
                Attachment attachment = new Attachment();
                if ("link".equals(att.getMsgtype()) && att.getLink() != null) {
                    Link link = new Link();
                    link.setTitle(att.getLink().getTitle());
                    link.setUrl(att.getLink().getUrl());
                    link.setDesc(att.getLink().getDesc());
                    link.setPicUrl(att.getLink().getPicUrl());
                    attachment.setLink(link);
                } else if ("miniprogram".equals(att.getMsgtype()) && att.getMiniprogram() != null) {
                    MiniProgram mp = new MiniProgram();
                    mp.setTitle(att.getMiniprogram().getTitle());
                    mp.setAppid(att.getMiniprogram().getAppid());
                    mp.setPage(att.getMiniprogram().getPage());
                    mp.setPicMediaId(att.getMiniprogram().getPicMediaId());
                    attachment.setMiniProgram(mp);
                } else if ("image".equals(att.getMsgtype()) && att.getImage() != null) {
                    Image image = new Image();
                    image.setMediaId(att.getImage().getMediaId());
                    attachment.setImage(image);
                }
                attachments.add(attachment);
            }
            template.setAttachments(attachments);
        }
    }

    @SuppressWarnings("unchecked")
    List<String> findMatchedChatIds(WecomGroupMessage message) throws Exception {
        return findMatchedChatIds(message, null);
    }

    @SuppressWarnings("unchecked")
    List<String> findMatchedChatIds(WecomGroupMessage message, String singleOwner) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT g.chat_id FROM wecom_group_chat g");

        List<String> wheres = new ArrayList<>();
        wheres.add("g.status = 0"); // Only active group chats

        GroupMessageDTO.TargetCondition condition = null;
        if (message.getTargetType() == 1 && message.getTargetCondition() != null) {
            condition = objectMapper.readValue(message.getTargetCondition(), GroupMessageDTO.TargetCondition.class);
        }

        if (singleOwner != null) {
            wheres.add("g.owner = :singleOwner");
        }

        if (condition != null) {
            if (condition.getCreateTimeStart() != null) {
                wheres.add("g.create_time >= :createTimeStart");
            }
            if (condition.getCreateTimeEnd() != null) {
                wheres.add("g.create_time <= :createTimeEnd");
            }
            if (condition.getGroupNameKeywords() != null && !condition.getGroupNameKeywords().isEmpty()) {
                List<String> keywordConditions = new ArrayList<>();
                for (int i = 0; i < condition.getGroupNameKeywords().size(); i++) {
                    keywordConditions.add("g.name LIKE :keyword" + i);
                }
                wheres.add("(" + String.join(" OR ", keywordConditions) + ")");
            }
            if (condition.getOwnerUserids() != null && !condition.getOwnerUserids().isEmpty()) {
                wheres.add("g.owner IN (:ownerUserids)");
            }
        }

        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }

        Query query = entityManager.createNativeQuery(sql.toString());

        if (singleOwner != null) {
            query.setParameter("singleOwner", singleOwner);
        }

        if (condition != null) {
            if (condition.getCreateTimeStart() != null) {
                query.setParameter("createTimeStart", condition.getCreateTimeStart());
            }
            if (condition.getCreateTimeEnd() != null) {
                query.setParameter("createTimeEnd", condition.getCreateTimeEnd());
            }
            if (condition.getGroupNameKeywords() != null && !condition.getGroupNameKeywords().isEmpty()) {
                for (int i = 0; i < condition.getGroupNameKeywords().size(); i++) {
                    query.setParameter("keyword" + i, "%" + condition.getGroupNameKeywords().get(i) + "%");
                }
            }
            if (condition.getOwnerUserids() != null && !condition.getOwnerUserids().isEmpty()) {
                query.setParameter("ownerUserids", condition.getOwnerUserids());
            }
        }

        return (List<String>) query.getResultList();
    }


    @SuppressWarnings("unchecked")
    List<String> findMatchedOwners(WecomGroupMessage message) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT g.owner FROM wecom_group_chat g");

        List<String> wheres = new ArrayList<>();
        wheres.add("g.status = 0");

        GroupMessageDTO.TargetCondition condition = null;
        if (message.getTargetType() == 1 && message.getTargetCondition() != null) {
            condition = objectMapper.readValue(message.getTargetCondition(), GroupMessageDTO.TargetCondition.class);
        }

        if (condition != null) {
            if (condition.getCreateTimeStart() != null) wheres.add("g.create_time >= :createTimeStart");
            if (condition.getCreateTimeEnd() != null) wheres.add("g.create_time <= :createTimeEnd");
            if (condition.getGroupNameKeywords() != null && !condition.getGroupNameKeywords().isEmpty()) {
                List<String> keywordConditions = new ArrayList<>();
                for (int i = 0; i < condition.getGroupNameKeywords().size(); i++) {
                    keywordConditions.add("g.name LIKE :keyword" + i);
                }
                wheres.add("(" + String.join(" OR ", keywordConditions) + ")");
            }
            if (condition.getOwnerUserids() != null && !condition.getOwnerUserids().isEmpty()) {
                wheres.add("g.owner IN (:ownerUserids)");
            }
        }

        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }

        Query query = entityManager.createNativeQuery(sql.toString());
        if (condition != null) {
            if (condition.getCreateTimeStart() != null) query.setParameter("createTimeStart", condition.getCreateTimeStart());
            if (condition.getCreateTimeEnd() != null) query.setParameter("createTimeEnd", condition.getCreateTimeEnd());
            if (condition.getGroupNameKeywords() != null && !condition.getGroupNameKeywords().isEmpty()) {
                for (int i = 0; i < condition.getGroupNameKeywords().size(); i++) {
                    query.setParameter("keyword" + i, "%" + condition.getGroupNameKeywords().get(i) + "%");
                }
            }
            if (condition.getOwnerUserids() != null && !condition.getOwnerUserids().isEmpty()) {
                query.setParameter("ownerUserids", condition.getOwnerUserids());
            }
        }
        return (List<String>) query.getResultList();
    }

    public List<WecomGroupMessage> listTasks() {
        return messageRepository.findAllByOrderByCreateTimeDesc();
    }

    public WecomGroupMessage getTask(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    @Transactional
    public WecomGroupMessage updateTask(Long id, GroupMessageDTO.CreateRequest request) throws Exception {
        WecomGroupMessage message = getTask(id);
        if (message.getStatus() != 0) {
            throw new RuntimeException("Only pending tasks can be modified.");
        }

        message.setTaskName(request.getTaskName());
        message.setSendType(request.getSendType());
        message.setSendTime(request.getSendTime());

        if (Integer.valueOf(1).equals(request.getSendType())) {
            if (request.getSendTime() == null) {
                throw new IllegalArgumentException("定时发送时间不能为空");
            }
            if (request.getSendTime().isBefore(java.time.LocalDateTime.now())) {
                throw new IllegalArgumentException("发送时间不得早于当前时间");
            }
        }

        message.setTargetType(request.getTargetType());
        if (request.getTargetCondition() != null) {
            message.setTargetCondition(objectMapper.writeValueAsString(request.getTargetCondition()));
        } else {
            message.setTargetCondition(null);
        }

        message.setContent(request.getText());
        if (request.getAttachments() != null) {
            message.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        } else {
            message.setAttachments(null);
        }

        // Recalculate target count
        List<String> chatIds = findMatchedChatIds(message);
        message.setTargetCount(chatIds.size());

        message = messageRepository.save(message);

        if (message.getSendType() == 0) {
            this.executeTask(message);
        }

        return message;
    }

    @Transactional
    public void deleteTask(Long id) {
        WecomGroupMessage message = getTask(id);
        if (message.getStatus() != 0 && message.getStatus() != 3) {
            throw new RuntimeException("Only pending or failed tasks can be deleted.");
        }
        messageRepository.delete(message);
    }

    public GroupMessageDTO.SendResult getSendResult(Long id) throws Exception {
        WecomGroupMessage message = getTask(id);
        if (message.getMsgid() == null || message.getMsgid().trim().isEmpty()) {
            return null;
        }

        String[] msgIds = message.getMsgid().split(",");
        GroupMessageDTO.SendResult sendResult = new GroupMessageDTO.SendResult();
        List<GroupMessageDTO.GroupSendRecord> groupList = new ArrayList<>();

        for (String msgid : msgIds) {
            String cleanMsgid = msgid.trim();
            if (cleanMsgid.isEmpty()) continue;
            
            try {
                WxCpGroupMsgResult rawResult = wxCpServiceManager.getWxCpService().getExternalContactService()
                        .getGroupMsgResult(cleanMsgid, 10000, null);

                if (rawResult != null && rawResult.getDetailList() != null) {
                    for (WxCpGroupMsgResult.ExternalContactGroupMsgDetailInfo item : rawResult.getDetailList()) {
                        GroupMessageDTO.GroupSendRecord record = new GroupMessageDTO.GroupSendRecord();
                        record.setChatId(item.getChatId());
                        record.setStatus(item.getStatus());
                        record.setSendTime(item.getSendTime());
                        groupChatRepository.findByChatId(item.getChatId())
                                .ifPresent(g -> record.setGroupName(g.getName()));
                        groupList.add(record);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to fetch group result for msgid: {}", cleanMsgid, e);
            }
        }

        sendResult.setGroupList(groupList);
        return sendResult;
    }
}
