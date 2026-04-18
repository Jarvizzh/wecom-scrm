package com.wecom.scrm.thirdparty.yuewen.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_yuewen_product")
public class YuewenProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    @Column(name = "app_flag", nullable = false, unique = true, length = 64)
    private String appFlag;

    @Column(name = "wx_app_id", length = 64)
    private String wxAppId;

    /**
     * status: 1: active, 0: inactive
     */
    @Column(name = "status")
    private Integer status = 1;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
