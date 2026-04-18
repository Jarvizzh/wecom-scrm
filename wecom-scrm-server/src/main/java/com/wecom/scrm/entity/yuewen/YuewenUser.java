package com.wecom.scrm.entity.yuewen;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_yuewen_user")
public class YuewenUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long guid;

    @Column(nullable = false, length = 64)
    private String openid;

    @Column(length = 128)
    private String nickname;

    @Column(name = "app_flag", nullable = false, length = 64)
    private String appFlag;

    @Column(name = "wx_app_id", length = 64)
    private String wxAppId;

    @Column(name = "charge_amount")
    private Long chargeAmount;

    @Column(name = "charge_num")
    private Integer chargeNum;

    @Column(name = "is_subscribe")
    private Integer isSubscribe;

    @Column(name = "regist_time")
    private LocalDateTime registTime;

    @Column(name = "yuewen_update_time")
    private LocalDateTime yuewenUpdateTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
