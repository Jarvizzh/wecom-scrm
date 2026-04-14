package com.wecom.scrm.handler;

import com.wecom.scrm.service.SyncService;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.cp.api.WxCpService;
import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;
import me.chanjar.weixin.cp.bean.message.WxCpXmlOutMessage;
import me.chanjar.weixin.cp.message.WxCpMessageHandler;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handle corporate tag changes (change_external_tag).
 */
@Slf4j
@Component
public class CorpTagEventHandler implements WxCpMessageHandler {

    private final SyncService syncService;

    public CorpTagEventHandler(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage, Map<String, Object> context, WxCpService wxCpService, WxSessionManager sessionManager) throws WxErrorException {
        String changeType = wxMessage.getChangeType();
        String tagType = wxMessage.getTagType();
        String id = wxMessage.getId();

        log.info("Received change_external_tag event: changeType={}, tagType={}, id={}", 
                changeType, tagType, id);

        // For any tag/group change (create, update, delete), we trigger a full sync to ensure consistency
        log.info("Triggering corporate tags synchronization due to callback event.");
        syncService.syncCorpTags();

        return null;
    }
}
