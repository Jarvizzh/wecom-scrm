package com.wecom.scrm.service.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenConsumeRecord;
import com.wecom.scrm.thirdparty.api.yuewen.client.IYuewenAPIClient;
import com.wecom.scrm.thirdparty.api.yuewen.dto.*;
import com.wecom.scrm.entity.yuewen.YuewenProduct;
import com.wecom.scrm.entity.yuewen.YuewenUser;
import com.wecom.scrm.repository.WecomCustomerRepository;
import com.wecom.scrm.repository.WecomMpUserRepository;
import com.wecom.scrm.repository.yuewen.YuewenConsumeRecordRepository;
import com.wecom.scrm.repository.yuewen.YuewenProductRepository;
import com.wecom.scrm.repository.yuewen.YuewenUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class YuewenSyncService {

    private final IYuewenAPIClient apiClient;
    private final YuewenUserRepository userRepository;
    private final YuewenProductRepository productRepository;
    private final WecomMpUserRepository mpUserRepository;
    private final WecomCustomerRepository customerRepository;
    private final YuewenConsumeRecordRepository consumeRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Async("thirdPartySyncExecutor")
    public void manualSync(String appFlag, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        if (Duration.between(start, end).toDays() > 7) {
            throw new RuntimeException("Sync interval cannot exceed 7 days");
        }

        log.info("Starting manual sync for appFlag: {}, from {} to {}", appFlag, start, end);
        syncUsers(appFlag, start.toEpochSecond(ZoneOffset.ofHours(8)), end.toEpochSecond(ZoneOffset.ofHours(8)));
    }

    public void syncUsers(String appFlag, Long startTimestamp, Long endTimestamp) {
        YuewenProduct product = productRepository.findByAppFlag(appFlag)
                .orElseThrow(() -> new RuntimeException("Product not found: " + appFlag));
        String wxAppId = product.getWxAppId();

        String nextId = null;
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            YuewenUserInfoRequest request = new YuewenUserInfoRequest();
            request.setAppflag(appFlag);
            request.setStartTime(startTimestamp);
            request.setEndTime(endTimestamp);
            request.setPage(page);
            request.setNextId(nextId);

            YuewenResponse<YuewenUserInfoResponse> response = apiClient.getUserInfo(request);
            if (response == null || response.getCode() != 0 || response.getData() == null) {
                log.error("Failed to fetch users from Yuewen: {}",
                        response != null ? response.getMsg() : "Empty response");
                break;
            }

            YuewenUserInfoResponse data = response.getData();
            List<YuewenUserItem> items = data.getList();
            if (items != null && !items.isEmpty()) {
                saveOrUpdateUsers(items, appFlag, wxAppId);
            }

            nextId = data.getNextId();
            if (StringUtils.hasText(nextId) && items != null && !items.isEmpty()) {
                page++;
            } else {
                hasMore = false;
            }
        }
        log.info("Sync completed for appFlag: {}， startTime:{} endTime:{}", appFlag, startTimestamp, endTimestamp);
    }

    @Async("thirdPartySyncExecutor")
    public void asyncSyncUsersForOneYear(String appFlag) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startLimit = now.minusDays(365);

        log.info("Starting async one-year sync (REVERSE) for appFlag: {} from {} back to {}", appFlag, now, startLimit);

        LocalDateTime currentEnd = now;
        while (currentEnd.isAfter(startLimit)) {
            LocalDateTime currentStart = currentEnd.minusDays(7);
            if (currentStart.isBefore(startLimit)) {
                currentStart = startLimit;
            }

            log.info("Syncing reverse batch range: {} to {}", currentStart, currentEnd);
            try {
                syncUsers(appFlag, currentStart.toEpochSecond(ZoneOffset.ofHours(8)),
                        currentEnd.toEpochSecond(ZoneOffset.ofHours(8)));
            } catch (Exception e) {
                log.error("Error during reverse batch sync for appFlag: {}, range: {} to {}", appFlag, currentStart,
                        currentEnd, e);
            }

            currentEnd = currentStart;
            // Small delay between batches
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
        log.info("Finished async one-year sync (REVERSE) for appFlag: {}", appFlag);
    }

    private void saveOrUpdateUsers(List<YuewenUserItem> items, String appFlag, String wxAppId) {
        for (YuewenUserItem item : items) {
            YuewenUser user = userRepository.findByAppFlagAndOpenid(appFlag, item.getOpenid())
                    .orElse(new YuewenUser());

            user.setAppFlag(appFlag);
            user.setWxAppId(wxAppId);
            user.setOpenid(item.getOpenid());
            user.setGuid(item.getGuid());
            user.setNickname(item.getNickname());
            user.setChargeAmount(item.getChargeAmount());
            user.setChargeNum(item.getChargeNum());
            user.setIsSubscribe(item.getIsSubscribe());

            if (StringUtils.hasText(item.getCreateTime())) {
                user.setRegistTime(LocalDateTime.parse(item.getCreateTime(), FORMATTER));
            }
            if (StringUtils.hasText(item.getVipEndTime())) {
                user.setVipEndTime(LocalDateTime.parse(item.getVipEndTime(), FORMATTER));
            }
            user.setChannelName(item.getChannelName());
            user.setBookName(item.getBookName());
            if (StringUtils.hasText(item.getUpdateTime())) {
                user.setYuewenUpdateTime(LocalDateTime.parse(item.getUpdateTime(), FORMATTER));
            }

            // Try to link with WecomCustomer via WecomMpUser
            if (StringUtils.hasText(wxAppId) && StringUtils.hasText(item.getOpenid())) {
                mpUserRepository.findByMpAppIdAndOpenid(wxAppId, item.getOpenid())
                        .ifPresent(mpUser -> {
                            String unionid = mpUser.getUnionid();
                            if (StringUtils.hasText(unionid)) {
                                customerRepository.findFirstByUnionid(unionid)
                                        .ifPresent(customer -> {
                                            user.setExternalUserid(customer.getExternalUserid());
                                            user.setNickname(customer.getName());
                                            user.setAvatar(customer.getAvatar());
                                        });
                            }
                        });

            }

            userRepository.save(user);
        }
    }

    @Async("thirdPartySyncExecutor")
    public void manualSyncConsume(String appFlag, LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new RuntimeException("Start time and end time are required");
        }
        log.info("Starting manual consume sync for appFlag: {}, from {} to {}", appFlag, start, end);

        LocalDateTime currentStart = start;
        while (currentStart.isBefore(end)) {
            LocalDateTime currentEnd = currentStart.plusDays(1);
            if (currentEnd.isAfter(end)) {
                currentEnd = end;
            }

            log.info("Syncing consume records for batch: {} to {}", currentStart, currentEnd);
            try {
                syncConsumeRecords(appFlag, currentStart.toEpochSecond(ZoneOffset.ofHours(8)),
                        currentEnd.toEpochSecond(ZoneOffset.ofHours(8)));
            } catch (Exception e) {
                log.error("Error syncing consume records for batch: {} to {}", currentStart, currentEnd, e);
            }

            currentStart = currentEnd;
            // Small delay to avoid rate limiting
            try {
                Thread.sleep(200);
            } catch (InterruptedException ignored) {
            }
        }
        log.info("Finished manual consume sync for appFlag: {}", appFlag);
    }

    public void syncConsumeRecords(String appFlag, Long startTimestamp, Long endTimestamp) {
        List<Long> guids = userRepository.findChargedGuidsByAppFlag(appFlag);
        log.info("Found {} users to sync consume records for appFlag: {}", guids.size(), appFlag);

        for (Long guid : guids) {
            try {
                syncConsumeRecordsByUser(appFlag, guid, startTimestamp, endTimestamp);
                // Small delay to avoid rate limiting
                try { Thread.sleep(200);} catch (InterruptedException ignored) {}
            } catch (Exception e) {
                log.error("Error syncing consume records for guid: {}", guid, e);
            }
        }
    }

    private void syncConsumeRecordsByUser(String appFlag, Long guid, Long startTimestamp, Long endTimestamp) {
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            YuewenConsumeLogRequest request = new YuewenConsumeLogRequest();
            request.setAppflag(appFlag);
            request.setStartTime(startTimestamp);
            request.setEndTime(endTimestamp);
            request.setPage(page);
            request.setGuid(guid);

            YuewenResponse<YuewenConsumeLogResponse> response = apiClient.getConsumeLog(request);
            if (response == null || response.getCode() != 0 || response.getData() == null) {
                log.error("Failed to fetch consume logs from Yuewen for guid {}: {}", guid,
                        response != null ? response.getMsg() : "Empty response");
                break;
            }

            YuewenConsumeLogResponse data = response.getData();
            List<YuewenConsumeItem> items = data.getList();
            if (items != null && !items.isEmpty()) {
                saveOrUpdateConsumeRecords(items, appFlag);
            }

            if (items != null && items.size() >= 10) { // Assuming 10 is page size
                page++;
            } else {
                hasMore = false;
            }
        }
    }

    private void saveOrUpdateConsumeRecords(List<YuewenConsumeItem> items, String appFlag) {
        for (YuewenConsumeItem item : items) {
            // Use orderId or consumeId + bookId + time as unique?
            // API example shows order_id. Let's use order_id if available.
            YuewenConsumeRecord record = null;
            if (StringUtils.hasText(item.getOrderId())) {
                record = consumeRepository.findByOrderId(item.getOrderId()).orElse(new YuewenConsumeRecord());
            } else {
                record = new YuewenConsumeRecord();
            }

            record.setAppFlag(appFlag);
            record.setOpenid(item.getOpenid());
            record.setGuid(item.getGuid());
            record.setOrderId(item.getOrderId());
            record.setConsumeId(item.getConsumeId());
            record.setWorthAmount(item.getWorthAmount());
            record.setFreeAmount(item.getFreeAmount());
            record.setBookId(item.getBookId());
            record.setBookName(item.getBookName());
            record.setChapterId(item.getChapterId());
            record.setChapterName(item.getChapterName());

            if (StringUtils.hasText(item.getConsumeTime())) {
                record.setConsumeTime(LocalDateTime.parse(item.getConsumeTime(), FORMATTER));
            }

            consumeRepository.save(record);
        }
    }
}
