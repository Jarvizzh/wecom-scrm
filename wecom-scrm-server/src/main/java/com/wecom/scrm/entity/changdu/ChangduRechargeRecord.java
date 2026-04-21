package com.wecom.scrm.entity.changdu;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_changdu_recharge_record")
public class ChangduRechargeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "distributor_id", nullable = false)
    private Long distributorId;

    @Column(name = "trade_no", unique = true)
    private Long tradeNo;

    @Column(name = "out_trade_no", length = 128)
    private String outTradeNo;

    @Column(name = "device_id", length = 128)
    private String deviceId;

    @Column(name = "open_id", length = 64)
    private String openId;

    @Column(name = "external_id", length = 64)
    private String externalId;

    @Column(name = "pay_way", length = 32)
    private String payWay; //支付方式：1 微信 2 支付宝 5 抖音支付 6 抖音钻石支付 200 未支付完成

    @Column(name = "pay_fee")
    private Long payFee;

    @Column(name = "status", length = 32)
    private String status; // 0 已支付 1 未支付

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "book_name", length = 255)
    private String bookName;

    @Column(name = "promotion_id")
    private Long promotionId;

    @Column(name = "pay_time")
    private String payTime;

    @Column(name = "order_create_time")
    private String orderCreateTime;

    @Column(name = "recharge_type")
    private Integer rechargeType;  //0-单次充值 1-会员充值 2-整剧购买 3-连包付费

    @Column(name = "order_type")
    private Integer orderType; // 1-虚拟支付, 2-非虚拟支付

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
