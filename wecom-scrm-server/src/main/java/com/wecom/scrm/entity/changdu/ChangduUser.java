package com.wecom.scrm.entity.changdu;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_changdu_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"distributor_id", "encrypted_device_id"})
})
public class ChangduUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distributor_id", nullable = false)
    private Long distributorId;

    @Column(name = "encrypted_device_id", nullable = false, length = 128)
    private String encryptedDeviceId;

    @Column(name = "device_brand", length = 64)
    private String deviceBrand;

    @Column(name = "media_source", length = 64)
    private String mediaSource;

    @Column(name = "book_source", length = 128)
    private String bookSource;

    @Column(name = "recharge_times")
    private Long rechargeTimes;

    @Column(name = "recharge_amount")
    private Long rechargeAmount;

    @Column(name = "balance_amount")
    private Long balanceAmount;

    @Column(name = "register_time")
    private String registerTime;

    @Column(name = "promotion_id", length = 64)
    private String promotionId;

    @Column(name = "promotion_name", length = 255)
    private String promotionName;

    @Column(name = "book_name", length = 255)
    private String bookName;

    @Column(name = "external_id", length = 64)
    private String externalId;

    @Column(name = "open_id", length = 64)
    private String openId;

    @Column(length = 128)
    private String nickname;

    @Column(length = 255)
    private String avatar;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
