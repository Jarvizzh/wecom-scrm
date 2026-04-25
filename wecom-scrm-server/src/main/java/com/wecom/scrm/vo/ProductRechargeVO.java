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
    private Long todayUserCount;
    private Long monthUserCount;
    
    // Recent 7 days daily data: [{date: '2023-01-01', amount: 100, userCount: 10}, ...]
    private List<Map<String, Object>> dailyStats;
}
