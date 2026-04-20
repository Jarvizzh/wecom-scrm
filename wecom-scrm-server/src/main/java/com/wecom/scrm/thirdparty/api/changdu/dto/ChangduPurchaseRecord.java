package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for Changdu User Purchase Action (Purchase Record).
 */
@Data
public class ChangduPurchaseRecord {
    @JsonProperty("device_id")
    private String deviceId;
    
    @JsonProperty("purchase_ts")
    private Long purchaseTs;
    
    @JsonProperty("coin_cost")
    private Long coinCost;
    
    @JsonProperty("ticket_cost")
    private Long ticketCost;
    
    @JsonProperty("book_id")
    private Long bookId;
    
    @JsonProperty("book_name")
    private String bookName;
    
    @JsonProperty("chapter_name")
    private String chapterName;
    
    @JsonProperty("open_id")
    private String openId;
}
