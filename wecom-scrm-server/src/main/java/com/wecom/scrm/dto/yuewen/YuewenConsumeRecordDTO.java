package com.wecom.scrm.dto.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenConsumeRecord;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
public class YuewenConsumeRecordDTO {
    private Long id;
    private String appFlag;
    private String openid;
    private Long guid;
    private String orderId;
    private String consumeId;
    private Integer worthAmount;
    private Integer freeAmount;
    private LocalDateTime consumeTime;
    private Long bookId;
    private String bookName;
    private String chapterId;
    private String chapterName;
    private String productName;

    public static YuewenConsumeRecordDTO fromEntity(YuewenConsumeRecord entity) {
        YuewenConsumeRecordDTO dto = new YuewenConsumeRecordDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
}
