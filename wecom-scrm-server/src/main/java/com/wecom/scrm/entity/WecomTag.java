package com.wecom.scrm.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_tag")
public class WecomTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tag_id", unique = true, nullable = false, length = 64)
    private String tagId;

    @Column(name = "group_id", nullable = false, length = 64)
    private String groupId;

    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Column(name = "order_num")
    private Long order;

    @CreationTimestamp
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
