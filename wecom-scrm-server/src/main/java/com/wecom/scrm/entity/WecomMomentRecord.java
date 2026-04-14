package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_moment_record")
@Data
@NoArgsConstructor
public class WecomMomentRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "moment_id", nullable = false)
    private Long momentId;

    @Column(nullable = false, length = 64)
    private String userid;

    @Column(name = "publish_status", columnDefinition = "TINYINT DEFAULT 0")
    private Integer publishStatus; // 0: Not Published, 1: Published

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
