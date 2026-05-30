package com.wecom.scrm.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_material")
@Data
@NoArgsConstructor
public class WecomMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 32)
    private String type; // TEXT, IMAGE, H5, MINIPROGRAM

    @Column(columnDefinition = "TEXT")
    private String content; // Text content, Image local path, H5 Link URL

    @Column(name = "app_id", length = 64)
    private String appId;

    @Column(name = "page_path")
    private String pagePath;

    @Column(name = "source_type", length = 32)
    private String sourceType; // NOVEL, DRAMA

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
