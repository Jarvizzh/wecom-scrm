package com.wecom.scrm.entity.yuewen;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_yuewen_recharge_record")
public class YuewenRechargeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_flag", length = 64)
    private String appFlag;

    @Column(name = "app_name", length = 128)
    private String appName;

    @Column(name = "amount")
    private String amount;

    @Column(name = "yw_order_id", unique = true, length = 128)
    private String ywOrderId;

    @Column(name = "order_id", length = 128)
    private String orderId;

    @Column(name = "order_time")
    private LocalDateTime orderTime;

    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "order_status")
    private Integer orderStatus;

    @Column(name = "order_type")
    private Integer orderType;

    @Column(name = "openid", length = 64)
    private String openid;

    @Column(name = "guid")
    private Long guid;

    @Column(name = "sex")
    private Integer sex;

    @Column(name = "reg_time")
    private LocalDateTime regTime;

    @Column(name = "sub_time")
    private LocalDateTime subTime;

    @Column(name = "channel_id")
    private Long channelId;

    @Column(name = "channel_name", length = 128)
    private String channelName;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "book_name", length = 255)
    private String bookName;

    @Column(name = "wx_appid", length = 64)
    private String wxAppId;

    @Column(name = "item_name", length = 128)
    private String itemName;

    @Column(name = "order_channel")
    private Integer orderChannel;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
