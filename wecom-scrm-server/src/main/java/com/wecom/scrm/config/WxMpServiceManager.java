package com.wecom.scrm.config;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class WxMpServiceManager {

    private final Map<String, WxMpService> wxMpServiceMap = new ConcurrentHashMap<>();

    public WxMpService getWxMpService(String appId) {
        return wxMpServiceMap.get(appId);
    }

    public synchronized WxMpService addAccount(String appId, String secret) {
        if (wxMpServiceMap.containsKey(appId)) {
            // Check if secret changed? For simplicity, we re-init if called (unless we want to optimize)
            WxMpService existing = wxMpServiceMap.get(appId);
            if (existing.getWxMpConfigStorage().getAppId().equals(appId) && 
                existing.getWxMpConfigStorage().getSecret().equals(secret)) {
                return existing;
            }
        }

        WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
        config.setAppId(appId);
        config.setSecret(secret);

        WxMpService wxMpService = new WxMpServiceImpl();
        wxMpService.setWxMpConfigStorage(config);

        wxMpServiceMap.put(appId, wxMpService);
        log.info("Initialized WxMpService for appId: {}", appId);
        return wxMpService;
    }

    public void removeAccount(String appId) {
        wxMpServiceMap.remove(appId);
        log.info("Removed WxMpService for appId: {}", appId);
    }
}
