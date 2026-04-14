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
 * Handle change_external_chat events from WeCom.
 */
@Slf4j
@Component
public class GroupChatEventHandler implements WxCpMessageHandler {

    private final SyncService syncService;

    public GroupChatEventHandler(SyncService syncService) {
        this.syncService = syncService;
    }

    @Override
    public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage, Map<String, Object> context, WxCpService wxCpService, WxSessionManager sessionManager) throws WxErrorException {
        String changeType = wxMessage.getChangeType();
        String chatId = wxMessage.getChatId();

        log.info("Received change_external_chat event: changeType={}, chatId={}", changeType, chatId);

        if (chatId == null) {
            log.warn("chat_id is null in change_external_chat event, skipping.");
            return null;
        }

        switch (changeType) {
            case "create":
                log.info("Triggering initial sync for new group chat: {}", chatId);
                syncService.syncSingleGroupChat(chatId, 0); // 0 - Normal
                break;

            case "update":
                log.info("Triggering update sync for group chat: {}", chatId);
                syncService.syncSingleGroupChat(chatId, 0); // 0 - Normal
                break;

            case "dismiss":
                log.info("Handling dismissal for group chat: {}", chatId);
                syncService.dismissGroupChat(chatId);
                break;

            default:
                log.info("Unhandled changeType for change_external_chat: {}", changeType);
                break;
        }

        return null;
    }
}
