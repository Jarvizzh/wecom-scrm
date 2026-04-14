package com.wecom.scrm.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DashboardVO {
    private long totalCustomerCount;
    private long todayNewCustomerCount;
    private long totalEmployeeCount;
    private long totalGroupChatCount;
    private long totalMessageCount;
    
    // Last 7 days trend: [{date: '2023-01-01', count: 10}, ...]
    private List<Map<String, Object>> customerGrowthTrend;
    
    // Add way distribution: [{name: '扫描二维码', value: 20}, ...]
    private List<Map<String, Object>> addWayDistribution;
}
