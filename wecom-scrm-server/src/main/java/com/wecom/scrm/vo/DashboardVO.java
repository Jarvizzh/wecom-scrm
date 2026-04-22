package com.wecom.scrm.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long totalCustomerCount;
    private long todayNewCustomerCount;
    private long totalEmployeeCount;
    private long totalGroupChatCount;
    private long totalMessageCount;
    
    private RechargeStatVO yuewenRecharge;
    private RechargeStatVO changduRecharge;
    
    // Last 7 days trend: [{date: '2023-01-01', count: 10}, ...]
    private List<Map<String, Object>> customerGrowthTrend;
    
    // Last 7 days recharge trend: [{date: '2023-01-01', yuewen: 100.5, changdu: 200.0, total: 300.5}, ...]
    private List<Map<String, Object>> rechargeTrend;
    
    // Add way distribution: [{name: '扫描二维码', value: 20}, ...]
    private List<Map<String, Object>> addWayDistribution;
}

