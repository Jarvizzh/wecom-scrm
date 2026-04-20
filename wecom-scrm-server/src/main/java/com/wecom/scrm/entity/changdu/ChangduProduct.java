package com.wecom.scrm.entity.changdu;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_changdu_product")
public class ChangduProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 128)
    private String productName;

    @Column(name = "distributor_id", nullable = false, unique = true)
    private Long distributorId;

    @Column(name = "app_id")
    private Integer appId;

    @Column(name = "app_type")
    private Integer appType; // 1-快应用；3-微信

    @Column(nullable = false)
    private Integer status = 1; // 1=启用, 0=禁用

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
