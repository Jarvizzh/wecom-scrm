package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_user")
@Data
@NoArgsConstructor
public class WecomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 64)
    private String userid;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 20)
    private String mobile;

    @Column(name = "department_ids")
    private String departmentIds;

    @Column(columnDefinition = "TINYINT DEFAULT 1")
    private Integer status;

    @Column(name = "scrm_status", columnDefinition = "TINYINT DEFAULT 0")
    private Integer scrmStatus; // 0: Normal, 1: Banned

    private String avatar;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
