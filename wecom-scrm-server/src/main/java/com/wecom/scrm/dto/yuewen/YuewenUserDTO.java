package com.wecom.scrm.dto.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenUser;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class YuewenUserDTO {
    private Long id;
    private Long guid;
    private String openid;
    private String nickname;
    private String avatar;
    private String appFlag;
    private String wxAppId;
    private Long chargeAmount;
    private Integer chargeNum;
    private Integer isSubscribe;
    private LocalDateTime registTime;
    private String externalUserid;
    private LocalDateTime yuewenUpdateTime;
    private String productName;

    public static YuewenUserDTO fromEntity(YuewenUser entity) {
        YuewenUserDTO dto = new YuewenUserDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
