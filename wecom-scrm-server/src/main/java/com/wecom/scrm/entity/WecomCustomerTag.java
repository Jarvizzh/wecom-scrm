package com.wecom.scrm.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_customer_tag")
public class WecomCustomerTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_userid", nullable = false, length = 64)
    private String externalUserid;

    @Column(name = "tag_id", nullable = false, length = 64)
    private String tagId;

    @Column(name = "userid", nullable = false, length = 64)
    private String userid; // The employee who added this tag

    @CreationTimestamp
    private LocalDateTime createTime;
}
