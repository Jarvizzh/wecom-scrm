package com.wecom.scrm.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerRelationDTO {
    private String customerName;
    private String avatar;
    private Integer type;
    private String externalUserid;
    private String unionid;
    private String employeeName;
    private String employeeUserid;
    private Integer status; // 0=Normal, 1=Deleted
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime relationCreateTime;
    private String tagNames; // Comma separated tag names
    private List<String> tagIds; // List of tag IDs
    private String mpName;
    private String mpOpenid;

    public CustomerRelationDTO(String customerName, String avatar, Integer type, String unionid, String externalUserid, 
                               String employeeName, String employeeUserid, Integer status, LocalDateTime relationCreateTime) {
        this.customerName = customerName;
        this.avatar = avatar;
        this.type = type;
        this.unionid = unionid;
        this.externalUserid = externalUserid;
        this.employeeName = employeeName;
        this.employeeUserid = employeeUserid;
        this.status = status;
        this.relationCreateTime = relationCreateTime;
    }
}
