package com.wecom.scrm.entity;

import com.baomidou.dynamic.datasource.annotation.DS;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@DS("master")
@Table(name = "sys_user")
public class SysUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String username;

    @Column(nullable = false, length = 128)
    private String password;

    @Column(length = 64)
    private String nickname;

    @Column(length = 255)
    private String avatar;

    @Column(columnDefinition = "TINYINT DEFAULT 0")
    private Integer status = 0; // 0-Normal, 1-Banned

    @Column(name = "is_super_admin", columnDefinition = "BIT DEFAULT 0")
    private Boolean isSuperAdmin = false;

    @Column(columnDefinition = "TEXT")
    private String permissions; // JSON string of route paths

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
