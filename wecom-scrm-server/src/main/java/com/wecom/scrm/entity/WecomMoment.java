package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_moment")
@Data
@NoArgsConstructor
public class WecomMoment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moment_id", length = 64)
    private String momentId;

    @Column(name = "jobid", length = 64)
    private String jobid;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "TEXT")
    private String attachments;

    @Column(name = "visible_range_type", columnDefinition = "TINYINT DEFAULT 0")
    private Integer visibleRangeType;

    @Column(name = "visible_range_users", columnDefinition = "TEXT")
    private String visibleRangeUsers;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status; // 0: Pending, 1: Published, 2: Failed

    @Column(name = "creator_userid", length = 64)
    private String creatorUserid;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
