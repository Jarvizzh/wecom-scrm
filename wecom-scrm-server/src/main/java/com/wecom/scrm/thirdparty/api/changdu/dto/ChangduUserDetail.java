package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * DTO for Changdu User Detail.
 */
@Data
public class ChangduUserDetail {
    @JsonProperty("encrypted_device_id")
    private String encryptedDeviceId;
    
    @JsonProperty("device_brand")
    private String deviceBrand;
    
    @JsonProperty("media_source")
    private String mediaSource;
    
    @JsonProperty("book_source")
    private String bookSource;
    
    @JsonProperty("recharge_times")
    private Long rechargeTimes;
    
    @JsonProperty("recharge_amount")
    private Long rechargeAmount;
    
    @JsonProperty("balance_amount")
    private Long balanceAmount;
    
    @JsonProperty("register_time")
    private String registerTime;
    
    @JsonProperty("promotion_id")
    private String promotionId;
    
    @JsonProperty("promotion_name")
    private String promotionName;
    
    @JsonProperty("book_name")
    private String bookName;
    
    private String clickid;
    private String oaid;
    private String caid;
    private Long adid;
    private Long creativeid;
    private Integer creativetype;
    private String ip;
    
    @JsonProperty("user_agent")
    private String userAgent;
    
    private Long timestamp;
    
    @JsonProperty("optimizer_account")
    private String optimizerAccount;
    
    @JsonProperty("project_id")
    private String projectId;
    
    @JsonProperty("ad_id_v2")
    private String adIdV2;
    
    private List<String> mid;
    
    @JsonProperty("clue_token")
    private String clueToken;
    
    @JsonProperty("external_id")
    private String externalId;
}
