package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for Changdu Distributor Info (Package Info).
 */
@Data
public class ChangduPackageInfo {
    @JsonProperty("app_id")
    private Integer appId;
    
    @JsonProperty("app_name")
    private String appName;
    
    @JsonProperty("distributor_id")
    private Long distributorId;
    
    @JsonProperty("app_type")
    private Integer appType; // 1-快应用；3-微信
}
