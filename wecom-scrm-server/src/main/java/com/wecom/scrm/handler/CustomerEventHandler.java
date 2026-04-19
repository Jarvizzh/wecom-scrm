package com.wecom.scrm.handler;

import com.wecom.scrm.entity.WecomCustomerRelation;
import com.wecom.scrm.repository.WecomCustomerRelationRepository;
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
import java.util.Optional;

@Slf4j
@Component
public class CustomerEventHandler implements WxCpMessageHandler {

    private final SyncService syncService;
    private final WecomCustomerRelationRepository relationRepository;

    public CustomerEventHandler(SyncService syncService, WecomCustomerRelationRepository relationRepository) {
        this.syncService = syncService;
        this.relationRepository = relationRepository;
    }

    @Override
    public WxCpXmlOutMessage handle(WxCpXmlMessage wxMessage, Map<String, Object> context, WxCpService wxCpService, WxSessionManager sessionManager) throws WxErrorException {
        String changeType = wxMessage.getChangeType();
        String externalUserid = wxMessage.getExternalUserId();
        String userid = wxMessage.getUserId();

        log.info("Received change_external_contact event: changeType={}, external_userid={}, userid={}", 
                changeType, externalUserid, userid);

        if (externalUserid == null) {
            log.warn("external_userid is null in change_external_contact event, skipping.");
            return null;
        }

        switch (changeType) {
            case "add_external_contact":
            case "edit_external_contact":  //TODO: 大量标签修改可能会打爆
            case "add_half_external_contact":
                log.info("Triggering sync for customer: {}", externalUserid);
                syncService.syncSingleCustomer(externalUserid);
                break;

            case "del_external_contact":
                log.info("Marking relation as deleted by staff: external_userid={}, userid={}", externalUserid, userid);
                markRelationAsLost(externalUserid, userid, 1);
                break;
            case "del_follow_user":
                log.info("Marking relation as lost (deleted by customer): external_userid={}, userid={}", externalUserid, userid);
                markRelationAsLost(externalUserid, userid, 2);
                break;
                
            default:
                log.info("Unhandled changeType: {}", changeType);
                break;
        }

        return null;
    }

    private void markRelationAsLost(String externalUserid, String userid, int status) {
        if (userid == null) {
            log.warn("userid is null, marking all relations for customer {} as lost/deleted with status {}", externalUserid, status);
            relationRepository.markStatusByExternalUseridIn(java.util.Collections.singletonList(externalUserid), status);
            return;
        }

        Optional<WecomCustomerRelation> relationOpt = relationRepository.findByUseridAndExternalUserid(userid, externalUserid);
        if (relationOpt.isPresent()) {
            WecomCustomerRelation relation = relationOpt.get();
            relation.setStatus(status);
            relationRepository.save(relation);
            log.info("Successfully marked relation as lost/deleted (status {}) for customer {} and user {}", status, externalUserid, userid);
        } else {
            log.warn("No relation found for customer {} and user {}", externalUserid, userid);
        }
    }
}
