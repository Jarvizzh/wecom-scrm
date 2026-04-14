package com.wecom.scrm.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "wecom_mp_account")
public class WecomMpAccount implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(name = "app_id", nullable = false, unique = true, length = 64)
    private String appId;

    @Column(nullable = false, length = 128)
    private String secret;

    @Column
    private Integer status = 1; // 1: active, 0: inactive

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;
}
