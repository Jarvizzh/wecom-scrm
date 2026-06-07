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

        vo.setTotalCustomerCount(wecomCustomerRelationRepository.countDistinctExternalUseridByStatusNotDeleted());
        vo.setTotalEmployeeCount(wecomUserRepository.count());
        vo.setTotalGroupChatCount(groupChatRepository.count());
        vo.setTotalMessageCount(wecomCustomerMessageRepository.count());

        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime tomorrowStart = todayStart.plusDays(1);
        LocalDateTime monthStart = LocalDateTime.of(LocalDate.now().withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime nextMonthStart = monthStart.plusMonths(1);
        LocalDateTime lastMonthStart = LocalDateTime.of(LocalDate.now().minusMonths(1).withDayOfMonth(1), LocalTime.MIN);
        LocalDateTime lastMonthEnd = monthStart;
        LocalDateTime yearStart = LocalDateTime.of(LocalDate.now().withDayOfYear(1), LocalTime.MIN);
        LocalDateTime nextYearStart = yearStart.plusYears(1);
        LocalDateTime tenDaysAgo = todayStart.minusDays(9); // Include today, so go back 9 days

        vo.setTodayNewCustomerCount(wecomCustomerRelationRepository.countByCreateTimeAfter(todayStart));

        // Trend for last 10 days (Customers)
        List<TrendStatDTO> trendRaw = wecomCustomerRelationRepository.countTrendByCreateTimeAfter(tenDaysAgo);
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
        yuewenRecharge.setLastMonthAmount(yuewenRechargeRecordRepository.sumAmountByTimeRange(lastMonthStart, lastMonthEnd));
        yuewenRecharge.setYearAmount(yuewenRechargeRecordRepository.sumAmountByTimeRange(yearStart, nextYearStart));
        yuewenRecharge.setTodayAmount(yuewenRechargeRecordRepository.sumAmountByTimeRange(todayStart, tomorrowStart));
        yuewenRecharge.setMonthAmount(yuewenRechargeRecordRepository.sumAmountByTimeRange(monthStart, nextMonthStart));

        List<Map<String, Object>> ywTodayProducts = yuewenRechargeRecordRepository.sumAmountByTimeRangeGroupByName(todayStart, tomorrowStart);
        List<Map<String, Object>> ywMonthProducts = yuewenRechargeRecordRepository.sumAmountByTimeRangeGroupByName(monthStart, nextMonthStart);
        List<Map<String, Object>> ywLastMonthProducts = yuewenRechargeRecordRepository.sumAmountByTimeRangeGroupByName(lastMonthStart, lastMonthEnd);
        List<Map<String, Object>> ywDailyProducts = yuewenRechargeRecordRepository.sumAmountByDateAndName(tenDaysAgo);

        Map<String, ProductRechargeVO> ywProductMap = new HashMap<>();
        mergeProductStats(ywProductMap, ywTodayProducts, "today", false);
        mergeProductStats(ywProductMap, ywMonthProducts, "month", false);
        mergeProductStats(ywProductMap, ywLastMonthProducts, "lastMonth", false);
        mergeProductDailyStats(ywProductMap, ywDailyProducts, tenDaysAgo, false);
        yuewenRecharge.setProductStats(new ArrayList<>(ywProductMap.values()));
        vo.setYuewenRecharge(yuewenRecharge);

        // --- Changdu Recharge ---
        RechargeStatVO changduRecharge = new RechargeStatVO();
        DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        changduRecharge.setLastMonthAmount(convertFenToYuan(changduRechargeRecordRepository.sumPayFeeByTimeRange(
                lastMonthStart.format(dtFormatter), lastMonthEnd.format(dtFormatter))));
        changduRecharge.setYearAmount(convertFenToYuan(changduRechargeRecordRepository.sumPayFeeByTimeRange(
                yearStart.format(dtFormatter), nextYearStart.format(dtFormatter))));
        changduRecharge.setTodayAmount(convertFenToYuan(changduRechargeRecordRepository.sumPayFeeByTimeRange(
                todayStart.format(dtFormatter), tomorrowStart.format(dtFormatter))));
        changduRecharge.setMonthAmount(convertFenToYuan(changduRechargeRecordRepository.sumPayFeeByTimeRange(
                monthStart.format(dtFormatter), nextMonthStart.format(dtFormatter))));

        List<Map<String, Object>> cdTodayProducts = changduRechargeRecordRepository.sumPayFeeByTimeRangeGroupByName(
                todayStart.format(dtFormatter), tomorrowStart.format(dtFormatter));
        List<Map<String, Object>> cdMonthProducts = changduRechargeRecordRepository.sumPayFeeByTimeRangeGroupByName(
                monthStart.format(dtFormatter), nextMonthStart.format(dtFormatter));
        List<Map<String, Object>> cdLastMonthProducts = changduRechargeRecordRepository.sumPayFeeByTimeRangeGroupByName(
                lastMonthStart.format(dtFormatter), lastMonthEnd.format(dtFormatter));
        List<Map<String, Object>> cdDailyProducts = changduRechargeRecordRepository.sumPayFeeByDateAndName(tenDaysAgo.format(dtFormatter));

        Map<String, ProductRechargeVO> cdProductMap = new HashMap<>();
        mergeProductStats(cdProductMap, cdTodayProducts, "today", true);
        mergeProductStats(cdProductMap, cdMonthProducts, "month", true);
        mergeProductStats(cdProductMap, cdLastMonthProducts, "lastMonth", true);
        mergeProductDailyStats(cdProductMap, cdDailyProducts, tenDaysAgo, true);
        changduRecharge.setProductStats(new ArrayList<>(cdProductMap.values()));
        vo.setChangduRecharge(changduRecharge);

        // --- Recharge Trend (Last 10 Days) ---
        List<Map<String, Object>> ywTrend = new ArrayList<>();
        List<Map<String, Object>> cdTrend = new ArrayList<>();
        
        // Aggregate trend from daily products
        Map<String, BigDecimal> ywDateSum = new HashMap<>();
        Map<String, BigDecimal> cdDateSum = new HashMap<>();
        
        for (Map<String, Object> row : ywDailyProducts) {
            String date = row.get("date") != null ? row.get("date").toString() : "";
            BigDecimal amt = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
            ywDateSum.put(date, ywDateSum.getOrDefault(date, BigDecimal.ZERO).add(amt));
        }
        for (Map<String, Object> row : cdDailyProducts) {
            String date = row.get("date") != null ? row.get("date").toString() : "";
            BigDecimal amt = row.get("amount") != null ? convertFenToYuan(((Number) row.get("amount")).longValue()) : BigDecimal.ZERO;
            cdDateSum.put(date, cdDateSum.getOrDefault(date, BigDecimal.ZERO).add(amt));
        }

        Map<String, Map<String, Object>> trendMap = new TreeMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 10; i++) {
            String dateStr = tenDaysAgo.plusDays(i).format(dateFormatter);
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("date", dateStr);
            BigDecimal ywAmt = ywDateSum.getOrDefault(dateStr, BigDecimal.ZERO);
            BigDecimal cdAmt = cdDateSum.getOrDefault(dateStr, BigDecimal.ZERO);
            dayMap.put("yuewen", ywAmt);
            dayMap.put("changdu", cdAmt);
            dayMap.put("total", ywAmt.add(cdAmt));
            trendMap.put(dateStr, dayMap);
        }

        vo.setRechargeTrend(new ArrayList<>(trendMap.values()));

        // --- Recharge Half Year Trend & Product Stats (Last 6 Months) ---
        LocalDateTime halfYearStart = LocalDateTime.of(LocalDate.now().minusMonths(5).withDayOfMonth(1), LocalTime.MIN);
        List<String> last6Months = new ArrayList<>();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = 5; i >= 0; i--) {
            last6Months.add(LocalDate.now().minusMonths(i).format(monthFormatter));
        }

        // Fetch Yuewen and Changdu 6-month daily raw data
        List<Map<String, Object>> ywHalfYearDaily = yuewenRechargeRecordRepository.sumAmountByDateAndName(halfYearStart);
        List<Map<String, Object>> cdHalfYearDaily = changduRechargeRecordRepository.sumPayFeeByDateAndName(halfYearStart.format(dtFormatter));

        // 1. Yuewen Product Half-Year Stats
        RechargeStatVO yuewenHalfYearRecharge = new RechargeStatVO();
        Map<String, ProductRechargeVO> ywHalfYearProductMap = new HashMap<>();
        mergeProductStats(ywHalfYearProductMap, ywTodayProducts, "today", false);
        mergeProductStats(ywHalfYearProductMap, ywMonthProducts, "month", false);
        mergeProductStats(ywHalfYearProductMap, ywLastMonthProducts, "lastMonth", false);
        mergeProductMonthlyStats(ywHalfYearProductMap, ywHalfYearDaily, last6Months, false);
        yuewenHalfYearRecharge.setProductStats(new ArrayList<>(ywHalfYearProductMap.values()));
        vo.setYuewenHalfYearRecharge(yuewenHalfYearRecharge);

        // 2. Changdu Product Half-Year Stats
        RechargeStatVO changduHalfYearRecharge = new RechargeStatVO();
        Map<String, ProductRechargeVO> cdHalfYearProductMap = new HashMap<>();
        mergeProductStats(cdHalfYearProductMap, cdTodayProducts, "today", true);
        mergeProductStats(cdHalfYearProductMap, cdMonthProducts, "month", true);
        mergeProductStats(cdHalfYearProductMap, cdLastMonthProducts, "lastMonth", true);
        mergeProductMonthlyStats(cdHalfYearProductMap, cdHalfYearDaily, last6Months, true);
        changduHalfYearRecharge.setProductStats(new ArrayList<>(cdHalfYearProductMap.values()));
        vo.setChangduHalfYearRecharge(changduHalfYearRecharge);

        // 3. Half Year Monthly Trend Trend
        Map<String, BigDecimal> ywMonthSum = new HashMap<>();
        Map<String, BigDecimal> cdMonthSum = new HashMap<>();

        for (Map<String, Object> row : ywHalfYearDaily) {
            String date = row.get("date") != null ? row.get("date").toString() : "";
            if (date.length() < 7) continue;
            String month = date.substring(0, 7);
            BigDecimal amt = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
            ywMonthSum.put(month, ywMonthSum.getOrDefault(month, BigDecimal.ZERO).add(amt));
        }
        for (Map<String, Object> row : cdHalfYearDaily) {
            String date = row.get("date") != null ? row.get("date").toString() : "";
            if (date.length() < 7) continue;
            String month = date.substring(0, 7);
            BigDecimal amt = row.get("amount") != null ? convertFenToYuan(((Number) row.get("amount")).longValue()) : BigDecimal.ZERO;
            cdMonthSum.put(month, cdMonthSum.getOrDefault(month, BigDecimal.ZERO).add(amt));
        }

        Map<String, Map<String, Object>> halfYearTrendMap = new TreeMap<>();
        for (String monthStr : last6Months) {
            Map<String, Object> dayMap = new HashMap<>();
            dayMap.put("month", monthStr);
            BigDecimal ywAmt = ywMonthSum.getOrDefault(monthStr, BigDecimal.ZERO);
            BigDecimal cdAmt = cdMonthSum.getOrDefault(monthStr, BigDecimal.ZERO);
            dayMap.put("yuewen", ywAmt);
            dayMap.put("changdu", cdAmt);
            dayMap.put("total", ywAmt.add(cdAmt));
            halfYearTrendMap.put(monthStr, dayMap);
        }
        vo.setRechargeHalfYearTrend(new ArrayList<>(halfYearTrendMap.values()));

        return vo;
    }

    private void mergeProductStats(Map<String, ProductRechargeVO> map, List<Map<String, Object>> data, String type, boolean isFen) {
        for (Map<String, Object> row : data) {
            String name = row.get("name") != null ? row.get("name").toString() : "未知";
            BigDecimal amount = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
            if (isFen) {
                amount = convertFenToYuan(((Number) row.get("amount")).longValue());
            }
            Long userCount = row.get("userCount") != null ? Long.valueOf(row.get("userCount").toString()) : 0L;

            ProductRechargeVO pvo = map.computeIfAbsent(name, k -> {
                ProductRechargeVO vo = new ProductRechargeVO();
                vo.setProductName(k);
                vo.setTodayAmount(BigDecimal.ZERO);
                vo.setMonthAmount(BigDecimal.ZERO);
                vo.setLastMonthAmount(BigDecimal.ZERO);
                vo.setTodayUserCount(0L);
                vo.setMonthUserCount(0L);
                vo.setLastMonthUserCount(0L);
                vo.setDailyStats(new ArrayList<>());
                return vo;
            });

            if ("today".equals(type)) {
                pvo.setTodayAmount(amount);
                pvo.setTodayUserCount(userCount);
            } else if ("month".equals(type)) {
                pvo.setMonthAmount(amount);
                pvo.setMonthUserCount(userCount);
            } else if ("lastMonth".equals(type)) {
                pvo.setLastMonthAmount(amount);
                pvo.setLastMonthUserCount(userCount);
            }
        }
    }

    private void mergeProductDailyStats(Map<String, ProductRechargeVO> map, List<Map<String, Object>> data, LocalDateTime startTime, boolean isFen) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Map<String, Map<String, Object>>> productDateStats = new HashMap<>();

        for (Map<String, Object> row : data) {
            String name = row.get("name") != null ? row.get("name").toString() : "未知";
            String date = row.get("date") != null ? row.get("date").toString() : "";
            BigDecimal amount = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
            if (isFen) {
                amount = convertFenToYuan(((Number) row.get("amount")).longValue());
            }
            Long userCount = row.get("userCount") != null ? Long.valueOf(row.get("userCount").toString()) : 0L;

            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("amount", amount);
            statsMap.put("userCount", userCount);
            productDateStats.computeIfAbsent(name, k -> new HashMap<>()).put(date, statsMap);
        }

        // Ensure all products in the map (even those from today/month stats) have 10 days daily stats
        for (String name : map.keySet()) {
            ProductRechargeVO pvo = map.get(name);
            List<Map<String, Object>> dailyList = new ArrayList<>();
            Map<String, Map<String, Object>> dateStats = productDateStats.getOrDefault(name, Collections.emptyMap());

            for (int i = 0; i < 10; i++) {
                String dateStr = startTime.plusDays(i).format(dateFormatter);
                Map<String, Object> dayData = new HashMap<>();
                dayData.put("date", dateStr);
                if (dateStats.containsKey(dateStr)) {
                    dayData.put("amount", dateStats.get(dateStr).get("amount"));
                    dayData.put("userCount", dateStats.get(dateStr).get("userCount"));
                } else {
                    dayData.put("amount", BigDecimal.ZERO);
                    dayData.put("userCount", 0L);
                }
                dailyList.add(dayData);
            }
            pvo.setDailyStats(dailyList);
        }

        // Also add products that only had activity in the last 10 days (not today/month)
        for (String name : productDateStats.keySet()) {
            if (!map.containsKey(name)) {
                ProductRechargeVO pvo = new ProductRechargeVO();
                pvo.setProductName(name);
                pvo.setTodayAmount(BigDecimal.ZERO);
                pvo.setMonthAmount(BigDecimal.ZERO);
                pvo.setLastMonthAmount(BigDecimal.ZERO);
                pvo.setTodayUserCount(0L);
                pvo.setMonthUserCount(0L);
                pvo.setLastMonthUserCount(0L);
                
                List<Map<String, Object>> dailyList = new ArrayList<>();
                Map<String, Map<String, Object>> dateStats = productDateStats.get(name);
                for (int i = 0; i < 10; i++) {
                    String dateStr = startTime.plusDays(i).format(dateFormatter);
                    Map<String, Object> dayData = new HashMap<>();
                    dayData.put("date", dateStr);
                    if (dateStats.containsKey(dateStr)) {
                        dayData.put("amount", dateStats.get(dateStr).get("amount"));
                        dayData.put("userCount", dateStats.get(dateStr).get("userCount"));
                    } else {
                        dayData.put("amount", BigDecimal.ZERO);
                        dayData.put("userCount", 0L);
                    }
                    dailyList.add(dayData);
                }
                pvo.setDailyStats(dailyList);
                map.put(name, pvo);
            }
        }
    }

    private void mergeProductMonthlyStats(Map<String, ProductRechargeVO> map, List<Map<String, Object>> dailyData, List<String> months, boolean isFen) {
        // Map of Product -> Month -> Stat (Amount, UserCount)
        Map<String, Map<String, Map<String, Object>>> productMonthStats = new HashMap<>();

        for (Map<String, Object> row : dailyData) {
            String name = row.get("name") != null ? row.get("name").toString() : "未知";
            String date = row.get("date") != null ? row.get("date").toString() : "";
            if (date.length() < 7) continue;
            String month = date.substring(0, 7); // yyyy-MM
            
            BigDecimal amount = row.get("amount") != null ? new BigDecimal(row.get("amount").toString()) : BigDecimal.ZERO;
            if (isFen) {
                amount = convertFenToYuan(((Number) row.get("amount")).longValue());
            }
            Long userCount = row.get("userCount") != null ? Long.valueOf(row.get("userCount").toString()) : 0L;

            Map<String, Map<String, Object>> monthMap = productMonthStats.computeIfAbsent(name, k -> new HashMap<>());
            Map<String, Object> stat = monthMap.computeIfAbsent(month, k -> {
                Map<String, Object> s = new HashMap<>();
                s.put("amount", BigDecimal.ZERO);
                s.put("userCount", 0L);
                return s;
            });
            
            stat.put("amount", ((BigDecimal) stat.get("amount")).add(amount));
            stat.put("userCount", ((Long) stat.get("userCount")) + userCount);
        }

        // Populating the map
        for (String name : map.keySet()) {
            ProductRechargeVO pvo = map.get(name);
            List<Map<String, Object>> monthlyList = new ArrayList<>();
            Map<String, Map<String, Object>> monthStats = productMonthStats.getOrDefault(name, Collections.emptyMap());

            for (String monthStr : months) {
                Map<String, Object> monthData = new HashMap<>();
                monthData.put("month", monthStr);
                if (monthStats.containsKey(monthStr)) {
                    monthData.put("amount", monthStats.get(monthStr).get("amount"));
                    monthData.put("userCount", monthStats.get(monthStr).get("userCount"));
                } else {
                    monthData.put("amount", BigDecimal.ZERO);
                    monthData.put("userCount", 0L);
                }
                monthlyList.add(monthData);
            }
            pvo.setMonthlyStats(monthlyList);
        }

        // Add products that only had activity in the monthly stats (not today/month)
        for (String name : productMonthStats.keySet()) {
            if (!map.containsKey(name)) {
                ProductRechargeVO pvo = new ProductRechargeVO();
                pvo.setProductName(name);
                pvo.setTodayAmount(BigDecimal.ZERO);
                pvo.setMonthAmount(BigDecimal.ZERO);
                pvo.setLastMonthAmount(BigDecimal.ZERO);
                pvo.setTodayUserCount(0L);
                pvo.setMonthUserCount(0L);
                pvo.setLastMonthUserCount(0L);
                pvo.setDailyStats(new ArrayList<>());
                
                List<Map<String, Object>> monthlyList = new ArrayList<>();
                Map<String, Map<String, Object>> monthStats = productMonthStats.get(name);
                for (String monthStr : months) {
                    Map<String, Object> monthData = new HashMap<>();
                    monthData.put("month", monthStr);
                    if (monthStats.containsKey(monthStr)) {
                        monthData.put("amount", monthStats.get(monthStr).get("amount"));
                        monthData.put("userCount", monthStats.get(monthStr).get("userCount"));
                    } else {
                        monthData.put("amount", BigDecimal.ZERO);
                        monthData.put("userCount", 0L);
                    }
                    monthlyList.add(monthData);
                }
                pvo.setMonthlyStats(monthlyList);
                map.put(name, pvo);
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
