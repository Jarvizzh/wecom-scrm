package com.wecom.scrm.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wecom.scrm.dto.WelcomeMessageAttachmentDTO;
import com.wecom.scrm.entity.WecomWelcomeMsg;
import com.wecom.scrm.repository.WecomWelcomeMsgRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.bean.external.WxCpWelcomeMsg;
import me.chanjar.weixin.cp.bean.external.msg.Attachment;
import me.chanjar.weixin.cp.bean.external.msg.Image;
import me.chanjar.weixin.cp.bean.external.msg.Link;
import me.chanjar.weixin.cp.bean.external.msg.MiniProgram;
import me.chanjar.weixin.cp.bean.external.msg.Text;
import me.chanjar.weixin.cp.message.WxCpMessageHandler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class WelcomeMsgHandler implements WxCpMessageHandler {

    private final Logger logger = LoggerFactory.getLogger(WelcomeMsgHandler.class);

    private final WecomWelcomeMsgRepository welcomeMsgRepository;
    private final ObjectMapper objectMapper;

    public WelcomeMsgHandler(WecomWelcomeMsgRepository welcomeMsgRepository, 
                             ObjectMapper objectMapper) {
        this.welcomeMsgRepository = welcomeMsgRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage, Map<String, Object> context, WxCpService wxCpService, WxSessionManager sessionManager) throws WxErrorException {
        String currentUserId = wxMessage.getUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            logger.warn("No UserID found in WeCom callback event. Cannot determine welcome message targeting.");
            return null;
        }

        // Fetch all welcome message configurations
        List<WecomWelcomeMsg> allConfigs = welcomeMsgRepository.findAll();
        if (allConfigs.isEmpty()) {
            logger.info("No welcome messages configured. Skipping.");
            return null;
        }

        WecomWelcomeMsg config = null;
        
        // Match by specific UserID
        for (WecomWelcomeMsg msg : allConfigs) {
            String userIdsJson = msg.getUserIds();
            if (userIdsJson != null && !userIdsJson.isEmpty()) {
                try {
                    List<String> targetUserIds = objectMapper.readValue(userIdsJson, new TypeReference<List<String>>() {});
                    if (targetUserIds.contains(currentUserId)) {
                        config = msg;
                        break;
                    }
                } catch (Exception e) {
                    logger.error("Failed to parse userIds JSON for message id: " + msg.getId(), e);
                }
            }
        }

        if (config == null) {
            logger.info("No welcome message matched for userid: {}. Skipping.", currentUserId);
            return null;
        }

        String welcomeCode = wxMessage.getWelcomeCode();
        if (welcomeCode == null || welcomeCode.isEmpty()) {
            logger.warn("Received event but no WelcomeCode found.");
            return null;
        }

        WxCpWelcomeMsg welcomeMsg = new WxCpWelcomeMsg();
        welcomeMsg.setWelcomeCode(welcomeCode);
        welcomeMsg.setAttachments(new ArrayList<>());

        if (config.getText() != null) {
            Text text = new Text();
            text.setContent(config.getText());
            welcomeMsg.setText(text);
        }

        try {
            if (config.getAttachments() != null && !config.getAttachments().isEmpty()) {
                // Attachments are stored as JSON in the database, parse them using DTO
                List<WelcomeMessageAttachmentDTO> attachments = objectMapper.readValue(config.getAttachments(),
                        new TypeReference<List<WelcomeMessageAttachmentDTO>>() {});

                for (WelcomeMessageAttachmentDTO att : attachments) {
                    Attachment attachment = new Attachment();
                    String msgtype = att.getMsgtype();

                    if ("image".equals(msgtype)) {
                        WelcomeMessageAttachmentDTO.Image image = att.getImage();
                        if (image != null && image.getMediaId() != null) {
                            Image img = new Image();
                            img.setMediaId(image.getMediaId());
                            img.setPicUrl(image.getPicUrl());
                            attachment.setImage(img);
                        }
                    } else if ("link".equals(msgtype)) {
                        WelcomeMessageAttachmentDTO.Link link = att.getLink();
                        if (link != null) {
                            Link lnk = new Link();
                            lnk.setTitle(link.getTitle());
                            lnk.setPicUrl(link.getPicUrl());
                            lnk.setDesc(link.getDesc());
                            lnk.setUrl(link.getUrl());
                            attachment.setLink(lnk);
                        }
                    } else if ("miniprogram".equals(msgtype)) {
                        WelcomeMessageAttachmentDTO.MiniProgram mp = att.getMiniprogram();
                        if (mp != null) {
                            MiniProgram miniProgram = new MiniProgram();
                            miniProgram.setTitle(mp.getTitle());
                            miniProgram.setPicMediaId(mp.getPicMediaId());
                            miniProgram.setAppid(mp.getAppid());
                            miniProgram.setPage(mp.getPage());
                            attachment.setMiniProgram(miniProgram);
                        }
                    }
                    if (attachment.getImage() != null || attachment.getLink() != null || attachment.getMiniProgram() != null) {
                        welcomeMsg.getAttachments().add(attachment);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse attachments for Welcome Message configuration id: " + config.getId(), e);
        }

        // Send the welcome message via WeCom API with retry logic and compliance handling
        int maxRetries = 3;
        int retryCount = 0;
        boolean sent = false;

        while (retryCount < maxRetries && !sent) {
            try {
                wxCpService.getExternalContactService().sendWelcomeMsg(welcomeMsg);
                sent = true;
                logger.info("Successfully sent welcome message for welcomeCode: {}", welcomeCode);
            } catch (WxErrorException e) {
                int errorCode = e.getError().getErrorCode();
                if (errorCode == 41051) {
                    // 41051: externaluser has started chatting
                    logger.info("Welcome message already sent successfully (41051) for welcomeCode: {}", welcomeCode);
                    sent = true;
                } else if (errorCode == 41096) {
                    // 41096: welcome msg is being distributed
                    retryCount++;
                    if (retryCount < maxRetries) {
                        logger.warn("Welcome message is being distributed (41096). Retrying {}/{} in 1s...", retryCount, maxRetries);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        logger.error("Failed to send welcome message after {} retries due to 41096 for welcomeCode: {}", maxRetries, welcomeCode);
                    }
                } else {
                    // Other WeCom errors
                    logger.error("Failed to send welcome message for welcomeCode: {}, error: {}", welcomeCode, e.getError());
                    break;
                }
            } catch (Exception e) {
                logger.error("Unexpected error sending welcome message for welcomeCode: " + welcomeCode, e);
                break;
            }
        }

        // WeCom callback response for success should be "success" string, but WxJava
        // router expects WxCpXmlOutMessage or null.
        // Returning null from handler sends empty success string to WeChat.
        return null;
    }

}
