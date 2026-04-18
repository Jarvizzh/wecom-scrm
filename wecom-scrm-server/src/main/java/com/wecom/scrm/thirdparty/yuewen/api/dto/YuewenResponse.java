package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Generic wrapper for all Yuewen API responses.
 * 
 * @param <T> The type of the business data.
 */
@Data
public class YuewenResponse<T> {
    /**
     * Result code (0 for success).
     */
    private Integer code;

    /**
     * Result message.
     */
    @JsonProperty("message")
    private String msg;

    /**
     * Business data.
     */
    private T data;

    /**
     * Returns true if the business call was successful.
     */
    public boolean isSuccess() {
        return code != null && code == 0;
    }
}
