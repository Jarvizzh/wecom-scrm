package com.wecom.scrm.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Data
@Entity
@Table(name = "wecom_mp_user")
public class WecomMpUser implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64)
    private String openid;

    @Column(length = 64)
    private String unionid;

    @Column(length = 128)
    private String nickname;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column
    private Integer gender; // 0: unknown, 1: male, 2: female

    @Column(length = 64)
    private String country;

    @Column(length = 64)
    private String province;

    @Column(length = 64)
    private String city;

    @Column(name = "mp_app_id", nullable = false, length = 64)
    private String mpAppId;

    @Column(name = "mp_name", length = 128)
    private String mpName;

    @Column(name = "subscribe_time")
    private Date subscribeTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private Date updateTime;
}
