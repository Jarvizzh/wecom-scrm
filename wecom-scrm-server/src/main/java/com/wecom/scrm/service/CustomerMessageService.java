package com.wecom.scrm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecom.scrm.dto.CustomerMessageDTO;
import com.wecom.scrm.dto.MomentDTO;
import com.wecom.scrm.entity.WecomCustomerMessage;
import com.wecom.scrm.repository.WecomCustomerMessageRepository;
import com.wecom.scrm.repository.WecomCustomerRepository;
import com.wecom.scrm.repository.WecomUserRepository;
import lombok.extern.slf4j.Slf4j;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.bean.external.WxCpMsgTemplate;
import me.chanjar.weixin.cp.bean.external.WxCpMsgTemplateAddResult;
import me.chanjar.weixin.cp.bean.external.contact.WxCpGroupMsgSendResult;
import me.chanjar.weixin.cp.bean.external.contact.WxCpGroupMsgTaskResult;
import me.chanjar.weixin.cp.bean.external.msg.Attachment;
import me.chanjar.weixin.cp.bean.external.msg.Link;
import me.chanjar.weixin.cp.bean.external.msg.MiniProgram;
import me.chanjar.weixin.cp.bean.external.msg.Text;
import me.chanjar.weixin.cp.bean.external.msg.Image;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CustomerMessageService {

    private final WecomCustomerMessageRepository messageRepository;
    private final WecomUserRepository userRepository;
    private final WecomCustomerRepository customerRepository;
    private final WxCpServiceManager wxCpServiceManager;
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;
    private final Executor customerMessageExecutor;

    public CustomerMessageService(WecomCustomerMessageRepository messageRepository,
            WecomUserRepository userRepository,
            WecomCustomerRepository customerRepository,
            WxCpServiceManager wxCpServiceManager,
            EntityManager entityManager,
            ObjectMapper objectMapper,
            @Qualifier("customerMessageExecutor") Executor customerMessageExecutor) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.wxCpServiceManager = wxCpServiceManager;
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
        this.customerMessageExecutor = customerMessageExecutor;
    }

    public WecomCustomerMessage createMessageTask(CustomerMessageDTO.CreateRequest request, String creatorUserid)
            throws Exception {
        WecomCustomerMessage message = new WecomCustomerMessage();
        message.setTaskName(request.getTaskName());
        message.setSendType(request.getSendType());
        message.setSendTime(request.getSendTime());

        if (Integer.valueOf(1).equals(request.getSendType())) {
            if (request.getSendTime() == null) {
                throw new IllegalArgumentException("定时发送时间不能为空 / Scheduled send time must be provided");
            }
            if (request.getSendTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("发送时间不得早于当前时间 / Scheduled send time cannot be in the past");
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
        if (request.getSenderList() != null && !request.getSenderList().isEmpty()) {
            message.setSenderList(objectMapper.writeValueAsString(request.getSenderList()));
        }
        message.setStatus(0); // Pending
        message.setCreatorUserid(creatorUserid);

        // Calculate target count
        List<String> targetExternalUserids = findMatchedExternalUserids(message);
        message.setTargetCount(targetExternalUserids.size());

        message = messageRepository.save(message);

        if (message.getSendType() == 0) {
            this.executeTask(message);
        }

        return message;
    }

    public void executeTask(WecomCustomerMessage message) {
        // Atomic claim at repository level
        int updated = messageRepository.updateStatusIfPending(message.getId(), 0, 1);
        if (updated <= 0) {
            log.info("Task {} already claimed or processed by another thread.", message.getId());
            return;
        }

        try {
            message.setStatus(1);

            List<String> senders = new ArrayList<>();
            if (message.getSenderList() != null) {
                senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {});
            }

            if (senders.isEmpty()) {
                List<String> targetExternalUserids = findMatchedExternalUserids(message);
                if (targetExternalUserids.isEmpty()) {
                    message.setStatus(2);
                    message.setFailMsg("No matching customers found.");
                    messageRepository.save(message);
                    return;
                }
                WxCpMsgTemplate template = new WxCpMsgTemplate();
                template.setExternalUserid(targetExternalUserids);
                populateTemplateContent(template, message);
                
                WxCpMsgTemplateAddResult result = wxCpServiceManager.getWxCpService().getExternalContactService()
                        .addMsgTemplate(template);
                if (result != null && result.getMsgId() != null) {
                    message.setMsgid(result.getMsgId());
                    message.setStatus(2);
                } else {
                    message.setStatus(3);
                    message.setFailMsg(result != null ? result.toString() : "Unknown WeCom error");
                }
            } else {
                List<String> msgIds = Collections.synchronizedList(new ArrayList<>());
                StringBuilder failMsgs = new StringBuilder();
                AtomicBoolean allEmpty = new AtomicBoolean(true);
                
                List<CompletableFuture<Void>> futures = new ArrayList<>();
                for (String sender : senders) {
                    try {
                        futures.add(CompletableFuture.runAsync(() -> {
                            try {
                                List<String> targetExternalUserids = findMatchedExternalUserids(message, sender);
                                if (targetExternalUserids.isEmpty()) {
                                    return;
                                }
                                allEmpty.set(false);
                                WxCpMsgTemplate template = new WxCpMsgTemplate();
                                template.setExternalUserid(targetExternalUserids);
                                template.setSender(sender);
                                populateTemplateContent(template, message);
                                
                                WxCpMsgTemplateAddResult result = wxCpServiceManager.getWxCpService().getExternalContactService()
                                        .addMsgTemplate(template);
                                if (result != null && result.getMsgId() != null) {
                                    msgIds.add(result.getMsgId());
                                } else {
                                    synchronized (failMsgs) {
                                        failMsgs.append("Sender ").append(sender).append(" failed: empty result. ");
                                    }
                                }
                            } catch (Throwable t) {
                                log.error("Failed to execute broadcast task for sender: {}", sender, t);
                                allEmpty.set(false);
                                synchronized (failMsgs) {
                                    failMsgs.append("Sender ").append(sender).append(" failed: ")
                                            .append(t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName()).append(". ");
                                }
                            }
                        }, customerMessageExecutor));
                    } catch (Throwable e) {
                        log.error("Failed to submit task for sender: {}", sender, e);
                        allEmpty.set(false);
                        synchronized (failMsgs) {
                            failMsgs.append("Sender ").append(sender).append(" submission failed: ").append(e.getMessage()).append(". ");
                        }
                    }
                }
                
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
                
                if (allEmpty.get()) {
                    message.setStatus(2);
                    message.setFailMsg("No matching customers found for selected senders.");
                } else if (msgIds.isEmpty()) {
                    message.setStatus(3);
                    message.setFailMsg(failMsgs.length() > 0 ? failMsgs.toString() : "All senders failed.");
                } else {
                    message.setMsgid(String.join(",", msgIds));
                    message.setStatus(2); // Finished partially or fully
                    if (failMsgs.length() > 0) {
                        message.setFailMsg(failMsgs.toString());
                    }
                }
            }
            messageRepository.save(message);

        } catch (Throwable e) {
            log.error("Failed to execute broadcast task: {}", message.getId(), e);
            message.setStatus(3);
            message.setFailMsg(e.getMessage());
            messageRepository.save(message);
        }
    }

    private void populateTemplateContent(WxCpMsgTemplate template, WecomCustomerMessage message) throws Exception {
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
    List<String> findMatchedExternalUserids(WecomCustomerMessage message) throws Exception {
        return findMatchedExternalUserids(message, null);
    }

    @SuppressWarnings("unchecked")
    List<String> findMatchedExternalUserids(WecomCustomerMessage message, String singleSender) throws Exception {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT r.external_userid FROM wecom_customer_relation r ");

        List<String> wheres = new ArrayList<>();
        wheres.add("r.status = 0");

        if (singleSender != null) {
            wheres.add("r.userid = :singleSender");
        } else if (message.getSenderList() != null) {
            List<String> senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {
            });
            if (!senders.isEmpty()) {
                wheres.add("r.userid IN (:senders)");
            }
        }

        CustomerMessageDTO.TargetCondition condition = null;
        if (message.getTargetType() == 1 && message.getTargetCondition() != null) {
            condition = objectMapper.readValue(message.getTargetCondition(), CustomerMessageDTO.TargetCondition.class);
        }

        if (condition != null) {
            if (condition.getAddTimeStart() != null) {
                wheres.add("r.create_time >= :addTimeStart");
            }
            if (condition.getAddTimeEnd() != null) {
                wheres.add("r.create_time <= :addTimeEnd");
            }

            // Include Tags
            if (condition.getIncludeTags() != null && !condition.getIncludeTags().isEmpty()) {
                if (Boolean.TRUE.equals(condition.getIncludeTagsAny())) {
                    // ANY: Exists in wecom_customer_tag with any of the IDs
                    sql.append(
                            "JOIN wecom_customer_tag t ON r.external_userid = t.external_userid AND r.userid = t.userid ");
                    wheres.add("t.tag_id IN (:includeTags)");
                } else {
                    // ALL: For each tag, an entry must exist
                    for (int i = 0; i < condition.getIncludeTags().size(); i++) {
                        sql.append("JOIN wecom_customer_tag t").append(i).append(" ON r.external_userid = t").append(i)
                                .append(".external_userid AND r.userid = t").append(i).append(".userid ");
                        wheres.add("t" + i + ".tag_id = :includeTag" + i);
                    }
                }
            }

            // Exclude Tags
            if (condition.getExcludeTags() != null && !condition.getExcludeTags().isEmpty()) {
                wheres.add(
                        "NOT EXISTS (SELECT 1 FROM wecom_customer_tag et WHERE et.external_userid = r.external_userid AND et.userid = r.userid AND et.tag_id IN (:excludeTags))");
            }
        }

        if (!wheres.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", wheres));
        }

        Query query = entityManager.createNativeQuery(sql.toString());
        // Bind parameters
        if (singleSender != null) {
            query.setParameter("singleSender", singleSender);
        } else if (message.getSenderList() != null) {
            List<String> senders = objectMapper.readValue(message.getSenderList(), new TypeReference<List<String>>() {
            });
            if (!senders.isEmpty()) {
                query.setParameter("senders", senders);
            }
        }
        if (condition != null) {
            if (condition.getAddTimeStart() != null)
                query.setParameter("addTimeStart", condition.getAddTimeStart());
            if (condition.getAddTimeEnd() != null)
                query.setParameter("addTimeEnd", condition.getAddTimeEnd());
            if (condition.getIncludeTags() != null && !condition.getIncludeTags().isEmpty()) {
                if (Boolean.TRUE.equals(condition.getIncludeTagsAny())) {
                    query.setParameter("includeTags", condition.getIncludeTags());
                } else {
                    for (int i = 0; i < condition.getIncludeTags().size(); i++) {
                        query.setParameter("includeTag" + i, condition.getIncludeTags().get(i));
                    }
                }
            }
            if (condition.getExcludeTags() != null && !condition.getExcludeTags().isEmpty()) {
                query.setParameter("excludeTags", condition.getExcludeTags());
            }
        }

        List<String> result = (List<String>) query.getResultList();
        return result;
    }

    public List<WecomCustomerMessage> listTasks() {
        return messageRepository.findAllByOrderByCreateTimeDesc();
    }

    public WecomCustomerMessage getTask(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
    }

    @Transactional
    public WecomCustomerMessage updateTask(Long id, CustomerMessageDTO.CreateRequest request) throws Exception {
        WecomCustomerMessage message = getTask(id);
        if (message.getStatus() != 0) {
            throw new RuntimeException("Only pending tasks can be modified.");
        }

        message.setTaskName(request.getTaskName());
        message.setSendType(request.getSendType());
        message.setSendTime(request.getSendTime());

        // Validation (same as create)
        if (Integer.valueOf(1).equals(request.getSendType())) {
            if (request.getSendTime() == null) {
                throw new IllegalArgumentException("定时发送时间不能为空 / Scheduled send time must be provided");
            }
            if (request.getSendTime().isBefore(LocalDateTime.now())) {
                throw new IllegalArgumentException("发送时间不得早于当前时间 / Scheduled send time cannot be in the past");
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

        if (request.getSenderList() != null) {
            message.setSenderList(objectMapper.writeValueAsString(request.getSenderList()));
        } else {
            message.setSenderList(null);
        }

        // Recalculate target count
        List<String> targetExternalUserids = findMatchedExternalUserids(message);
        message.setTargetCount(targetExternalUserids.size());

        message = messageRepository.save(message);

        // If changed to immediate send, execute now
        if (message.getSendType() == 0) {
            this.executeTask(message);
        }

        return message;
    }

    @Transactional
    public void deleteTask(Long id) {
        WecomCustomerMessage message = getTask(id);
        if (message.getStatus() != 0 && message.getStatus() != 3) {
            throw new RuntimeException("Only pending or failed tasks can be deleted.");
        }
        messageRepository.delete(message);
    }

    public CustomerMessageDTO.SendResult getSendResult(Long id) throws Exception {
        WecomCustomerMessage message = getTask(id);
        if (message.getMsgid() == null || message.getMsgid().trim().isEmpty()) {
            return null;
        }

        String[] msgIds = message.getMsgid().split(",");
        CustomerMessageDTO.SendResult sendResult = new CustomerMessageDTO.SendResult();
        List<CustomerMessageDTO.MemberTask> allTasks = new ArrayList<>();

        for (String msgid : msgIds) {
            String cleanMsgid = msgid.trim();
            if (cleanMsgid.isEmpty()) continue;
            
            try {
                WxCpGroupMsgTaskResult result = wxCpServiceManager.getWxCpService().getExternalContactService()
                        .getGroupMsgTask(cleanMsgid, 1000, null);

                if (result != null && result.getTaskList() != null) {
                    List<CustomerMessageDTO.MemberTask> tasks = objectMapper.convertValue(result.getTaskList(),
                            new TypeReference<List<CustomerMessageDTO.MemberTask>>() {});

                    for (CustomerMessageDTO.MemberTask task : tasks) {
                        if (task.getUserId() != null) {
                            userRepository.findByUserid(task.getUserId()).ifPresent(u -> task.setUserName(u.getName()));

                            try {
                                WxCpGroupMsgSendResult customerResults = wxCpServiceManager.getWxCpService()
                                        .getExternalContactService().getGroupMsgSendResult(cleanMsgid, task.getUserId(), 1000, null);
                                
                                if (customerResults != null && customerResults.getSendList() != null) {
                                    List<?> details = customerResults.getSendList();
                                    task.setTotalCount(details.size());

                                    List<CustomerMessageDTO.CustomerResult> results = objectMapper.convertValue(details,
                                            new TypeReference<List<CustomerMessageDTO.CustomerResult>>() {});
                                    task.setSuccessCount((int) results.stream()
                                            .filter(r -> Integer.valueOf(1).equals(r.getStatus())).count());
                                    task.setFailCount((int) results.stream()
                                            .filter(r -> Integer.valueOf(2).equals(r.getStatus())
                                                    || Integer.valueOf(3).equals(r.getStatus())).count());
                                }
                            } catch (Exception e) {
                                log.error("Failed to fetch customer results for msgid: {}, user: {}", cleanMsgid, task.getUserId(), e);
                            }
                        }
                        allTasks.add(task);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to fetch task result for msgid: {}", cleanMsgid, e);
            }
        }

        sendResult.setTaskList(allTasks);
        return sendResult;
    }

    public CustomerMessageDTO.MemberSendDetail getMemberSendDetail(Long id, String userid) throws Exception {
        WecomCustomerMessage message = getTask(id);
        CustomerMessageDTO.MemberSendDetail detail = new CustomerMessageDTO.MemberSendDetail();
        detail.setUserId(userid);
        userRepository.findByUserid(userid).ifPresent(u -> detail.setUserName(u.getName()));

        List<CustomerMessageDTO.CustomerResult> allCustomerResults = new ArrayList<>();
        
        if (message.getMsgid() != null && !message.getMsgid().trim().isEmpty()) {
            String[] msgIds = message.getMsgid().split(",");
            for (String msgid : msgIds) {
                String cleanMsgid = msgid.trim();
                if (cleanMsgid.isEmpty()) continue;
                
                try {
                    WxCpGroupMsgSendResult result = wxCpServiceManager.getWxCpService().getExternalContactService()
                            .getGroupMsgSendResult(cleanMsgid, userid, 1000, null);
                    List<?> sendList = result.getSendList();

                    if (sendList != null && !sendList.isEmpty()) {
                        List<CustomerMessageDTO.CustomerResult> customerResults = objectMapper.convertValue(sendList,
                                new TypeReference<List<CustomerMessageDTO.CustomerResult>>() {
                                });
                        for (CustomerMessageDTO.CustomerResult cr : customerResults) {
                            if (cr.getStatus() == 2) {
                                cr.setFailReason("因客户非好友导致失败");
                            } else if (cr.getStatus() == 3) {
                                cr.setFailReason("因客户当天接收消息已达上限导致发送失败");
                            } else if (cr.getStatus() == 0) {
                                cr.setFailReason("未发送");
                            }

                            if (cr.getExternalUserid() != null) {
                                customerRepository.findByExternalUserid(cr.getExternalUserid())
                                        .ifPresent(c -> cr.setCustomerName(c.getName()));
                            }
                        }
                        allCustomerResults.addAll(customerResults);
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch member details for msgid: {}, userid: {}", cleanMsgid, userid);
                }
            }
        }
        
        detail.setCustomerList(allCustomerResults);
        return detail;
    }
}
