package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_group_chat")
@Data
@NoArgsConstructor
public class WecomGroupChat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", length = 64, unique = true)
    private String chatId;

    @Column(length = 128)
    private String name;

    @Column(length = 64)
    private String owner;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(columnDefinition = "TEXT")
    private String notice;

    @Column(name = "member_count")
    private Integer memberCount;

     /**
     * 客户群状态
     * 0 - 正常
     * 1 - 已解散
     * 2 - 已删除
     */
    @Column
    private Integer status;
    @CreationTimestamp
    @Column(name = "sys_create_time", updatable = false)
    private LocalDateTime sysCreateTime;

    @UpdateTimestamp
    @Column(name = "sys_update_time")
    private LocalDateTime sysUpdateTime;
}
