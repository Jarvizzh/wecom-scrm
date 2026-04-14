package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_customer_message_record")
@Data
@NoArgsConstructor
public class WecomCustomerMessageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "message_id", nullable = false)
    private Long messageId;

    @Column(name = "external_userid", nullable = false, length = 64)
    private String externalUserid;

    @Column(name = "userid", nullable = false, length = 64)
    private String userid;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0: Pending, 1: Sent, 2: Failed, 3: Sent unseen, etc.

    @Column(name = "send_time")
    private LocalDateTime sendTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
