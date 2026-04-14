package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_customer_relation")
@Data
@NoArgsConstructor
public class WecomCustomerRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_userid", nullable = false, length = 64)
    private String externalUserid;

    @Column(nullable = false, length = 64)
    private String userid;

    private String remark;

    private String description;

    @Column(name = "add_way")
    private Integer addWay;

    @Column(length = 64)
    private String state;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status = 0; // 0=Normal, 1=Deleted, 2=Lost

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
