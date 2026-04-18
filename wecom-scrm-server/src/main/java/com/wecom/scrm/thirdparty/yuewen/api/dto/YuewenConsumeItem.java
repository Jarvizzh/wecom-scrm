package com.wecom.scrm.thirdparty.yuewen.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * Represents a single consumption record item in the Yuewen-4.4 API.
 */
@Data
public class YuewenConsumeItem {
    private String appflag;

    /**
     * User's openid.
     */
    private String openid;

    /**
     * Yuewen user ID.
     */
    private Long guid;

    /**
     * Order number.
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * Consumption order ID.
     */
    @JsonProperty("consume_id")
    private String consumeId;

    /**
     * Worth amount (unit: cent or as specified in docs).
     */
    @JsonProperty("worth_amount")
    private Integer worthAmount;

    /**
     * Free amount (unit: cent or as specified in docs).
     */
    @JsonProperty("free_amount")
    private Integer freeAmount;

    /**
     * Consumption time.
     */
    @JsonProperty("consume_time")
    private String consumeTime;

    /**
     * Book ID.
     */
    @JsonProperty("book_id")
    private Long bookId;

    /**
     * Book name.
     */
    @JsonProperty("book_name")
    private String bookName;

    /**
     * Chapter ID.
     */
    @JsonProperty("chapter_id")
    private String chapterId;

    /**
     * Chapter name.
     */
    @JsonProperty("chapter_name")
    private String chapterName;
}
