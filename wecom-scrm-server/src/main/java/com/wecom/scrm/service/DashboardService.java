package com.wecom.scrm.service;

import com.wecom.scrm.repository.*;
import com.wecom.scrm.repository.yuewen.YuewenRechargeRecordRepository;
import com.wecom.scrm.repository.changdu.ChangduRechargeRecordRepository;
import com.wecom.scrm.vo.DashboardVO;
import com.wecom.scrm.vo.RechargeStatVO;
import com.wecom.scrm.vo.ProductRechargeVO;
import com.wecom.scrm.dto.AddWayStatDTO;
import com.wecom.scrm.dto.TrendStatDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WecomCustomerRepository wecomCustomerRepository;
    private final WecomCustomerRelationRepository wecomCustomerRelationRepository;
    private final WecomUserRepository wecomUserRepository;
    private final GroupChatRepository groupChatRepository;
    private final WecomCustomerMessageRepository wecomCustomerMessageRepository;
    private final YuewenRechargeRecordRepository yuewenRechargeRecordRepository;
    private final ChangduRechargeRecordRepository changduRechargeRecordRepository;

    public DashboardVO getSummary() {
        DashboardVO vo = new DashboardVO();

        vo.setTotalCustomerCount(wecomCustomerRepository.count());
        vo.setTotalEmployeeCount(wecomUserRepository.count());
        vo.setTotalGroupChatCount(groupChatRepository.count());
        vo.setTotalMessageCount(wecomCustomerMessageRepository.count());

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        LocalDateTime sevenDaysAgo = todayStart.minusDays(6); // Include today, so go back 6 days

        vo.setTodayNewCustomerCount(wecomCustomerRelationRepository.countByCreateTimeAfter(todayStart));

        // Trend for last 7 days (Customers)
        List<TrendStatDTO> trendRaw = wecomCustomerRelationRepository.countTrendByCreateTimeAfter(sevenDaysAgo);
        vo.setCustomerGrowthTrend(trendRaw.stream().map(dto -> {
            Map<String, Object> map = new HashMap<>();
            map.put("date", dto.getDateString());
            map.put("count", dto.getCount());
            return map;
        }).collect(Collectors.toList()));

        // Add way distribution
        List<AddWayStatDTO> addWayRaw = wecomCustomerRelationRepository.countByAddWay();
        vo.setAddWayDistribution(addWayRaw.stream().map(dto -> {
            Map<String, Object> map = new HashMap<>();
            Integer way = dto.getAddWay();
            map.put("name", getAddWayName(way));
            map.put("value", dto.getCount());
            return map;
        }).collect(Collectors.toList()));

        // --- Yuewen Recharge ---
        RechargeStatVO yuewenRecharge = new RechargeStatVO();
        yuewenRecharge.setTotalAmount(yuewenRechargeRecordRepository.sumTotalAmount());
        yuewenRecharge.setTodayAmount(yuewenRechargeRecordRepository.sumAmountByTimeRange(todayStart, tomorrowStart));
        yuewenRecharge.setMonthAmount(yuewenRechargeRecordRepository.sumAmountByTimeRange(monthStart, nextMonthStart));

        List<Map<String, Object>> ywTodayProducts = yuewenRechargeRecordRepository.sumAmountByTimeRangeGroupByName(todayStart, tomorrowStart);
        List<Map<String, Object>> ywMonthProducts = yuewenRechargeRecordRepository.sumAmountByTimeRangeGroupByName(monthStart, nextMonthStart);

        Map<String, ProductRechargeVO> ywProductMap = new HashMap<>();
        mergeProductStats(ywProductMap, ywTodayProducts, true);
        mergeProductStats(ywProductMap, ywMonthProducts, false);
        yuewenRecharge.setProductStats(new ArrayList<>(ywProductMap.values()));
        vo.setYuewenRecharge(yuewenRecharge);

        // --- Changdu Recharge ---
        RechargeStatVO changduRecharge = new RechargeStatVO();
        changduRecharge.setTotalAmount(convertFenToYuan(changduRechargeRecordRepository.sumTotalPayFee()));
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        changduRecharge.setTodayAmount(convertFenToYuan(changduRechargeRecordRepository.sumPayFeeByTimeRange(
                todayStart.format(dtFormatter), tomorrowStart.format(dtFormatter))));
        changduRecharge.setMonthAmount(convertFenToYuan(changduRechargeRecordRepository.sumPayFeeByTimeRange(
                monthStart.format(dtFormatter), nextMonthStart.format(dtFormatter))));

        List<Map<String, Object>> cdTodayProducts = changduRechargeRecordRepository.sumPayFeeByTimeRangeGroupByName(
                todayStart.format(dtFormatter), tomorrowStart.format(dtFormatter));
        List<Map<String, Object>> cdMonthProducts = changduRechargeRecordRepository.sumPayFeeByTimeRangeGroupByName(
                monthStart.format(dtFormatter), nextMonthStart.format(dtFormatter));

        Map<String, ProductRechargeVO> cdProductMap = new HashMap<>();
        mergeProductStatsFen(cdProductMap, cdTodayProducts, true);
        mergeProductStatsFen(cdProductMap, cdMonthProducts, false);
        changduRecharge.setProductStats(new ArrayList<>(cdProductMap.values()));
        vo.setChangduRecharge(changduRecharge);

        // --- Recharge Trend (Last 7 Days) ---
        List<Map<String, Object>> ywTrend = yuewenRechargeRecordRepository.sumTrendByTimeAfter(sevenDaysAgo);
        List<Map<String, Object>> cdTrend = changduRechargeRecordRepository.sumTrendByTimeAfter(sevenDaysAgo.format(dtFormatter));

        Map<String, Map<String, Object>> trendMap = new TreeMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            String dateStr = sevenDaysAgo.plusDays(i).format(dateFormatter);
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", dateStr);
            dayMap.put("yuewen", BigDecimal.ZERO);
            dayMap.put("changdu", BigDecimal.ZERO);
            dayMap.put("total", BigDecimal.ZERO);
            trendMap.put(dateStr, dayMap);
        }

        for (Map<String, Object> t : ywTrend) {
            String date = t.get("date") != null ? t.get("date").toString() : "";
            if (trendMap.containsKey(date)) {
                BigDecimal amt = t.get("amount") != null ? new BigDecimal(t.get("amount").toString()) : BigDecimal.ZERO;
                trendMap.get(date).put("yuewen", amt);
                trendMap.get(date).put("total", ((BigDecimal) trendMap.get(date).get("total")).add(amt));
            }
        }

        for (Map<String, Object> t : cdTrend) {
            String date = t.get("date") != null ? t.get("date").toString() : "";
            if (trendMap.containsKey(date)) {
                BigDecimal amt = t.get("amount") != null ? convertFenToYuan(((Number) t.get("amount")).longValue()) : BigDecimal.ZERO;
                trendMap.get(date).put("changdu", amt);
                trendMap.get(date).put("total", ((BigDecimal) trendMap.get(date).get("total")).add(amt));
            }
        }

        vo.setRechargeTrend(new ArrayList<>(trendMap.values()));

        return vo;
    }

    private void mergeProductStats(Map<String, ProductRechargeVO> map, List<Map<String, Object>> data, boolean isToday) {
        for (Map<String, Object> row : data) {
            String name = row.get("name") != null ? row.get("name").toString() : "未知";
            BigDecimal amount = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
            Long userCount = row.get("userCount") != null ? Long.valueOf(row.get("userCount").toString()) : 0L;
            ProductRechargeVO pvo = map.computeIfAbsent(name, k -> {
                ProductRechargeVO vo = new ProductRechargeVO();
                vo.setProductName(k);
                vo.setTodayAmount(BigDecimal.ZERO);
                vo.setMonthAmount(BigDecimal.ZERO);
                vo.setTodayUserCount(0L);
                vo.setMonthUserCount(0L);
                return vo;
            });
            if (isToday) {
                pvo.setTodayAmount(amount);
                pvo.setTodayUserCount(userCount);
            } else {
                pvo.setMonthAmount(amount);
                pvo.setMonthUserCount(userCount);
            }
        }
    }

    private void mergeProductStatsFen(Map<String, ProductRechargeVO> map, List<Map<String, Object>> data, boolean isToday) {
        for (Map<String, Object> row : data) {
            String name = row.get("name") != null ? row.get("name").toString() : "未知";
            BigDecimal amount = row.get("amount") != null ? convertFenToYuan(((Number) row.get("amount")).longValue()) : BigDecimal.ZERO;
            Long userCount = row.get("userCount") != null ? Long.valueOf(row.get("userCount").toString()) : 0L;
            ProductRechargeVO pvo = map.computeIfAbsent(name, k -> {
                ProductRechargeVO vo = new ProductRechargeVO();
                vo.setProductName(k);
                vo.setTodayAmount(BigDecimal.ZERO);
                vo.setMonthAmount(BigDecimal.ZERO);
                vo.setTodayUserCount(0L);
                vo.setMonthUserCount(0L);
                return vo;
            });
            if (isToday) {
                pvo.setTodayAmount(amount);
                pvo.setTodayUserCount(userCount);
            } else {
                pvo.setMonthAmount(amount);
                pvo.setMonthUserCount(userCount);
            }
        }
    }

    private BigDecimal convertFenToYuan(Long fen) {
        if (fen == null) return BigDecimal.ZERO;
        return new BigDecimal(fen).divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    private String getAddWayName(Integer way) {
        if (way == null)
            return "未知";
        switch (way) {
            case 0: return "未知来源";
            case 1: return "扫描二维码";
            case 2: return "搜索手机号";
            case 3: return "名片分享";
            case 4: return "群聊";
            case 5: return "手机通讯录";
            case 6: return "微信联系人";
            case 8: return "安装第三方应用自动添加";
            case 9: return "搜索微信号";
            case 10: return "视频号添加";
            case 16: return "获客链接添加";
            case 201: return "内部成员共享";
            case 202: return "管理员/负责人分配";
            default: return "其他途径(" + way + ")";
        }
    }
}
