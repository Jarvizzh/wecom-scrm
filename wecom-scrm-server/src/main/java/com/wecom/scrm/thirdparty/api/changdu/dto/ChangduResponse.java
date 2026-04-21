package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.wecom.scrm.thirdparty.api.changdu.constant.ChangduErrorCodes;
import lombok.Data;

/**
 * Generic wrapper for Changdu API responses.
 *
 * @param <T> The type of the result/data.
 */
@Data
public class ChangduResponse<T> {
    private Integer code;
    private String message;
    private Long total;

    // Some endpoints use 'result', some use 'data'
    private T result;
    private T data;

    // Pagination info (Section 4)
    private Boolean hasMore;
    private Integer nextOffset;

    public boolean isSuccess() {
        return code != null && code == ChangduErrorCodes.SUCCESS.getCode();
    }

    /**
     * Get the message, if the message is empty or null, get it from ChangduErrorCodes.
     */
    public String getMessage() {
        String msgByCode = ChangduErrorCodes.getMsgByCode(code);
        return msgByCode != null ? msgByCode : message;
    }

    /**
     * Helper to get the actual list data regardless of whether it's in 'result' or 'data'.
     */
    public T getBusinessData() {
        return result != null ? result : data;
    }
}
