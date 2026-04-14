package com.wecom.scrm.dto;

import lombok.Data;

@Data
public class CopyMpAccountRequest {
    private String appId;
    private String targetCorpId;
}
