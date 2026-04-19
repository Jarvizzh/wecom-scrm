package com.wecom.scrm.entity.yuewen;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wecom_yuewen_consume_record")
public class YuewenConsumeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "app_flag", nullable = false, length = 64)
    private String appFlag;

    @Column(nullable = false, length = 64)
    private String openid;

    private Long guid;

    @Column(name = "order_id", length = 128)
    private String orderId;

    @Column(name = "consume_id", length = 128)
    private String consumeId;

    @Column(name = "worth_amount")
    private Integer worthAmount;

    @Column(name = "free_amount")
    private Integer freeAmount;

    @Column(name = "consume_time")
    private LocalDateTime consumeTime;

    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "book_name", length = 255)
    private String bookName;

    @Column(name = "chapter_id", length = 128)
    private String chapterId;

    @Column(name = "chapter_name", length = 255)
    private String chapterName;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
