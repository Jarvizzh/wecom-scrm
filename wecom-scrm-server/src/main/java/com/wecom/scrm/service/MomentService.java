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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MomentService(WxCpServiceManager wxCpServiceManager,
                         WecomMomentRepository momentRepository,
                         WecomMomentRecordRepository momentRecordRepository) {
        this.wxCpServiceManager = wxCpServiceManager;
        this.momentRepository = momentRepository;
        this.momentRecordRepository = momentRecordRepository;
    }

    @Transactional
    public WecomMoment createMomentTask(MomentDTO.CreateRequest request, String creatorUserid) throws Exception {
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
                // We use setters that automatically set msgType in WxJava
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

        // Call WeCom API
        WxCpAddMomentResult result = wxCpServiceManager.getWxCpService().getExternalContactService().addMomentTask(task);
        if (result == null || result.getJobId() == null) {
            throw new RuntimeException("Failed to create moment task: " + (result != null ? result.getErrmsg() : "Unknown error"));
        }
        String jobid = result.getJobId();
        log.info("Created moment task in WeCom, jobid: {}", jobid);

        // Save to database
        WecomMoment moment = new WecomMoment();
        moment.setJobid(jobid);
        moment.setText(request.getText());
        moment.setAttachments(objectMapper.writeValueAsString(request.getAttachments()));
        if (request.getVisibleRange() != null) {
            moment.setVisibleRangeType(1);
            moment.setVisibleRangeUsers(objectMapper.writeValueAsString(request.getVisibleRange()));
        } else {
            moment.setVisibleRangeType(0);
        }
        moment.setStatus(0); // Pending
        moment.setCreatorUserid(creatorUserid);
        
        return momentRepository.save(moment);
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

    public List<WecomMomentRecord> getRecords(Long momentId) {
        return momentRecordRepository.findByMomentId(momentId);
    }
}
