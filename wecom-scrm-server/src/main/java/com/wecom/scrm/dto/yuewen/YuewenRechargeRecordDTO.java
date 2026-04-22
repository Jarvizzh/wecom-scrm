package com.wecom.scrm.dto.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenRechargeRecord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class YuewenRechargeRecordDTO {
    private Long id;
    private String appFlag;
    private String appName;
    private String amount;
    private String ywOrderId;
    private String orderId;
    private LocalDateTime orderTime;
    private LocalDateTime payTime;
    private Integer orderStatus;
    private Integer orderType;
    private String openid;
    private Long guid;
    private String nickname;
    private String avatar;
    private Integer sex;
    private Long channelId;
    private String channelName;
    private Long bookId;
    private String bookName;
    private String wxAppId;
    private String itemName;
    private Integer orderChannel;

    public static YuewenRechargeRecordDTO fromEntity(YuewenRechargeRecord entity) {
        YuewenRechargeRecordDTO dto = new YuewenRechargeRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
