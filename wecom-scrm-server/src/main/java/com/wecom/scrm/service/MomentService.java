package com.wecom.scrm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecom.scrm.dto.MomentDTO;
import com.wecom.scrm.entity.WecomMoment;
import com.wecom.scrm.entity.WecomMomentRecord;
import com.wecom.scrm.repository.WecomMomentRecordRepository;
import com.wecom.scrm.repository.WecomMomentRepository;
import lombok.extern.slf4j.Slf4j;
import com.wecom.scrm.config.WxCpServiceManager;
import me.chanjar.weixin.cp.bean.external.WxCpAddMomentResult;
import me.chanjar.weixin.cp.bean.external.WxCpAddMomentTask;
import me.chanjar.weixin.cp.bean.external.WxCpGetMomentTask;
import me.chanjar.weixin.cp.bean.external.WxCpGetMomentTaskResult;
import me.chanjar.weixin.cp.bean.external.moment.SenderList;
import me.chanjar.weixin.cp.bean.external.moment.VisibleRange;
import me.chanjar.weixin.cp.bean.external.moment.ExternalContactList;
import me.chanjar.weixin.cp.bean.external.msg.*;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MomentService {

    private final WxCpServiceManager wxCpServiceManager;
    private final WecomMomentRepository momentRepository;
    private final WecomMomentRecordRepository momentRecordRepository;
    private final MomentService self;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public MomentService(WxCpServiceManager wxCpServiceManager,
                         WecomMomentRepository momentRepository,
                         WecomMomentRecordRepository momentRecordRepository,
                         @Lazy MomentService self) {
        this.wxCpServiceManager = wxCpServiceManager;
        this.momentRepository = momentRepository;
        this.momentRecordRepository = momentRecordRepository;
        this.self = self;
    }

    @Transactional
    public WecomMoment createMomentTask(MomentDTO.CreateRequest request, String creatorUserid) throws Exception {
        // Save to database first
        WecomMoment moment = new WecomMoment();
        moment.setTaskName(request.getTaskName());
        moment.setText(request.getText());
        moment.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        if (request.getVisibleRange() != null) {
            moment.setVisibleRangeType(1);
            moment.setVisibleRangeUsers(objectMapper.writeValueAsString(request.getVisibleRange()));
        } else {
            moment.setVisibleRangeType(0);
        }
        moment.setCreatorUserid(creatorUserid);
        moment.setSendType(request.getSendType());
        
        if (request.getSendType() != null && request.getSendType() == 1) {
            moment.setSendTime(LocalDateTime.parse(request.getSendTime(), DATE_TIME_FORMATTER));
            moment.setStatus(3); // Scheduled
        } else {
            moment.setStatus(0); // Pending/Processing
        }

        moment = momentRepository.save(moment);

        // If immediate, publish to WeCom (Asynchronous call outside this transaction)
        if (moment.getSendType() == null || moment.getSendType() == 0) {
            self.publishMomentToWeCom(moment.getId());
        }
        
        return moment;
    }

    @Async("syncExecutor")
    public void publishMomentToWeCom(Long internalId) throws Exception {
        log.info("Starting async publish for moment task ID: {}", internalId);
        
        Optional<WecomMoment> momentOpt = momentRepository.findById(internalId);
        if (!momentOpt.isPresent()) {
            log.error("Moment task not found: {}", internalId);
            return;
        }
        WecomMoment moment = momentOpt.get();

        // Reconstruct request from entity JSON
        MomentDTO.CreateRequest request = new MomentDTO.CreateRequest();
        request.setText(moment.getText());
        if (moment.getAttachments() != null) {
            request.setAttachments(objectMapper.readValue(moment.getAttachments(), 
                new com.fasterxml.jackson.core.type.TypeReference<List<MomentDTO.Attachment>>() {}));
        }
        if (moment.getVisibleRangeUsers() != null) {
            request.setVisibleRange(objectMapper.readValue(moment.getVisibleRangeUsers(), MomentDTO.VisibleRange.class));
        }

        WxCpAddMomentTask task = new WxCpAddMomentTask();
        
        // Text
        if (request.getText() != null) {
            Text text = new Text();
            text.setContent(request.getText());
            task.setText(text);
        }

        // Attachments
        if (request.getAttachments() != null && !request.getAttachments().isEmpty()) {
            List<Attachment> attachments = new ArrayList<>();
            for (MomentDTO.Attachment att : request.getAttachments()) {
                Attachment attachment = new Attachment();
                if ("image".equals(att.getMsgtype()) && att.getImage() != null) {
                    Image image = new Image();
                    image.setMediaId(att.getImage().getMediaId());
                    attachment.setImage(image);
                } else if ("video".equals(att.getMsgtype()) && att.getVideo() != null) {
                    Video video = new Video();
                    video.setMediaId(att.getVideo().getMediaId());
                    video.setThumbMediaId(att.getVideo().getThumbMediaId());
                    attachment.setVideo(video);
                } else if ("link".equals(att.getMsgtype()) && att.getLink() != null) {
                    Link link = new Link();
                    link.setTitle(att.getLink().getTitle());
                    link.setUrl(att.getLink().getUrl());
                    link.setPicUrl(att.getLink().getPicUrl());
                    link.setDesc(att.getLink().getDesc());
                    attachment.setLink(link);
                } else if ("miniprogram".equals(att.getMsgtype()) && att.getMiniprogram() != null) {
                    MiniProgram miniProgram = new MiniProgram();
                    miniProgram.setTitle(att.getMiniprogram().getTitle());
                    miniProgram.setPicMediaId(att.getMiniprogram().getPicMediaId());
                    miniProgram.setAppid(att.getMiniprogram().getAppid());
                    miniProgram.setPage(att.getMiniprogram().getPage());
                    attachment.setMiniProgram(miniProgram);
                }
                attachments.add(attachment);
            }
            task.setAttachments(attachments);
        }

        // Visible Range
        if (request.getVisibleRange() != null) {
            VisibleRange range = new VisibleRange();
            SenderList senderList = new SenderList();
            if (request.getVisibleRange().getSenderList() != null) {
                senderList.setUserList(request.getVisibleRange().getSenderList());
            }
            if (request.getVisibleRange().getDepartmentList() != null) {
                senderList.setDepartmentList(request.getVisibleRange().getDepartmentList().stream()
                        .map(String::valueOf).collect(Collectors.toList()));
            }
            range.setSenderList(senderList);
            
            if (request.getVisibleRange().getExternalContactList() != null) {
                ExternalContactList extList = new ExternalContactList();
                if (request.getVisibleRange().getExternalContactList().getTagList() != null) {
                    extList.setTagList(request.getVisibleRange().getExternalContactList().getTagList());
                }
                range.setExternalContactList(extList);
            }
            task.setVisibleRange(range);
        }

        try {
            WxCpAddMomentResult result = wxCpServiceManager.getWxCpService().getExternalContactService().addMomentTask(task);
            if (result != null && result.getJobId() != null) {
                moment.setJobid(result.getJobId());
                moment.setStatus(0); // Processing
            } else {
                moment.setStatus(2); // Failed
            }
        } catch (Exception e) {
            log.error("Failed to publish moment to WeCom: {}", moment.getId(), e);
            moment.setStatus(2);
        }
        momentRepository.save(moment);
    }

    @Transactional
    public void syncAllMomentStatuses() {
        List<WecomMoment> pendingMoments = momentRepository.findAll(); 
        for (WecomMoment moment : pendingMoments) {
            try {
                syncMomentStatus(moment);
            } catch (Exception e) {
                log.error("Failed to sync status for moment: {}", moment.getId(), e);
            }
        }
    }

    @Transactional
    public void syncMomentStatus(WecomMoment moment) throws Exception {
        if (moment.getMomentId() == null && moment.getJobid() != null) {
            // Check job result
            WxCpGetMomentTaskResult taskResult = wxCpServiceManager.getWxCpService().getExternalContactService().getMomentTaskResult(moment.getJobid());
            if (taskResult != null && taskResult.getStatus() != null && taskResult.getStatus() == 1 && taskResult.getResult() != null) {
                moment.setMomentId(taskResult.getResult().getMomentId());
                moment.setStatus(1); // Published
                momentRepository.save(moment);
            }
        }

        if (moment.getMomentId() != null) {
            // Check individual results
            syncMomentRecords(moment);
        }
    }

    private void syncMomentRecords(WecomMoment moment) throws Exception {
        String cursor = null;
        do {
            WxCpGetMomentTask result = wxCpServiceManager.getWxCpService().getExternalContactService().getMomentTask(moment.getMomentId(), cursor, null);
            if (result == null || result.getTaskList() == null) break;

            for (WxCpGetMomentTask.MomentTaskItem staff : result.getTaskList()) {
                Optional<WecomMomentRecord> recordOpt = momentRecordRepository.findByMomentIdAndUserid(moment.getId(), staff.getUserId());
                WecomMomentRecord record = recordOpt.orElse(new WecomMomentRecord());
                record.setMomentId(moment.getId());
                record.setUserid(staff.getUserId());
                if (staff.getPublishStatus() != null) {
                    try {
                        record.setPublishStatus(Integer.parseInt(staff.getPublishStatus()));
                    } catch (Exception e) {
                        log.warn("Invalid publish_status: {}", staff.getPublishStatus());
                    }
                }
                momentRecordRepository.save(record);
            }
            cursor = result.getNextCursor();
        } while (cursor != null && !cursor.isEmpty());
    }

    public Page<WecomMoment> listMoments(int page, int size) {
        return momentRepository.findAll(PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime")));
    }

    public Optional<WecomMoment> getMomentById(Long id) {
        return momentRepository.findById(id);
    }

    @Transactional
    public WecomMoment updateMomentTask(Long id, MomentDTO.CreateRequest request) throws Exception {
        WecomMoment moment = momentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        if (moment.getStatus() != 3 || moment.getSendTime() == null || moment.getSendTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("仅定时发送且未到发送时间的内容支持编辑");
        }

        moment.setTaskName(request.getTaskName());
        moment.setText(request.getText());
        moment.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        if (request.getVisibleRange() != null) {
            moment.setVisibleRangeType(1);
            moment.setVisibleRangeUsers(objectMapper.writeValueAsString(request.getVisibleRange()));
        } else {
            moment.setVisibleRangeType(0);
        }
        
        if (request.getSendType() != null && request.getSendType() == 1) {
            moment.setSendTime(LocalDateTime.parse(request.getSendTime(), DATE_TIME_FORMATTER));
        } else {
            // If changed to immediate send
            moment.setSendType(0);
            moment.setStatus(0);
            moment.setSendTime(null);
            moment = momentRepository.save(moment);
            self.publishMomentToWeCom(moment.getId());
            return moment;
        }

        return momentRepository.save(moment);
    }

    @Transactional
    public void deleteMomentTask(Long id) {
        WecomMoment moment = momentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        if (moment.getStatus() != 3 || moment.getSendTime() == null || moment.getSendTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("仅定时发送且未到发送时间的内容支持删除");
        }
        
        momentRecordRepository.deleteByMomentId(id);
        momentRepository.delete(moment);
    }

    public List<WecomMomentRecord> getRecords(Long momentId) {
        return momentRecordRepository.findByMomentId(momentId);
    }
}
