package com.wecom.scrm.service;

import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.wecom.scrm.config.WxMpServiceManager;
import com.wecom.scrm.entity.WecomMpAccount;
import com.wecom.scrm.entity.WecomMpUser;
import com.wecom.scrm.entity.WecomSyncLog;
import com.wecom.scrm.repository.WecomMpAccountRepository;
import com.wecom.scrm.repository.WecomMpUserRepository;
import com.wecom.scrm.repository.WecomSyncLogRepository;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import me.chanjar.weixin.mp.bean.result.WxMpUserList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import com.wecom.scrm.dto.CopyMpAccountRequest;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class MpService {

    private final WecomMpAccountRepository mpAccountRepository;
    private final WecomMpUserRepository mpUserRepository;
    private final WxMpServiceManager wxMpServiceManager;
    private final WecomSyncLogRepository syncLogRepository;
    private final DynamicRoutingDataSource dynamicRoutingDataSource;
    private final MpService self;

    public MpService(WecomMpAccountRepository mpAccountRepository,
            WecomMpUserRepository mpUserRepository,
            WxMpServiceManager wxMpServiceManager,
            WecomSyncLogRepository syncLogRepository,
            DataSource dynamicRoutingDataSource,
            @Lazy MpService self) {
        this.mpAccountRepository = mpAccountRepository;
        this.mpUserRepository = mpUserRepository;
        this.wxMpServiceManager = wxMpServiceManager;
        this.syncLogRepository = syncLogRepository;
        this.dynamicRoutingDataSource = (DynamicRoutingDataSource) dynamicRoutingDataSource;
        this.self = self;
    }

    public List<WecomMpAccount> getAllAccounts() {
        return mpAccountRepository.findAll();
    }

    @Transactional
    public WecomMpAccount saveAccount(WecomMpAccount account) {
        WecomMpAccount saved = mpAccountRepository.save(account);
        // Ensure its service is initialized
        wxMpServiceManager.addAccount(saved.getAppId(), saved.getSecret());

        // Auto trigger sync
        self.syncUsers(saved.getAppId(), DynamicDataSourceContextHolder.peek());

        return saved;
    }

    @Transactional
    public void deleteAccount(Long id) {
        mpAccountRepository.findById(id).ifPresent(account -> {
            wxMpServiceManager.removeAccount(account.getAppId());
            mpAccountRepository.delete(account);
        });
    }

    public Page<WecomMpUser> getUsers(String mpAppId, String keyword, Pageable pageable) {
        String appId = (mpAppId != null && !mpAppId.isEmpty()) ? mpAppId : null;
        String kw = (keyword != null && !keyword.isEmpty()) ? keyword : null;
        return mpUserRepository.findByKeyword(appId, kw, pageable);
    }

    @Async("mpSyncExecutor")
    @Transactional
    public void syncUsers(String appId, String corpId) {
        try {
            if (corpId != null) {
                DynamicDataSourceContextHolder.push(corpId);
            }

            Optional<WecomMpAccount> accountOpt = mpAccountRepository.findByAppId(appId);
            if (!accountOpt.isPresent()) {
                log.error("Account not found for appId: {}", appId);
                return;
            }
            WecomMpAccount account = accountOpt.get();

            WecomSyncLog syncLog = new WecomSyncLog();
            syncLog.setSyncType("MP_USER_SYNC");
            syncLog.setStatus(0); // Running
            syncLog = syncLogRepository.save(syncLog);

            try {
                WxMpService wxMpService = wxMpServiceManager.addAccount(account.getAppId(), account.getSecret());

                log.info("Starting MP User sync for: {}", account.getName());

                String nextOpenid = null;
                do {
                    WxMpUserList userList = wxMpService.getUserService().userList(nextOpenid);
                    if (userList == null || userList.getOpenids() == null || userList.getOpenids().isEmpty()) {
                        break;
                    }

                    List<String> openids = userList.getOpenids();
                    // Batch fetch user info (max 100 per request)
                    int batchSize = 100;
                    for (int i = 0; i < openids.size(); i += batchSize) {
                        List<String> batch = openids.subList(i, Math.min(i + batchSize, openids.size()));
                        List<WxMpUser> userInfoList = wxMpService.getUserService().userInfoList(batch);

                        if (userInfoList != null) {
                            for (WxMpUser mpUser : userInfoList) {
                                saveOrUpdateMpUser(mpUser, account);
                            }
                        }
                    }

                    nextOpenid = userList.getNextOpenid();
                } while (nextOpenid != null && !nextOpenid.isEmpty());

                syncLog.setStatus(1); // Success
                syncLogRepository.save(syncLog);
                log.info("MP User sync completed for: {}", account.getName());

            } catch (Exception e) {
                log.error("Failed to sync MP users for appId: {}", appId, e);
                syncLog.setStatus(2); // Error
                syncLog.setErrorMsg(e.getMessage());
                syncLogRepository.save(syncLog);
            }
        } finally {
            if (corpId != null) {
                DynamicDataSourceContextHolder.poll();
            }
        }
    }

    public void copyToEnterprise(CopyMpAccountRequest request) {
        // 1. Get from current source (Current Context)
        WecomMpAccount source = mpAccountRepository.findByAppId(request.getAppId())
                .orElseThrow(
                        () -> new RuntimeException("Account not found in current enterprise: " + request.getAppId()));

        log.info("Copying MP account {} from current enterprise to {}", request.getAppId(), request.getTargetCorpId());

        // 2. Switch to target and execute save in a NEW transaction
        // 依然需要切换上下文，但核心目的是为了保护下面的 Async Transaction 正常运行
        try {
            // 关键：修改当前线程上下文，让 MdcTaskDecorator 捕获到它，带入异步线程
            DynamicDataSourceContextHolder.push(request.getTargetCorpId());

            // 直接调用（不需要 self 代理了，因为里面全是独立 JDBC 代码，不依赖 Spring 事务）
            saveInTargetEnterprise(source, request.getTargetCorpId());

            // Auto trigger sync in target enterprise (Async)
            self.syncUsers(request.getAppId(), request.getTargetCorpId());

            log.info("Successfully initiated copy and sync for MP account {} in enterprise {}", request.getAppId(),
                    request.getTargetCorpId());
        } finally {
            DynamicDataSourceContextHolder.poll();
        }
    }

    public void saveInTargetEnterprise(WecomMpAccount source, String targetCorpId) {
        log.info("Saving MP account to target enterprise {} via ISOLATED JDBC.", targetCorpId);

        // 1. Fetch raw target datasource directly to completely bypass OSIV / Spring Tx caching
        DataSource targetDs = dynamicRoutingDataSource.getDataSource(targetCorpId);
        if (targetDs == null) {
            throw new RuntimeException("Target enterprise datasource not found: " + targetCorpId);
        }

        // 2. Create ad-hoc JdbcTemplate for this target datasource
        JdbcTemplate adhocJdbcTemplate = new JdbcTemplate(targetDs);

        String sql = "INSERT INTO wecom_mp_account (name, app_id, secret, status, create_time, update_time) " +
                     "VALUES (?, ?, ?, ?, NOW(), NOW()) " +
                     "ON DUPLICATE KEY UPDATE name = VALUES(name), secret = VALUES(secret), status = VALUES(status), update_time = NOW()";
        
        adhocJdbcTemplate.update(sql, source.getName(), source.getAppId(), source.getSecret(), source.getStatus());
        
        log.info("Isolated JDBC save completed for appId: {}", source.getAppId());

        // Ensure the manager also knows about it in the target context
        wxMpServiceManager.addAccount(source.getAppId(), source.getSecret());
    }

    private void saveOrUpdateMpUser(WxMpUser mpUser, WecomMpAccount account) {
        WecomMpUser user = mpUserRepository.findByMpAppIdAndOpenid(account.getAppId(), mpUser.getOpenId())
                .orElse(new WecomMpUser());

        user.setOpenid(mpUser.getOpenId());
        user.setUnionid(mpUser.getUnionId());
        user.setNickname(mpUser.getNickname());
        user.setAvatarUrl(mpUser.getHeadImgUrl());
        user.setGender(null);
        user.setCountry(null);
        user.setProvince(null);
        user.setCity(null);
        user.setMpAppId(account.getAppId());
        user.setMpName(account.getName());
        user.setSubscribeTime(mpUser.getSubscribeTime() != null ? new Date(mpUser.getSubscribeTime() * 1000L) : null);

        mpUserRepository.save(user);
    }
}
