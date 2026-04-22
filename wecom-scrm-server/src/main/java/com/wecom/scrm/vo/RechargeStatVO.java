package com.wecom.scrm.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RechargeStatVO {
    private BigDecimal todayAmount;
    private BigDecimal monthAmount;
    private BigDecimal totalAmount;
    
    private List<ProductRechargeVO> productStats;
}
