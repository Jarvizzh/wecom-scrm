package com.wecom.scrm.service.changdu;

import com.wecom.scrm.entity.changdu.ChangduProduct;
import com.wecom.scrm.entity.changdu.ChangduRechargeRecord;
import com.wecom.scrm.entity.changdu.ChangduUser;
import com.wecom.scrm.repository.changdu.ChangduProductRepository;
import com.wecom.scrm.repository.changdu.ChangduRechargeRecordRepository;
import com.wecom.scrm.repository.changdu.ChangduUserRepository;
import com.wecom.scrm.thirdparty.api.changdu.client.IChangduApiClient;
import com.wecom.scrm.thirdparty.api.changdu.config.ChangduConfig;
import com.wecom.scrm.thirdparty.api.changdu.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangduSyncService {

    private final IChangduApiClient apiClient;
    private final ChangduConfig config;
    private final ChangduProductRepository productRepository;
    private final ChangduUserRepository userRepository;
    private final ChangduRechargeRecordRepository rechargeRecordRepository;

    /**
     * Sync all products (distributors) under the main account.
     */
    public void syncProducts() {
        if (!StringUtils.hasText(config.getDistributorId())) {
            log.warn("Changdu distributorId not configured, skipping product sync.");
            return;
        }

        Long mainId = Long.valueOf(config.getDistributorId());
        log.info("Starting Changdu product sync for mainId: {}", mainId);

        ChangduPackageInfoRequest request = ChangduPackageInfoRequest.builder()
                .distributorId(mainId)
                .build();

        ChangduResponse<List<ChangduPackageInfo>> response = apiClient.getPackageInfo(request);
        if (response != null && response.isSuccess()) {
            List<ChangduPackageInfo> packages = response.getResult();
            if (packages != null) {
                for (ChangduPackageInfo info : packages) {
                    ChangduProduct product = productRepository.findByDistributorId(info.getDistributorId())
                            .orElse(new ChangduProduct());
                    product.setDistributorId(info.getDistributorId());
                    product.setProductName(info.getAppName());
                    product.setAppId(info.getAppId());
                    product.setAppType(info.getAppType());
                    productRepository.save(product);
                }
                log.info("Successfully synced {} Changdu products.", packages.size());
            }
        } else {
            log.error("Failed to sync Changdu products: {}", response != null ? response.getMessage() : "Empty response");
        }
    }

    /**
     * Sync users for a specific distributor.
     */
    public void syncUsers(Long distributorId, Long start, Long end) {
        log.info("Syncing Changdu users for distributorId: {}, from {} to {}", distributorId, start, end);
        int pageIndex = 0;
        int pageSize = 100;
        boolean hasMore = true;

        while (hasMore) {
            ChangduUserListRequest request = ChangduUserListRequest.builder()
                    .distributorId(distributorId)
                    .pageIndex((long) pageIndex)
                    .pageSize((long) pageSize)
                    .beginTime(start)
                    .endTime(end)
                    .build();

            ChangduResponse<List<ChangduUserDetail>> response = apiClient.getUserList(request);
            if (response != null && response.isSuccess()) {
                List<ChangduUserDetail> users = response.getData();
                if (users != null && !users.isEmpty()) {
                    saveOrUpdateUsers(users, distributorId);
                    pageIndex++;
                    if (users.size() < pageSize) {
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            } else {
                log.error("Failed to fetch Changdu users: {}", response != null ? response.getMessage() : "Empty response");
                hasMore = false;
            }
        }
    }

    private void saveOrUpdateUsers(List<ChangduUserDetail> items, Long distributorId) {
        for (ChangduUserDetail item : items) {
            ChangduUser user = userRepository.findByDistributorIdAndEncryptedDeviceId(distributorId, item.getEncryptedDeviceId())
                    .orElse(new ChangduUser());

            user.setDistributorId(distributorId);
            user.setEncryptedDeviceId(item.getEncryptedDeviceId());
            user.setDeviceBrand(item.getDeviceBrand());
            user.setMediaSource(item.getMediaSource());
            user.setBookSource(item.getBookSource());
            user.setRechargeTimes(item.getRechargeTimes());
            user.setRechargeAmount(item.getRechargeAmount());
            user.setBalanceAmount(item.getBalanceAmount());
            user.setRegisterTime(item.getRegisterTime());
            user.setPromotionId(item.getPromotionId());
            user.setPromotionName(item.getPromotionName());
            user.setBookName(item.getBookName());
            user.setExternalId(item.getExternalId());
            user.setOptimizerAccount(item.getOptimizerAccount());
            user.setProjectId(item.getProjectId());
            user.setAdIdV2(item.getAdIdV2());
            // Note: open_id is not in UserDetailOpen according to current formatting but added to DTO for safety
            
            userRepository.save(user);
        }
    }

    /**
     * Sync recharge records for a specific distributor.
     */
    public void syncRecharge(Long distributorId, Long start, Long end) {
        log.info("Syncing Changdu recharge records for distributorId: {}, from {} to {}", distributorId, start, end);
        int offset = 0;
        int limit = 50;
        boolean hasMore = true;

        while (hasMore) {
            ChangduRechargeRequest request = ChangduRechargeRequest.builder()
                    .distributorId(distributorId)
                    .begin(start)
                    .end(end)
                    .offset(offset)
                    .limit(limit)
                    .paid(true) // Only sync paid records
                    .build();

            ChangduResponse<List<ChangduRechargeEvent>> response = apiClient.getUserRecharge(request);
            if (response != null && response.isSuccess()) {
                List<ChangduRechargeEvent> events = response.getResult();
                if (events != null && !events.isEmpty()) {
                    saveOrUpdateRechargeRecords(events, distributorId);
                    offset += events.size();
                    
                    // Documentation says if has_more is true, continue. 
                    // Let's use both result size and hasMore flag if available.
                    Boolean apiHasMore = response.getHasMore();
                    if (apiHasMore != null) {
                        hasMore = apiHasMore;
                    } else {
                        hasMore = events.size() >= limit;
                    }
                    
                    // Safety break for deep paging as mentioned in docs
                    if (offset > 10000) {
                        log.warn("Deep paging threshold reached (>10000) for distributorId: {}", distributorId);
                        hasMore = false;
                    }
                } else {
                    hasMore = false;
                }
            } else {
                log.error("Failed to fetch Changdu recharge records: {}", response != null ? response.getMessage() : "Empty response");
                hasMore = false;
            }
        }
    }

    private void saveOrUpdateRechargeRecords(List<ChangduRechargeEvent> items, Long distributorId) {
        for (ChangduRechargeEvent item : items) {
            if (item.getTradeNo() == null) continue;

            ChangduRechargeRecord record = rechargeRecordRepository.findByTradeNo(item.getTradeNo())
                    .orElse(new ChangduRechargeRecord());

            record.setDistributorId(distributorId);
            record.setTradeNo(item.getTradeNo());
            record.setOutTradeNo(item.getOutTradeNo());
            record.setDeviceId(item.getDeviceId());
            record.setOpenId(item.getOpenId());
            record.setExternalId(item.getExternalId());
            record.setPayWay(item.getPayWay());
            record.setPayFee(item.getPayFee());
            record.setStatus(item.getStatus());
            record.setBookId(item.getBookId());
            record.setBookName(item.getBookName());
            record.setPromotionId(item.getPromotionId());
            record.setPayTime(item.getPayTime());
            record.setOrderCreateTime(item.getCreateTime());
            record.setRechargeType(item.getRechargeType());

            rechargeRecordRepository.save(record);
        }
    }

    @Async("thirdPartySyncExecutor")
    public void syncAllEnabledProducts(LocalDateTime start, LocalDateTime end) {
        log.info("Starting global Changdu sync from {} to {}", start, end);
        syncProducts(); // Refresh product list first

        List<ChangduProduct> products = productRepository.findAll();
        long startTs = start.toEpochSecond(ZoneOffset.ofHours(8));
        long endTs = end.toEpochSecond(ZoneOffset.ofHours(8));

        for (ChangduProduct product : products) {
            if (product.getStatus() == 1) {
                try {
                    syncUsers(product.getDistributorId(), startTs, endTs);
                    syncRecharge(product.getDistributorId(), startTs, endTs);
                } catch (Exception e) {
                    log.error("Error syncing data for Changdu product: {}", product.getProductName(), e);
                }
            }
        }
        log.info("Global Changdu sync finished.");
    }
}
