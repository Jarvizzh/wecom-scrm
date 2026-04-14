package com.wecom.scrm.config;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.api.impl.WxCpServiceImpl;
import me.chanjar.weixin.cp.config.impl.WxCpDefaultConfigImpl;
import me.chanjar.weixin.cp.message.WxCpMessageRouter;
import com.wecom.scrm.handler.WelcomeMsgHandler;
import com.wecom.scrm.handler.CustomerEventHandler;
import com.wecom.scrm.handler.GroupChatEventHandler;
import com.wecom.scrm.handler.CorpTagEventHandler;
import me.chanjar.weixin.cp.message.WxCpMessageRouterRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WxCpServiceManager {

    private final Map<String, WxCpService> wxCpServiceMap = new ConcurrentHashMap<>();
    private final Map<String, WxCpMessageRouter> wxCpMessageRouterMap = new ConcurrentHashMap<>();

    // ThreadLocal to hold current active corpId
    private static final ThreadLocal<String> currentCorpId = new ThreadLocal<>();

    private final WelcomeMsgHandler welcomeMsgHandler;
    private final CustomerEventHandler customerEventHandler;
    private final GroupChatEventHandler groupChatEventHandler;
    private final CorpTagEventHandler corpTagEventHandler;

    public WxCpServiceManager(@Autowired(required = false) @Lazy WelcomeMsgHandler welcomeMsgHandler,
            @Autowired(required = false) @Lazy CustomerEventHandler customerEventHandler,
            @Autowired(required = false) @Lazy GroupChatEventHandler groupChatEventHandler,
            @Autowired(required = false) @Lazy CorpTagEventHandler corpTagEventHandler) {
        this.welcomeMsgHandler = welcomeMsgHandler;
        this.customerEventHandler = customerEventHandler;
        this.groupChatEventHandler = groupChatEventHandler;
        this.corpTagEventHandler = corpTagEventHandler;
    }

    public static void setCurrentCorpId(String corpId) {
        currentCorpId.set(corpId);
    }

    public static String getCurrentCorpId() {
        return currentCorpId.get();
    }

    public static void clearCurrentCorpId() {
        currentCorpId.remove();
    }

    public WxCpService getWxCpService() {
        String corpId = getCurrentCorpId();
        return getWxCpService(corpId);
    }

    public WxCpService getWxCpService(String corpId) {
        if (corpId == null) {
            // return a default one or throw an error
            return wxCpServiceMap.values().stream().findFirst().orElse(null);
        }
        return wxCpServiceMap.get(corpId);
    }

    public WxCpMessageRouter getWxCpMessageRouter() {
        return wxCpMessageRouterMap.get(getCurrentCorpId());
    }

    public WxCpMessageRouter getWxCpMessageRouter(String corpId) {
        return wxCpMessageRouterMap.get(corpId);
    }

    public void addEnterprise(String corpId, Integer agentId, String secret, String token, String aesKey) {
        WxCpDefaultConfigImpl config = new WxCpDefaultConfigImpl();
        config.setCorpId(corpId);
        config.setAgentId(agentId);
        config.setCorpSecret(secret);
        config.setToken(token);
        config.setAesKey(aesKey);

        WxCpServiceImpl wxCpService = new WxCpServiceImpl();
        wxCpService.setWxCpConfigStorage(config);

        wxCpServiceMap.put(corpId, wxCpService);

        WxCpMessageRouter newRouter = new WxCpMessageRouter(wxCpService);

        // Register handlers for external contact changes
        WxCpMessageRouterRule externalContactRule = newRouter.rule()
                .async(false)
                .msgType("event")
                .event("change_external_contact");
        if (customerEventHandler != null) {
            externalContactRule.handler(customerEventHandler);
        }
        if (welcomeMsgHandler != null) {
            externalContactRule.handler(welcomeMsgHandler);
        }
        externalContactRule.end();

        // Register handlers for external chat changes
        if (groupChatEventHandler != null) {
            newRouter.rule().async(false)
                    .msgType("event")
                    .event("change_external_chat")
                    .handler(groupChatEventHandler)
                    .end();
        }

        // Register handlers for external tag changes
        if (corpTagEventHandler != null) {
            newRouter.rule().async(false)
                    .msgType("event")
                    .event("change_external_tag")
                    .handler(corpTagEventHandler)
                    .end();
        }

        log.info("Registered WxCpMessageRouter for corpId: {}", corpId);
        wxCpMessageRouterMap.put(corpId, newRouter);
    }

    public void removeEnterprise(String corpId) {
        wxCpServiceMap.remove(corpId);
        wxCpMessageRouterMap.remove(corpId);
        log.info("Removed WxCp service and router for corpId: {}", corpId);
    }
}
