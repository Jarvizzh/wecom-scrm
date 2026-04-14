package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_customer")
@Data
@NoArgsConstructor
public class WecomCustomer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_userid", unique = true, nullable = false, length = 64)
    private String externalUserid;

    @Column(nullable = false, length = 64)
    private String name;

    private String avatar;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer type;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer gender;

    @Column(length = 64)
    private String unionid;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
