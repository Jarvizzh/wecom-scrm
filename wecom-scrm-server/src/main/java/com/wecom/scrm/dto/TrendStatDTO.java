package com.wecom.scrm.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendStatDTO {
    private Object date;
    private Long count;

    public String getDateString() {
        return date != null ? date.toString() : "-";
    }
}
