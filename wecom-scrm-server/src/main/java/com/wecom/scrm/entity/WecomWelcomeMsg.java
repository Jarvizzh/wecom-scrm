package com.wecom.scrm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_welcome_msg")
@Data
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class WecomWelcomeMsg {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String text;

    @Column(columnDefinition = "JSON")
    private String attachments;

    @Column(name = "user_ids", columnDefinition = "JSON")
    private String userIds;

    @Column(name = "department_ids", columnDefinition = "JSON")
    private String departmentIds;

    @Column(name = "is_default", columnDefinition = "TINYINT DEFAULT 0")
    private Integer isDefault;

    @CreatedDate
    @Column(name = "sys_create_time", updatable = false)
    private LocalDateTime sysCreateTime;

    @LastModifiedDate
    @Column(name = "sys_update_time")
    private LocalDateTime sysUpdateTime;
}
