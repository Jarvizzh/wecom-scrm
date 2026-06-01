package com.wecom.scrm.vo;

import lombok.Data;
import java.math.BigDecimal;

import java.util.List;
import java.util.Map;

@Data
public class ProductRechargeVO {
    private String productName;
    private BigDecimal todayAmount;
    private BigDecimal monthAmount;
    private BigDecimal lastMonthAmount;
    private Long todayUserCount;
    private Long monthUserCount;
    private Long lastMonthUserCount;
    
    // Recent 7 days daily data: [{date: '2023-01-01', amount: 100, userCount: 10}, ...]
    private List<Map<String, Object>> dailyStats;
    
    // Recent 6 months monthly data: [{month: '2023-01', amount: 1000, userCount: 50}, ...]
    private List<Map<String, Object>> monthlyStats;
}
