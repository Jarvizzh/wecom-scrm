package com.wecom.scrm.entity;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Entity
@Table(name = "wecom_enterprise")
@EntityListeners(AuditingEntityListener.class)
public class WecomEnterprise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "corp_id", nullable = false, unique = true, length = 64)
    private String corpId;

    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "agent_id", nullable = false)
    private Integer agentId;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "agent_secret", nullable = false, length = 128)
    private String agentSecret;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "token", length = 128)
    private String token;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "encoding_aes_key", length = 128)
    private String encodingAesKey;

    @Column(name = "db_url", nullable = false, length = 255)
    private String dbUrl;

    @Column(name = "db_username", nullable = false, length = 64)
    private String dbUsername;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "db_password", nullable = false, length = 64)
    private String dbPassword;

    @Column(name = "status")
    private Integer status = 1; // 1=active, 0=inactive

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private Date updateTime;
}
