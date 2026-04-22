package com.wecom.scrm.thirdparty.api.changdu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * DTO for Changdu Read Record.
 */
@Data
public class ChangduReadRecord {
    @JsonProperty("book_id")
    private String bookId;
    
    @JsonProperty("book_name")
    private String bookName;
    
    @JsonProperty("latest_chapter")
    private Long latestChapter;
    
    @JsonProperty("latest_chapter_name")
    private String latestChapterName;
}
