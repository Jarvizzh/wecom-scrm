package com.wecom.scrm.dto.changdu;

import com.wecom.scrm.entity.changdu.ChangduRechargeRecord;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class ChangduRechargeRecordDTO {
    private Long id;
    private Long distributorId;
    private Long tradeNo;
    private String outTradeNo;
    private String deviceId;
    private String openId;
    private String externalId;
    private String payWay;
    private Long payFee;
    private String status;
    private Long bookId;
    private String bookName;
    private Long promotionId;
    private String payTime;
    private String orderCreateTime;
    private Integer rechargeType;
    private Integer orderType;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // Additional User Info
    private String nickname;
    private String avatar;

    public static ChangduRechargeRecordDTO fromEntity(ChangduRechargeRecord entity) {
        ChangduRechargeRecordDTO dto = new ChangduRechargeRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
