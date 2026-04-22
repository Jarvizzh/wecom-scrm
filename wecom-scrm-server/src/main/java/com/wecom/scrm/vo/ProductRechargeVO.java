package com.wecom.scrm.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductRechargeVO {
    private String productName;
    private BigDecimal todayAmount;
    private BigDecimal monthAmount;
    private Long todayUserCount;
    private Long monthUserCount;
}
