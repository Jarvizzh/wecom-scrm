package com.wecom.scrm.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import me.chanjar.weixin.cp.util.crypto.WxCpCryptUtil;
import com.wecom.scrm.config.WxCpServiceManager;
import com.wecom.scrm.service.WecomEventService;
import com.wecom.scrm.entity.WecomEventLog;
import org.springframework.web.bind.annotation.*;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;

@RestController
@RequestMapping("/api/wecom/callback/receive/{corpId}")
public class WecomCallbackController {

    private final Logger logger = LoggerFactory.getLogger(WecomCallbackController.class);

    private final WxCpServiceManager wxCpServiceManager;
    private final WecomEventService wecomEventService;

    public WecomCallbackController(WxCpServiceManager wxCpServiceManager, WecomEventService wecomEventService) {
        this.wxCpServiceManager = wxCpServiceManager;
        this.wecomEventService = wecomEventService;
    }

    @GetMapping
    public String verifyUrl(
            @PathVariable("corpId") String corpId,
            @RequestParam(name = "msg_signature", required = false) String signature,
            @RequestParam(name = "timestamp", required = false) String timestamp,
            @RequestParam(name = "nonce", required = false) String nonce,
            @RequestParam(name = "echostr", required = false) String echostr) {
        
        DynamicDataSourceContextHolder.push(corpId);
        WxCpServiceManager.setCurrentCorpId(corpId);
        WxCpService wxCpService = wxCpServiceManager.getWxCpService(corpId);

        logger.info("Received WeCom Callback URL Verification: signature={}, timestamp={}, nonce={}, echostr={}", 
                signature, timestamp, nonce, echostr);

        try {
            if (wxCpService != null && wxCpService.checkSignature(signature, timestamp, nonce, echostr)) {
                return wxCpService.getWxCpConfigStorage().getAesKey() != null ? 
                       new WxCpCryptUtil(wxCpService.getWxCpConfigStorage()).decrypt(echostr) : echostr;
            }
        } catch (Exception e) {
            logger.error("Error during verifyUrl", e);
        } finally {
            DynamicDataSourceContextHolder.poll();
            WxCpServiceManager.clearCurrentCorpId();
        }

        return "Illegal Request";
    }

    @PostMapping
    public String receiveMessage(
            @PathVariable("corpId") String corpId,
            @RequestBody String requestBody,
            @RequestParam("msg_signature") String signature,
            @RequestParam("timestamp") String timestamp,
            @RequestParam("nonce") String nonce) {

        DynamicDataSourceContextHolder.push(corpId);
        WxCpServiceManager.setCurrentCorpId(corpId);
        WxCpService wxCpService = wxCpServiceManager.getWxCpService(corpId);
        WxCpMessageRouter messageRouter = wxCpServiceManager.getWxCpMessageRouter(corpId);

        logger.debug("Received WeCom Callback Event: \n{}", requestBody);

        try {
            if (wxCpService != null && messageRouter != null) {
                // Decrypt message
                WxCpXmlMessage inMessage = WxCpXmlMessage.fromEncryptedXml(requestBody, wxCpService.getWxCpConfigStorage(), timestamp, nonce, signature);
                
                // 1. Save event to DB
                WecomEventLog eventLog = wecomEventService.saveEvent(corpId, inMessage);
                
                // 2. Process event (Synchronous attempt)
                wecomEventService.processEvent(eventLog);
            }
        } catch (Exception e) {
            logger.error("Failed to process WeCom Callback Event for corpId: {}", corpId, e);
        } finally {
            DynamicDataSourceContextHolder.poll();
            WxCpServiceManager.clearCurrentCorpId();
        }

        return "success";
    }
}
