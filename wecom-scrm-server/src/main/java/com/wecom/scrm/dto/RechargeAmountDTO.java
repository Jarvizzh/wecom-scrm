package com.wecom.scrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeAmountDTO {
    private String name;
    private BigDecimal amount;
}
