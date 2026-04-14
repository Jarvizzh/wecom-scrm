package com.wecom.scrm.entity;

import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "wecom_group_chat_member")
@Data
@NoArgsConstructor
public class WecomGroupChatMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", length = 64)
    private String chatId;

    @Column(length = 64)
    private String userid;

    @Column(columnDefinition = "TINYINT")
    private Integer type; // 1: Internal, 2: External

    @Column(name = "join_time")
    private LocalDateTime joinTime;

    @Column(name = "join_scene")
    private Integer joinScene;

    @Column(length = 64)
    private String invitor;
}
