package com.wecom.scrm.dto.changdu;

import com.wecom.scrm.entity.changdu.ChangduUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class ChangduUserDTO {
    private Long id;
    private Long distributorId;
    private String encryptedDeviceId;
    private String deviceBrand;
    private String mediaSource;
    private String bookSource;
    private Long rechargeTimes;
    private Long rechargeAmount;
    private Long balanceAmount;
    private String registerTime;
    private String promotionId;
    private String promotionName;
    private String bookName;
    private String externalId;
    private String openId;
    private String nickname;
    private String avatar;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String employeeName;
    private Integer relationStatus;

    public static ChangduUserDTO fromEntity(ChangduUser entity) {
        ChangduUserDTO dto = new ChangduUserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
